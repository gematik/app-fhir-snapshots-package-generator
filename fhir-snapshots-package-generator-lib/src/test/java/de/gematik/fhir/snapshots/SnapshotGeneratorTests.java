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

import de.gematik.fhir.snapshots.helper.TARGZ;
import de.gematik.fhir.snapshots.support.DirectoryComparator;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SnapshotGeneratorTests {

    private static final String CORRECT_PACKAGE = "src/test/resources/package/minimal.example-1.0.0-correct.tgz";
    private static final String OUTPUT_SNAPSHOT_PACKAGES_DIR = "target/generated-snapshots/";
    private static final String SRC_PACKAGES_DIR = "src/test/resources/src-package/";
    public static final String EXCLUDED_PACKAGE = "excluded.package-1.0.0.tgz";
    private static SnapshotGenerator snapshotGenerator;

    @SneakyThrows
    private static String getDecompressDir() {
        return Paths.get(Objects.requireNonNull(SnapshotGenerator.class.getResource("/")).toURI()).getParent().toString() + "/decompressed-packages/";
    }

    @BeforeAll
    static void setUp() {
        snapshotGenerator = new SnapshotGenerator();
    }

    @BeforeEach
    @SneakyThrows
    void beforeTest() {
        FileUtils.deleteDirectory(new File(OUTPUT_SNAPSHOT_PACKAGES_DIR));
    }

    @Test
    @SneakyThrows
    void testGenerateSnapshotsEqual() {
        File correctSnapshotPackage = new File(CORRECT_PACKAGE);
        snapshotGenerator.generateSnapshots(SRC_PACKAGES_DIR, OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir(), new ArrayList<>());
        File generatedSnapshotPackage = new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "minimal.example-1.0.0.tgz");
        assertTrue(comparePackages(correctSnapshotPackage, generatedSnapshotPackage));
    }

    @Test
    @SneakyThrows
    void testPackagesCanBeExcluded() {
        List<String> excludedPackages = new ArrayList<>();
        excludedPackages.add(EXCLUDED_PACKAGE);
        var snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(SRC_PACKAGES_DIR, OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir(), excludedPackages);
        assertFalse(new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "excluded.package-1.0.0.tgz").exists());
    }

    @Test
    @SneakyThrows
    void testNoExceptionOnDependenciesWithUpperCase() {
        var snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots("src/test/resources/src-package-upper-case/", OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir(), new ArrayList<>());
        File generatedSnapshotPackage = new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "dependencies-in-upper-case-1.0.0.tgz");
        assertTrue(generatedSnapshotPackage.exists(), "No snapshots generated for package with upper-case dependencies");
    }

    @Test
    @SneakyThrows
    void testCleanUpDirectoryWorksBeforeSnapshotGeneration() {
        String path = Paths.get(Objects.requireNonNull(SnapshotGenerator.class.getResource("/")).toURI()).getParent().toString() + "/decompressed-packages/minimal.example-1.0.0/";
        File file = new File(path + "test");
        if(!file.exists()) {
            file.mkdir();
        }
        snapshotGenerator.generateSnapshots(SRC_PACKAGES_DIR, OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir(), new ArrayList<>());
        assertFalse(file.exists());
    }

    private boolean comparePackages(File correctSnapshotPackage, File generatedSnapshotPackage) throws IOException {
        TARGZ.decompress(correctSnapshotPackage.getPath(), new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "correct"));
        TARGZ.decompress(generatedSnapshotPackage.getPath(), new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "generated" + "-equal"));
        Path correctPackageDecompressedPath = Path.of(OUTPUT_SNAPSHOT_PACKAGES_DIR + "correct");
        Path generatedPackageDecompressedPath = Path.of(OUTPUT_SNAPSHOT_PACKAGES_DIR + "generated" + "-equal");
        return DirectoryComparator.directoryContentEquals(correctPackageDecompressedPath, generatedPackageDecompressedPath);
    }
}
