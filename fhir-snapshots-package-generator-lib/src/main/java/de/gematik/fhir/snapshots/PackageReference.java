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

import de.gematik.fhir.snapshots.helper.PackageVersion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

@Data
@AllArgsConstructor
public class PackageReference {
    private String packageName;
    private String packageVersion;

    public String getWildcardPackageFilename(String packageFolderPath) {

        File folder = new File(packageFolderPath);
        File[] files = folder.listFiles((dir, name) -> name.matches(packageName + "-\\d+\\.\\d+\\.\\d+\\.tgz"));

        String highestVersionFilename = "";
        PackageVersion highestVersion = null;

        if(files != null) {
            for (File file : files) {
                String filename = file.getName();
                String versionString = filename.substring(packageName.length() + 1, filename.length() - 4);
                PackageVersion version = new PackageVersion(versionString);

                if (version.matchesWildcard(packageVersion) && (highestVersion == null || version.compareTo(highestVersion) > 0)) {
                    highestVersion = version;
                    highestVersionFilename = filename;
                }
            }
        }

        return highestVersionFilename;
    }
}
