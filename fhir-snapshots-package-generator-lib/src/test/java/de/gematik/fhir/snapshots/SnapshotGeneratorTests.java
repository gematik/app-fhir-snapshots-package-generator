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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnapshotGeneratorTests {

    private static final String CORRECT_PACKAGE = "src/test/resources/package/minimal.example-1.0.0-correct.tgz";
    private static final String OUTPUT_SNAPSHOT_PACKAGES_DIR = "target/generated-snapshots/";
    private static final String SRC_PACKAGES_DIR = "src/test/resources/src-package/";
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
        snapshotGenerator.generateSnapshots(SRC_PACKAGES_DIR, OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir());
        File generatedSnapshotPackage = new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "minimal.example-1.0.0.tgz");
        assertThat(comparePackages(correctSnapshotPackage, generatedSnapshotPackage)).isTrue();
    }

    @Test
    @SneakyThrows
    void testNoExceptionOnDependenciesWithUpperCase() {
        var snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots("src/test/resources/src-package-upper-case/", OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir());
        File generatedSnapshotPackage = new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "dependencies-in-upper-case-1.0.0.tgz");
        assertThat(generatedSnapshotPackage).exists();
    }

    @Test
    @SneakyThrows
    void testWildcardDependencyWithNoMatchingPackageThrowsException() {
        var snapshotGenerator = new SnapshotGenerator();
        String decompressDir = getDecompressDir();

        assertThatThrownBy(() -> snapshotGenerator.generateSnapshots("src/test/resources/src-package-wildcard-error/", OUTPUT_SNAPSHOT_PACKAGES_DIR, decompressDir))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wildcard version '1.1.x' has been found for package name 'wildcard.package'. But no matching packages were found in 'src/test/resources/src-package-wildcard-error/'");
    }

    @Test
    @SneakyThrows
    void testCleanUpDirectoryWorksBeforeSnapshotGeneration() {
        String path = Paths.get(Objects.requireNonNull(SnapshotGenerator.class.getResource("/")).toURI()).getParent().toString() + "/decompressed-packages/minimal.example-1.0.0/";
        File file = new File(path + "test" + File.separator + "somefile.txt");
        if(!file.exists()) {
            FileUtils.createParentDirectories(file);
            if(!file.createNewFile())
                throw new IOException(String.format("Could not create test file %s", file.getPath()));
        }
        snapshotGenerator.generateSnapshots(SRC_PACKAGES_DIR, OUTPUT_SNAPSHOT_PACKAGES_DIR, getDecompressDir());
        assertThat(file).doesNotExist();
    }

    private boolean comparePackages(File correctSnapshotPackage, File generatedSnapshotPackage) throws IOException {
        TARGZ.decompress(correctSnapshotPackage.getPath(), new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "correct"));
        TARGZ.decompress(generatedSnapshotPackage.getPath(), new File(OUTPUT_SNAPSHOT_PACKAGES_DIR + "generated" + "-equal"));
        Path correctPackageDecompressedPath = Path.of(OUTPUT_SNAPSHOT_PACKAGES_DIR + "correct");
        Path generatedPackageDecompressedPath = Path.of(OUTPUT_SNAPSHOT_PACKAGES_DIR + "generated" + "-equal");
        return DirectoryComparator.directoryContentEquals(correctPackageDecompressedPath, generatedPackageDecompressedPath);
    }
}
