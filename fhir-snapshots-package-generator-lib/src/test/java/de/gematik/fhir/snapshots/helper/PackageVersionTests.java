package de.gematik.fhir.snapshots.helper;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PackageVersionTests {

  @Test
  void testEquals() {
    PackageVersion version1 = new PackageVersion("1.0.0");
    PackageVersion version2 = new PackageVersion("1.0.0");
    PackageVersion version3 = new PackageVersion("2.0.0");

    assertThat(version1).isEqualTo(version2).isNotEqualTo(version3);
  }

  @Test
  void testHashCode() {
    PackageVersion version1 = new PackageVersion("1.0.0");
    PackageVersion version2 = new PackageVersion("1.0.0");
    PackageVersion version3 = new PackageVersion("2.0.0");

    assertThat(version1.hashCode()).hasSameHashCodeAs(version2.hashCode());
    assertThat(version1.hashCode()).isNotEqualTo(version3.hashCode());
  }

  @Test
  void testCompareTo() {
    PackageVersion version1 = new PackageVersion("1.0.0");
    PackageVersion version2 = new PackageVersion("1.0.0");
    PackageVersion version3 = new PackageVersion("2.0.0");

    assertThat(version1).isEqualByComparingTo(version2).isLessThan(version3);
    assertThat(version3).isGreaterThan(version1);
  }

  @Test
  void testToString() {
    PackageVersion version = new PackageVersion("1.0.0");
    assertThat(version.toString()).hasToString("1.0.0");
  }

  @Test
  void testMatchesWildcard() {
    PackageVersion version = new PackageVersion("1.0.0");
    assertThat(version.matchesWildcard("1.0.x")).isTrue();
    assertThat(version.matchesWildcard("2.0.x")).isFalse();
  }
}
