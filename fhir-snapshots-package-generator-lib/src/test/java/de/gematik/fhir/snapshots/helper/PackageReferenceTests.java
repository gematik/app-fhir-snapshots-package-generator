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
package de.gematik.fhir.snapshots.helper;

import de.gematik.fhir.snapshots.PackageReference;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageReferenceTests {
    @Test
    void testGetWildcardPackageFilename() {
        PackageReference wildcardPackageReference = new PackageReference("wildcard.example", "1.1.x");
        String expectedFilename = "wildcard.example-1.1.2.tgz";
        String actualFilename = wildcardPackageReference.getWildcardPackageFilename("src/test/resources/src-package-wildcard");

        assertThat(actualFilename).isEqualTo(expectedFilename);
    }
}
