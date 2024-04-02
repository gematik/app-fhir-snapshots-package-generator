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


import lombok.NonNull;

import java.util.Comparator;
import java.util.Objects;

public class PackageVersion implements Comparable<PackageVersion>{
    private final String version;

    public PackageVersion(String version) {
        this.version = version;
    }

    @Override
    public int compareTo(@NonNull PackageVersion other) {
        return Comparator.comparing((PackageVersion v) -> v.version)
                .compare(this, other);
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PackageVersion that = (PackageVersion) obj;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }


    public boolean matchesWildcard(String wildcardVersion) {
        return version.startsWith(wildcardVersion.substring(0, wildcardVersion.length() - 1));
    }
}
