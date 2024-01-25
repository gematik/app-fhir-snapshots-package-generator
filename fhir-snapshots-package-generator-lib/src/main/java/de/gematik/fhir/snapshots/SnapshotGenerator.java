/*
Copyright (c) 2023 gematik GmbH

Licensed under the Apache License, Version 2.0 (the License);
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.gematik.fhir.snapshots;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import de.gematik.fhir.snapshots.helper.DependencyGenerator;
import de.gematik.fhir.snapshots.helper.FixedSnapshotGeneratingValidationSupport;
import de.gematik.fhir.snapshots.helper.NpmPackageLoader;
import de.gematik.fhir.snapshots.helper.TARGZ;
import de.gematik.fhir.snapshots.helper.ZipSlipProtect;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SnapshotGenerator class is responsible for generating FHIR snapshots
 * from FHIR package files and their dependencies.
 */
@Slf4j
@NoArgsConstructor
public class SnapshotGenerator {

    private static final FhirContext fhirContext = FhirContext.forR4();
    private final DependencyGenerator dependencyGenerator = new DependencyGenerator();
    private final Map<String, IBaseResource> currentPatches = new HashMap<>();
    private FixedSnapshotGeneratingValidationSupport snapshotGeneratingValidationSupport;
    private ValidationSupportChain chain;
    private String currentPackageName = "";
    private static final String PACKAGE_FOLDER_PREFIX = "package/";

    /**
     * Generates snapshots for FHIR packages and their dependencies.
     *
     * @param packageFolderPath The path to the folder containing source FHIR packages.
     * @param outputFolder      The output folder where FHIR packages with the generated snapshots will be stored.
     * @param workingDirectory  The temporary directory for decompressing the FHIR packages.
     * @throws IOException If an I/O error occurs during the snapshot generation process.
     */
    public void generateSnapshots(String packageFolderPath, String outputFolder, String workingDirectory) throws IOException {
        if(workingDirectory.isEmpty()) {
            workingDirectory = System.getProperty("java.io.tmpdir") + File.separator;
        }
        File packageFolder = new File(packageFolderPath);
        File[] tgzFiles = packageFolder.listFiles((dir, name) -> name.endsWith(".tgz"));
        if(tgzFiles != null) {
            for (File fhirPackageFile : tgzFiles) {
                log.info("Starting snapshot generation for {}", fhirPackageFile.getName());
                List<String> dependencies = dependencyGenerator.generateListOfDependenciesFor(fhirPackageFile.getName(), packageFolderPath);
                generateSnapshotsAndCompressAsTgz(packageFolderPath, outputFolder, fhirPackageFile.getName(), dependencies, workingDirectory);
            }
        } else {
            log.error("No fhir packages found at: {}", packageFolderPath);
        }
    }

    private void generateSnapshotsAndCompressAsTgz(String sourceDir, String outputDir, String filename, List<String> dependencies, String decompressDir) throws IOException {
        setupSupportChain(dependencies, sourceDir);
        decompressPackage(sourceDir, filename, decompressDir);
        readStructureDefinitionsFromTgz(sourceDir, filename, decompressDir);
        compressPackage(outputDir, decompressDir);
        log.info("Finished snapshot generation for {}", filename);
    }

    private void setupSupportChain(List<String> dependencies, String sourceDir) throws IOException {
        var npmPackageSupport = new NpmPackageLoader().loadPackagesAndCreatePrePopulatedValidationSupport(fhirContext, dependencies, sourceDir);
        getPatches(dependencies, sourceDir);

        PrePopulatedValidationSupport patchesSupport = new PrePopulatedValidationSupport(fhirContext);

        for (Map.Entry<String, IBaseResource> entry : currentPatches.entrySet()) {
            log.info("Applying patch for {}", entry.getValue());
            patchesSupport.addResource(entry.getValue());
        }
        IValidationSupport validationSupport = fhirContext.getValidationSupport();
        snapshotGeneratingValidationSupport = new FixedSnapshotGeneratingValidationSupport(fhirContext);
        chain = new ValidationSupportChain(
                patchesSupport,
                npmPackageSupport,
                validationSupport,
                snapshotGeneratingValidationSupport
        );
    }

    private void getPatches(List<String> dependencies, String sourceDir) throws IOException {
        currentPatches.clear();
        for(String currentPackageFilename : dependencies) {
            getPatchesFor(currentPackageFilename.replace(".tgz", ""), sourceDir);
        }
    }

    private void getPatchesFor(String currentPackage, String sourceDir) throws IOException {
        File directory = new File(sourceDir + "patches/" + currentPackage);
        if(directory.exists()){
            File[] directoryListing = directory.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (child.getName().endsWith(".json")) {
                        try(FileInputStream inputStream = new FileInputStream(child)) {
                            var reader = new InputStreamReader(inputStream);
                            var patch = fhirContext.newJsonParser().parseResource(reader);
                            currentPatches.put(child.getName(), patch);
                        }
                    }
                }
            }
        }
    }

    private void decompressPackage(String sourceDir, String fileName, String decompressDirPath) throws IOException {
        currentPackageName = fileName.replaceAll(".tgz", "");
        File decompressDir = new File(decompressDirPath + currentPackageName);
        FileUtils.deleteDirectory(decompressDir);
        TARGZ.decompress(sourceDir + fileName, decompressDir);
    }

    private void compressPackage(String outputDir, String decompressDir) throws IOException {
        Path source = Paths.get(decompressDir + currentPackageName);
        Files.createDirectories(Paths.get(outputDir));
        TARGZ.compress(source, outputDir);
    }

    private void readStructureDefinitionsFromTgz(String sourceDir, String filename, String decompressDir) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(sourceDir + filename);
                GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
                TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(tarInputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {

            TarArchiveEntry currentEntry = tarInputStream.getNextEntry();
            while (currentEntry != null) {

                String currentEntryName = currentEntry.getName();
                log.debug("Processing " + currentEntryName);

                // Only work with files on the top level of the "package" folder and only handle .json files
                try {
                    createSnapshotIfStructureDefinitionAndWrite(decompressDir, currentEntryName, bufferedReader);

                    currentEntry = tarInputStream.getNextEntry();
                } catch (Exception e) {
                    log.error("Could not create a snapshot for " + currentEntryName + " (" + filename + ")", e);
                    throw e;
                }
            }
        }
    }

    private void createSnapshotIfStructureDefinitionAndWrite(String decompressDir, String currentEntryName, BufferedReader bufferedReader) throws IOException {
        if (currentEntryName.startsWith(PACKAGE_FOLDER_PREFIX) && !currentEntryName.substring(PACKAGE_FOLDER_PREFIX.length()).contains("/") && currentEntryName.endsWith(".json")) {
            File destDir = new File(decompressDir + currentPackageName);
            File newFile = ZipSlipProtect.newFile(destDir, currentEntryName);

            if(fileShouldBeIgnored(currentEntryName)) {
                String resourceFileName = currentEntryName.replace(PACKAGE_FOLDER_PREFIX, "");

                var originalResource = fhirContext.newJsonParser().parseResource(bufferedReader);
                var patchedResource = currentPatches.getOrDefault(resourceFileName, null);

                // Original ValueSet, CodeSystem etc. without Patch --> ignore
                if (patchedResource == null && !(originalResource instanceof StructureDefinition))
                    return;

                // Patched ValueSets, CodeSystems etc. --> copy without snapshot generation
                if (patchedResource != null && !(originalResource instanceof StructureDefinition)) {
                    writeResource(patchedResource, newFile);
                    return;
                }

                // SDs with or without Patch --> generate snapshot
                var patchOrOriginalStructureDefinition = patchedResource != null ? patchedResource : originalResource;
                logGeneratingSnapshotFor(newFile.getName());
                var snapshot = generateSnapshot(patchOrOriginalStructureDefinition);
                writeResource(snapshot, newFile);
            }
        }
    }

    private static boolean fileShouldBeIgnored(String currentEntryName) {
        return !currentEntryName.equals("package/package.json") && !currentEntryName.equals("package/.index.json");
    }

    private static void writeResource(IBaseResource resource, File newFile) throws IOException {
        try (FileWriterWithEncoding fileWriterWithEncoding = new FileWriterWithEncoding(newFile, StandardCharsets.UTF_8)) {
            FileUtils.write(newFile, "", StandardCharsets.UTF_8);
            fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(resource, fileWriterWithEncoding);
        }
    }

    private IBaseResource generateSnapshot(IBaseResource resource)  {
        return snapshotGeneratingValidationSupport.generateSnapshot(
                new ValidationSupportContext(chain), resource, null, null, null);
    }

    private void logGeneratingSnapshotFor(String currentFileName) {
        String packageAndProfile = String.format("(%s) %s", currentPackageName, currentFileName.replace("/package/", ""));
        log.info("Generating snapshot for: {}", packageAndProfile);
    }
}
