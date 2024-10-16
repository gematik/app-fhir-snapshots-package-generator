/*
Copyright (c) 2023-2024 gematik GmbH

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
package de.gematik.fhir.snapshots.support;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
//solution for checking if directories are equal taken and adjusted from: http://www.java2s.com/example/java/java.nio.file/compare-the-contents-of-two-directories-to-determine-if-they-are-equal.html
public class DirectoryComparator {

    public static boolean directoryContentEquals(Path dir1, Path dir2) throws IOException {
        boolean dir1Exists = Files.exists(dir1) && Files.isDirectory(dir1);

        if(!dir1Exists)
            throw new IllegalArgumentException("Directory " + dir1 + " does not exist or is not a directory");


        boolean dir2Exists = Files.exists(dir2) && Files.isDirectory(dir2);

        if(!dir2Exists)
            throw new IllegalArgumentException("Directory " + dir2 + " does not exist or is not a directory");

        HashMap<Path, Path> dir1Paths = new HashMap<>();
        HashMap<Path, Path> dir2Paths = new HashMap<>();

        // Map the path relative to the base directory to the complete path.
        for (Path p : listPaths(dir1)) {
            dir1Paths.put(dir1.relativize(p), p);
        }

        for (Path p : listPaths(dir2)) {
            dir2Paths.put(dir2.relativize(p), p);
        }

        // The directories cannot be equal if the number of files aren't equal.
        if (dir1Paths.size() != dir2Paths.size()) {
            log.debug("Number of files in directories are not equal: {}, {}", dir1Paths.size(), dir2Paths.size());
            return false;
        }

        // For each file in dir1, check if also dir2 contains this file and if
        // their contents are equal.
        for (Map.Entry<Path, Path> pathEntry : dir1Paths.entrySet()) {
            Path relativePath = pathEntry.getKey();
            Path absolutePath = pathEntry.getValue();
            if (!dir2Paths.containsKey(relativePath)) {
                log.debug("File {} from dir1 ({}) not found in dir2 ({}))", relativePath, dir1, dir2);
                return false;
            } else {
                if (!contentEquals(absolutePath, dir2Paths.get(relativePath))) {
                    log.debug("Content of file {} (folder {}) differs from {} (folder {})", absolutePath, dir1, dir2Paths.get(relativePath), dir2);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Recursively finds all files with given extensions in the given directory
     * and all of its sub-directories.
     */
    private static List<Path> listPaths(Path file, String... extensions) throws IOException {
        if (file == null) {
            return null;
        }

        List<Path> paths = new ArrayList<>();
        listPaths(file, paths, extensions);

        return paths;
    }

    /**
     * Recursively finds all paths with given extensions in the given directory
     * and all of its sub-directories.
     */
    protected static void listPaths(Path path, List<Path> result, String... extensions) throws IOException {
        if (path == null) {
            return;
        }

        if (Files.isReadable(path)) {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                    for (Path p : directoryStream) {
                        listPaths(p, result, extensions);
                    }
                }
            } else {
                String filename = path.getFileName().toString();
                if (extensions.length == 0) {
                    result.add(path);
                } else {
                    for (String extension : extensions) {
                        if (filename.toLowerCase().endsWith(extension)) {
                            result.add(path);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Compares the contents of the two given paths. If both paths don't exist,
     * the contents aren't equal and this method returns false.
     */
    private static boolean contentEquals(Path p1, Path p2) throws IOException {
        if (!Files.exists(p1) || !Files.exists(p2)) {
            log.debug("One of files do not exist: {}, {}", p1, p2);
            return false;
        }

        if (Files.isDirectory(p1) && Files.isDirectory(p2)) {
            return directoryContentEquals(p1, p2);
        }

        if (p1.equals(p2)) {
            return true;
        }

        if (Files.size(p1) != Files.size(p2)) {
            log.debug("Files have different size: {}, {}", Files.size(p1), Files.size(p2));
            return false;
        }

        try (InputStream in1 = Files.newInputStream(p1);
             InputStream in2 = Files.newInputStream(p2)) {

            int expectedByte = in1.read();
            while (expectedByte != -1) {
                int readByte = in2.read();
                if (expectedByte != readByte) {
                    log.debug("Unexpected byte in in2: {}, {}", expectedByte, readByte);
                    return false;
                }
                expectedByte = in1.read();
            }
            return in2.read() == -1;
        }
    }
}
