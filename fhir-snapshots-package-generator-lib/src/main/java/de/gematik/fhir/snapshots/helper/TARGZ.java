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
package de.gematik.fhir.snapshots.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@UtilityClass
public class TARGZ {

    public static void compress(Path sourceDirectory, String outputDirectory) throws IOException {
        validateDirectory(sourceDirectory);

        String tarFileName = getTarFileName(sourceDirectory, outputDirectory);

        try (OutputStream fileOutputStream = Files.newOutputStream(Paths.get(tarFileName));
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {

            configureTarOutputStream(tarOutputStream);

            Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    if (!attributes.isSymbolicLink()) {
                        Path targetFile = sourceDirectory.relativize(file);
                        addFileToTar(file, targetFile, tarOutputStream);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("Unable to tar.gz : {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });

            tarOutputStream.finish();
        }
    }

    public static void decompress(String inputFileName, File outputDirectory) throws IOException {
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(inputFileName)))) {

            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String currentEntryName = entry.getName();
                File currentFile = ZipSlipProtect.newFile(outputDirectory, currentEntryName);
                File parent = currentFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                try(var outputFileStream = new FileOutputStream(currentFile)) {
                    IOUtils.copy(tarInputStream, outputFileStream);
                }
            }
        }
    }

    private static void validateDirectory(Path source) throws IOException {
        if (!Files.isDirectory(source)) {
            throw new IOException("Please provide a directory.");
        }
    }

    private static String getTarFileName(Path sourceDirectory, String outputDirectory) {
        return outputDirectory + sourceDirectory.getFileName().toString() + ".tgz";
    }

    private static void configureTarOutputStream(TarArchiveOutputStream tOut) {
        tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    }

    private static void addFileToTar(Path file, Path targetFile, TarArchiveOutputStream tOut) throws IOException {
        TarArchiveEntry tarEntry = new TarArchiveEntry(file.toFile(), targetFile.toString());

        // setting default values for tar meta data to prevent misleading changes between final .tgz files when regenerating snapshots
        tarEntry.setModTime(0);
        tarEntry.setUserName("defaultUser");
        tarEntry.setGroupName("defaultGroup");
        tarEntry.setUserId(1000);
        tarEntry.setGroupId(1000);

        tOut.putArchiveEntry(tarEntry);
        Files.copy(file, tOut);
        tOut.closeArchiveEntry();
    }

}


