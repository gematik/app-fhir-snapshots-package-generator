<img align="right" width="250" height="47" alt="gematik GmbH" src="docs/img/Gematik_Logo_Flag.png"/> <br/> 

# FHIR Snapshots Package Generator

![GitHub Latest Release)](https://img.shields.io/github/v/release/gematik/app-fhir-snapshots-package-generator?label=release&logo=github) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.gematik.fhir/fhir-snapshots-package-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.gematik.fhir/fhir-snapshots-package-generator) [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
       <ul>
        <li><a href="#release-notes">Release Notes</a></li>
      </ul>
	</li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About The Project

The FHIR Snapshots Package Generator
creates [Snapshots](https://www.hl7.org/fhir/R4/structuredefinition-definitions.html#StructureDefinition.snapshot) for
all [StructureDefinitions](https://www.hl7.org/fhir/R4/structuredefinition.html) in one or
several [FHIR-Packages](https://registry.fhir.org/learn). The result is one or several new FHIR packages with updated
StructureDefinitions. Other Resources get copied without change to the final FHIR-Packages.

> **Warning**
> Package resources in formats other than JSON (i.e. do not end with `.json`) are ignored and are not copied to the
> final FHIR-Package. 

### Release Notes

See [ReleaseNotes.md](./ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started

### Prerequisites

The FHIR Snapshot Package Generator requires Java 21 or higher.

### Installation

Download the latest release of FHIR Snapshot Package Generator
from [Releases](https://github.com/gematik/app-fhir-snapshots-package-generator/releases) and put the downloaded file
`fhir-snapshots-package-generator-cli-X.Y.Z.jar` in a folder of your choice.

To integrate the FHIR Snapshot Package Generator into a Java application use Maven or Gradle and declare a dependency on
```de.gematik.fhir/fhir-snapshots-package-generator-lib``` (
cf. [Maven Central](https://search.maven.org/artifact/de.gematik.fhir.snapshots/fhir-snapshots-package-generator)).

Example for declaration of a Maven dependency:

``` XML
<dependency>
   <groupId>de.gematik.fhir</groupId>
   <artifactId>fhir-snapshots-package-generator-lib</artifactId>
   <version>${version.snapshot-generator}</version>
</dependency>
```   

with `${version.snapshot-generator}` being a placeholder for the version of the FHIR Snapshot Package Generator.

## Usage

To create snapshots of one or several FHIR packages create the following example folder structure, which will be used as
input for the FHIR Snapshot Package Generator:

```
└── src-packages/
    ├── patches/
    │   └── mypackage-1.0.0/
    │       └── myprofile.json
    ├── mypackage-1.0.0.tgz
    ├── mypackage-dependency1-1.0.0.tgz
    └── mypackage-dependency2-1.0.0.tgz
```

The `src-packages` folder should contain all FHIR packages, for which snapshots should be created, and their
dependencies. The source folder can also include so-called `patches`. These are single FHIR resources, which override
the original ones during snapshot generation, e.g. in case of bugs in third-party FHIR packages. For this purpose a
patched resource, e.g. `myprofile.json`,should be placed in a subfolder with exactly the same name as the original
package file name but without the ending, i.e. `mypackage-1.0.0/` for the package `mypackage-1.0.0.tgz` in the example
above.

After processing the src-packages folder the FHIR Snapshot Package Generator creates the following output folder
structure:

```
└── output-folder/
    ├── mypackage-1.0.0.tgz
    ├── mypackage-dependency1-1.0.0.tgz
    └── mypackage-dependency2-1.0.0.tgz
```

All FHIR packages in the output folder contain the patches or original resources from the corresponding packages with
updated snapshot elements for all StructureDefinitions.

### Console application

The FHIR Snapshot Package Generator requires a valid path to a folder with source FHIR packages and a valid path to the
output directory as arguments. The following example shows how to use the FHIR Snapshot Package Generator from the
command line:

```shell
    java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder
```

There are also two optional arguments: `--packages` and `--tempDir`.
With `--packages` you can set a specific list of FHIR packages for which you want to generate the snapshots. If set, the
FHIR Snapshot Package Generator will only generate snapshots for FHIR packages from the folder with source FHIR packages
that are also specified in this list. Package names must be separated with commas.

```shell
java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder --packages=package1-1.0.0.tgz,package2-1.0.0.tgz,package3-1.0.0.tgz
```

With `--tempDir` you can also set a working directory.

```shell
java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder --tempDir=path/to/working-directory
```

Or both:

```shell
java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder --packages=package1-1.0.0.tgz,package2-1.0.0.tgz,package3-1.0.0.tgz --tempDir=path/to/working-directory
```

### Java library

The following example shows how to use the FHIR Snapshot Package Generator as a Java library:

```java
String srcPackageFolderPath = "path/to/src-packages";
String outputFolderPath = "path/to/output-folder";
SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE, "package"), "");
``` 

```java
String srcPackageFolderPath = "path/to/src-packages";
String outputFolderPath = "path/to/output-folder";
String tempDir = "path/to/tempDir";
SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE, "package"),tempDir);
```

### Customizing the execution

With HAPI FHIR 8.8.1, the validation is stricter. An Environment Variable can define the behavior of the library,
allowing to generate the snapshot, even in case of detected failures.

The Environment Variable is `WRITE_ORIGINAL_ON_SNAPSHOT_FAIL` and per default it is set to `true`, to avoid breaking
changes with the previous HAPI FHIR Version being used, 6.6.2.

> [!WARNING]
> While breaking changes from the previous version are being avoided, having the flag set to "true"
> means that all the errors that occur during the snapshot generation will be skipped.

## Contributing

If you want to contribute, please check our [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

Copyright 2024-2026 gematik GmbH

Apache License, Version 2.0

See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License

## Additional Notes and Disclaimer from gematik GmbH

1. Copyright notice: Each published work result is accompanied by an explicit statement of the license conditions for use. These are regularly typical conditions in connection with open source or free software. Programs described/provided/linked here are free software, unless otherwise stated.
2. Permission notice: Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    1. The copyright notice (Item 1) and the permission notice (Item 2) shall be included in all copies or substantial portions of the Software.
    2. The software is provided "as is" without warranty of any kind, either express or implied, including, but not limited to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of or in connection with the software or the use or other dealings with the software, whether in an action of contract, tort, or otherwise.
    3. We take open source license compliance very seriously. We are always striving to achieve compliance at all times and to improve our processes. If you find any issues or have any suggestions or comments, or if you see any other ways in which we can improve, please reach out to: ospo@gematik.de
3. Parts of this software and - in isolated cases - content such as text or images may have been developed using the support of AI tools. They are subject to the same reviews, tests, and security checks as any other contribution. The functionality of the software itself is not based on AI decisions. 

## Contact

To get in touch with us, please open an issue in this GitHub project.