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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SnapshotGeneratorCli {

    @SneakyThrows
    public static void main(String[] args) {
        if(args.length < 2) {
            throw new IllegalArgumentException("Mandatory arguments are missing. (1st mandatory argument: packageFolderPath, 2nd mandatory argument: outputFolderPath)");
        }
        String packageFolderPath = args[0] + File.separator;
        String outputFolderPath = args[1] + File.separator;

        String decompressDir = "";
        if(args.length > 2)
            decompressDir = args[2];

        List<String> excludedPackages = new ArrayList<>();
        if(args.length > 3) {
            excludedPackages = Arrays.stream(args).skip(3).collect(Collectors.toList());
        }


        SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(packageFolderPath, outputFolderPath, decompressDir, excludedPackages);
    }
}
