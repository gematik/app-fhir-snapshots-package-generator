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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoDependenciesIT {

    private static final String OUTPUT_SNAPSHOT_PACKAGES_DIR = "target/generated-snapshots/";
    private static final String DECOMPRESS_DIR = "target/generated-snapshots/";

    private static final SnapshotGenerator snapshotGenerator = new SnapshotGenerator();

    @Test
    void testNoExceptionIfNoDependenciesAreDefined() {
        Assertions.assertDoesNotThrow(() -> snapshotGenerator.generateSnapshots("src/test/resources/no-dependencies/", OUTPUT_SNAPSHOT_PACKAGES_DIR, DECOMPRESS_DIR));
    }

}
