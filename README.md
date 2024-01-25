<img align="right" width="250" height="47" src="docs/img/Gematik_Logo_Flag.png"/> <br/> 

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

The FHIR Snapshots Package Generator creates [Snapshots](https://www.hl7.org/fhir/R4/structuredefinition-definitions.html#StructureDefinition.snapshot) for all [StructureDefinitions](https://www.hl7.org/fhir/R4/structuredefinition.html) in one or several [FHIR-Packages](https://registry.fhir.org/learn). The result is one or several new FHIR packages with updated StructureDefinitions. Other Resources get copied without change to the final FHIR-Packages.

> **Warning**
> Package resources in formats other than JSON (i.e. do not end with `.json`) are ignored and are not copied to the final FHIR-Package. The current version of The FHIR Snapshots Package Generator supports FHIR R4-Version of resources only.

### Release Notes
See [ReleaseNotes.md](./ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started

### Prerequisites
The FHIR Snapshot Package Generator requires Java 11 or higher. 

### Installation
Download the latest release of FHIR Snapshot Package Generator from [Releases](https://github.com/gematik/app-fhir-snapshots-package-generator/releases) and put the downloaded file `fhir-snapshots-package-generator-cli-X.Y.Z.jar` in a folder of your choice.

To integrate the FHIR Snapshot Package Generator into a Java application use Maven or Gradle and declare a dependency on ```de.gematik.fhir/fhir-snapshots-package-generator-lib``` (cf. [Maven Central](https://search.maven.org/artifact/de.gematik.fhir.snapshots/fhir-snapshots-package-generator)).

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

To create snapshots of one or several FHIR packages create the following example folder structure, which will be used as input for the FHIR Snapshot Package Generator:

```
└── src-packages/
    ├── patches/
    │   └── mypackage-1.0.0/
    │       └── myprofile.json
    ├── mypackage-1.0.0.tgz
    ├── mypackage-dependency1-1.0.0.tgz
    └── mypackage-dependency2-1.0.0.tgz
```
The `src-packages` folder should contain all FHIR packages, for which snapshots should be created, and their dependencies. The source folder can also include so-called `patches`. These are single FHIR resources, which override the original ones during snapshot generation, e.g. in case of bugs in third-party FHIR packages. For this purpose a patched resource, e.g. `myprofile.json`,should be placed in a subfolder with exactly the same name as the original package file name but without the ending, i.e. `mypackage-1.0.0/` for the package `mypackage-1.0.0.tgz` in the example above.

After processing the src-packages folder the FHIR Snapshot Package Generator creates the following output folder structure:

```
└── output-folder/
    ├── mypackage-1.0.0.tgz
    ├── mypackage-dependency1-1.0.0.tgz
    └── mypackage-dependency2-1.0.0.tgz
```

All FHIR packages in the output folder contain the patches or original resources from the corresponding packages with updated snapshot elements for all StructureDefinitions.

### Console application

The FHIR Snapshot Package Generator requires a valid path to a folder with source FHIR packages and a valid path to the output directory as arguments. The following example shows how to use the FHIR Snapshot Package Generator from the command line:

    java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder

The third argument, which is optional, can set a working directory.

    java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder path/to/working-directory

### Java library

The following example shows how to use the FHIR Snapshot Package Generator as a Java library:

``` Java
        String srcPackageFolderPath = "path/to/src-packages";
        String outputFolderPath = "path/to/output-folder";
        SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE, "package"), "");
``` 

``` Java
        String srcPackageFolderPath = "path/to/src-packages";
        String outputFolderPath = "path/to/output-folder";
        String workingDirectory = "path/to/working-directory";
        SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE, "package"), workingDirectory);
``` 

## Contributing
If you want to contribute, please check our [CONTRIBUTING.md](./CONTRIBUTING.md).

## License
Check [Apache License, Version 2.0](LICENSE)

## Contact
To get in touch with us, please open an issue in this GitHub project.