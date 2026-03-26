package de.gematik.fhir.snapshots;

/*-
 * #%L
 * fhir-snapshots-package-generator-lib
 * %%
 * Copyright (C) 2024 - 2026 gematik GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes
 * by gematik, find details in the "Readme" file.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import de.gematik.fhir.snapshots.helper.DependencyGenerator;
import de.gematik.fhir.snapshots.helper.NpmPackageLoader;
import de.gematik.fhir.snapshots.helper.TARGZ;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StructureDefinition;

/**
 * The SnapshotGenerator class is responsible for generating FHIR snapshots from FHIR package files
 * and their dependencies.
 */
@Slf4j
@NoArgsConstructor
public class SnapshotGenerator {

  private static final FhirContext fhirContext = FhirContext.forR4Cached();
  private final DependencyGenerator dependencyGenerator = new DependencyGenerator();
  private final Map<String, IBaseResource> currentPatches = new HashMap<>();
  private IValidationSupport snapshotGeneratingValidationSupport;
  private ValidationSupportChain chain;
  private String currentPackageName = "";

  /**
   * Generates snapshots for all FHIR packages and their dependencies.
   *
   * @param packageFolderPath The path to the folder containing source FHIR packages.
   * @param outputFolder The output folder where FHIR packages with the generated snapshots will be
   *     stored.
   * @param tempDir The temporary directory for decompressing the FHIR packages.
   * @throws IOException If an I/O error occurs during the snapshot generation process.
   */
  public void generateSnapshots(String packageFolderPath, String outputFolder, String tempDir)
      throws IOException {
    generateSnapshots(packageFolderPath, outputFolder, new ArrayList<>(), tempDir);
  }

  /**
   * Generates snapshots for specified FHIR packages and their dependencies.
   *
   * @param packageFolderPath The path to the folder containing source FHIR packages.
   * @param outputFolder The output folder where FHIR packages with the generated snapshots will be
   *     stored.
   * @param packagesForSnapshotGeneration The list of package names (optional). If empty, all
   *     packages are processed.
   * @param tempDir The temporary directory for decompressing the FHIR packages.
   * @throws IOException If an I/O error occurs during the snapshot generation process.
   */
  public void generateSnapshots(
      String packageFolderPath,
      String outputFolder,
      Collection<String> packagesForSnapshotGeneration,
      String tempDir)
      throws IOException {
    File packageFolder = new File(packageFolderPath);
    File[] tgzFiles = packageFolder.listFiles((dir, name) -> name.endsWith(".tgz"));

    if (tgzFiles == null || tgzFiles.length == 0) {
      log.warn("No FHIR packages found at: {}", packageFolderPath);
      return;
    }

    final List<String> allPackageNames = Arrays.stream(tgzFiles).map(File::getName).toList();

    final Set<String> packagesToGenerate =
        packagesForSnapshotGeneration.isEmpty()
            ? new HashSet<>(allPackageNames)
            : new HashSet<>(packagesForSnapshotGeneration);

    for (String packageName : allPackageNames) {

      if (!packagesToGenerate.contains(packageName)) {
        log.info("Skipping package: {} (not specified for snapshot generation)", packageName);
        continue;
      }

      log.info("Starting snapshot generation for {}", packageName);
      List<String> dependencies =
          dependencyGenerator.generateListOfDependenciesFor(packageName, packageFolderPath);
      generateSnapshotsAndCompressAsTgz(
          packageFolderPath, outputFolder, packageName, dependencies, tempDir);
    }
  }

  private void generateSnapshotsAndCompressAsTgz(
      String sourceDir,
      String outputDir,
      String filename,
      List<String> dependencies,
      String tempDir)
      throws IOException {
    setupSupportChain(dependencies, sourceDir);
    final Path packageRoot = decompressPackage(sourceDir, filename, tempDir);
    processDecompressedPackage(packageRoot);
    compressPackage(outputDir, tempDir);
    log.info("Finished snapshot generation for {}", filename);
  }

  private void setupSupportChain(List<String> dependencies, String sourceDir) throws IOException {
    var npmPackageSupport =
        NpmPackageLoader.createValidationSupportForPackages(fhirContext, dependencies, sourceDir);
    getPatches(dependencies, sourceDir);

    PrePopulatedValidationSupport patchesSupport = new PrePopulatedValidationSupport(fhirContext);

    for (Map.Entry<String, IBaseResource> entry : currentPatches.entrySet()) {
      log.info("Applying patch for {}", entry.getValue());
      patchesSupport.addResource(entry.getValue());
    }
    IValidationSupport validationSupport = fhirContext.getValidationSupport();
    snapshotGeneratingValidationSupport = new SnapshotGeneratingValidationSupport(fhirContext);
    chain =
        new ValidationSupportChain(
            patchesSupport,
            npmPackageSupport,
            validationSupport,
            snapshotGeneratingValidationSupport);
  }

  private void getPatches(List<String> dependencies, String sourceDir) throws IOException {
    currentPatches.clear();
    for (String currentPackageFilename : dependencies) {
      getPatchesFor(currentPackageFilename.replace(".tgz", ""), sourceDir);
    }
  }

  private void getPatchesFor(String currentPackage, String sourceDir) throws IOException {
    File directory = new File(sourceDir + "patches/" + currentPackage);
    if (directory.exists()) {
      File[] directoryListing = directory.listFiles();
      if (directoryListing != null) {
        for (File child : directoryListing) {
          if (child.getName().endsWith(".json")) {
            try (FileInputStream inputStream = new FileInputStream(child)) {
              var reader = new InputStreamReader(inputStream);
              var patch = fhirContext.newJsonParser().parseResource(reader);
              currentPatches.put(child.getName(), patch);
            }
          }
        }
      }
    }
  }

  private Path decompressPackage(String sourceDir, String fileName, String tempDirPath)
      throws IOException {
    currentPackageName =
        fileName.endsWith(".tgz") ? fileName.substring(0, fileName.length() - 4) : fileName;
    File tempDir = new File(tempDirPath + currentPackageName);
    FileUtils.deleteDirectory(tempDir);
    TARGZ.decompress(sourceDir + fileName, tempDir);
    return tempDir.toPath();
  }

  private void compressPackage(String outputDir, String tempDir) throws IOException {
    Path source = Paths.get(tempDir + currentPackageName);
    Files.createDirectories(Paths.get(outputDir));
    TARGZ.compress(source, outputDir);
  }

  private static void writeResource(IBaseResource resource, File newFile) throws IOException {
    FileUtils.write(
        newFile,
        fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource),
        StandardCharsets.UTF_8);
  }

  private IBaseResource generateSnapshot(IBaseResource resource) {
    return snapshotGeneratingValidationSupport.generateSnapshot(
        new ValidationSupportContext(chain), resource, null, null, null);
  }

  private void logGeneratingSnapshotFor(String currentFileName) {
    String packageAndProfile =
        String.format("(%s) %s", currentPackageName, currentFileName.replace("/package/", ""));
    log.info("Generating snapshot for: {}", packageAndProfile);
  }

  private void processDecompressedPackage(Path packageRoot) throws IOException {
    Path packageDir = packageRoot.resolve("package");

    try (var files = Files.list(packageDir)) {
      for (Path file : files.filter(Files::isRegularFile).toList()) {
        String fileName = file.getFileName().toString();
        if (!fileName.endsWith(".json")
            || "package.json".equals(fileName)
            || ".index.json".equals(fileName)) {
          continue;
        }
        processPackageFile(file);
      }
    }
  }

  private void processPackageFile(Path file) throws IOException {
    String resourceFileName = file.getFileName().toString();
    IBaseResource patchedResource = currentPatches.get(resourceFileName);

    if (patchedResource != null && !(patchedResource instanceof StructureDefinition)) {
      writeResource(patchedResource, file.toFile());
      return;
    }

    IBaseResource originalResource;
    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      originalResource = fhirContext.newJsonParser().parseResource(reader);
    }

    if (patchedResource == null && !(originalResource instanceof StructureDefinition)) {
      return;
    }

    IBaseResource resourceForSnapshot =
        patchedResource != null ? patchedResource : originalResource;
    writeSnapshotOrFallback(resourceForSnapshot, originalResource, file.toFile());
  }

  private void writeSnapshotOrFallback(
      IBaseResource snapshotSource, IBaseResource originalResource, File newFile)
      throws IOException {
    logGeneratingSnapshotFor(newFile.getName());
    try {
      writeResource(generateSnapshot(snapshotSource), newFile);
    } catch (RuntimeException | Error e) {
      // Suppress error and copy the original file due to HAPI 8.8.1 validation
      if (Boolean.parseBoolean(
          System.getenv().getOrDefault("WRITE_ORIGINAL_ON_SNAPSHOT_FAIL", "true"))) {
        log.warn(
            "Snapshot Generation failed with: {} - writing original resource to {}",
            e.getLocalizedMessage(),
            newFile);
        writeResource(originalResource, newFile);
      } else {
        log.error("Snapshot Generation failed with: {}", e.getLocalizedMessage(), e);
        throw new IOException(e);
      }
    }
  }
}
