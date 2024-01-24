![gematik GmbH](docs/img/Gematik_Logo_Flag.png)

# FHIR Snapshots Package Generator

![GitHub Latest Release)](https://img.shields.io/github/v/release/gematik/app-fhir-snapshots-package-generator?label=release&logo=github) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.gematik.fhir/fhir-snapshots-package-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.gematik.refv/referencevalidator) [![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

## Über das Projekt

Der FHIR Snapshots Package Generator erzeugt [Snapshots](https://www.hl7.org/fhir/R4/structuredefinition-definitions.html#StructureDefinition.snapshot) für alle [StructureDefinitions](https://www.hl7.org/fhir/R4/structuredefinition.html) eines oder mehreren [FHIR-Packages](https://registry.fhir.org/learn). Das Ergebnis ist ein oder mehrere neue FHIR-Packages mit den aktualisierten StructureDefinitions. Restliche Ressourcen werden ohne Verwendung in die finalen FHIR-Packages übernommen.  

> **Warning**
> Der FHIR Snapshots Package Generator ignoriert aktuell alle Einträge aus den .tgz Quell-FHIR-Packages, die keine json-Dateien sind (mit `.json` enden). Des Weiteren wird derzeit nur die FHIR R4-Version unterstützt.  

## Verwendung

### Konsolenanwendung

Für die Verwendung der Konsolenanwendung soll die Datei `fhir-snapshots-package-generator-cli-X.Y.Z.jar` in einem beliebigen Ordner im Dateisystem abgelegt werden (siehe [Releases](https://github.com/gematik/app-fhir-snapshots-package-generator/releases)).

Der SnapshotGenerator erfordert als Eingabe einen gültigen Pfad zu einem Ordner mit Quell-FHIR-Packages und einen gültigen Pfad zum Ausgabeverzeichnis, in dem die fertigen FHIR-Packages abgelegt werden, die dann die generierten Snapshots enthalten.

    java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder

Optional kann als dritter Übergabeparameter noch ein gültiger Pfad angegeben werden, den der SnapshotGenerator als temporäres Arbeitsverzeichnis verwendet. Zudem kann auch noch ein viertes Argument mitgegeben werden - eine mit Leerzeichen separierte Liste von FHIR-Package Namen, die nur für die Snapshot-Generierung von bestimmten Quell-FHIR-Packages als Abhängigkeiten benötigt werden, für die aber keine Snapshots generiert werden sollen.

    java -jar fhir-snapshots-package-generator-cli-X.Y.Z.jar path/to/src-packages path/to/output-folder path/to/working-directory snapshot-dependency-package1.tgz snapshot-dependency-package2.tgz

Im Ordner mit den Quell-FHIR-Packages können außerdem Patches abgelegt werden. Hier können Fehler in Quell-FHIR-Packages behoben werden.
Im folgenden Beispiel wird die Ordnerstruktur beschrieben, die notwendig wäre, um dem Quell-FHIR-Package `mypackage-1.0.0.tgz` Patches hinzuzufügen:

```
└── src-packages/
    ├── patches/
    │   └── mypackage-1.0.0/
    │       └── myprofile.json
    ├── mypackage-1.0.0.tgz
    ├── mypackage-dependency1-1.0.0.tgz
    └── mypackage-dependency2-1.0.0.tgz
```

Hierbei ist es notwendig, dass der Patch `myprofile.json` im Unterordner `mypackage-1.0.0/` exakt den selben Dateinamen hat, wie das zu patchende Profile, das im Quell-FHIR-Package `mypackage-1.0.0.tgz` enthalten ist.

```
└── output-folder/
    ├── mypackage-1.0.0.tgz
    ├── mypackage-dependency1-1.0.0.tgz
    └── mypackage-dependency2-1.0.0.tgz
```

Der SnapshotGenerator legt dann im Ausgabeverzeichnis `output-folder` für jedes Quell-FHIR-Package ein FHIR-Package mit den dazugehörigen generierten Snapshots und darin integrierten Patches ab.

### Nutzung mit der Java-Bibliothek

Der SnapshotGenerator wird zur Einbindung in andere Projekte auf [Maven Central](https://search.maven.org/artifact/de.gematik.fhir.snapshots/fhir-snapshots-package-generator) veröffentlicht.

Beispiel zur Einbindung des SnapshotGenerator:

``` XML
    <dependency>
        <groupId>de.gematik.fhir</groupId>
        <artifactId>fhir-snapshots-package-generator-lib</artifactId>
        <version>${version.snapshot-generator}</version>
    </dependency>
```   

Die Versionsangabe `${version.snapshot-generator}` soll mit der gewünschten einzubindenden SnapshotGenerator-Version ersetzt werden.

Folgende Beispiele veranschaulichen die Verwendung vom SnapshotGenerator in einer Java-Anwendung.

``` Java
        String srcPackageFolderPath = "path/to/src-packages";
        String outputFolderPath = "path/to/output-folder";
        SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE, "package"), "", new ArrayList<>());
``` 

``` Java
        String srcPackageFolderPath = "path/to/src-packages";
        String outputFolderPath = "path/to/output-folder";
        String workingDirectory = "path/to/working-directory";
        List<String> excludedPackages = List.of("excluded-fhir-package-1.tgz", "excluded-fhir-package-2.tgz", "excluded-fhir-package-3.tgz");
        SnapshotGenerator snapshotGenerator = new SnapshotGenerator();
        snapshotGenerator.generateSnapshots(packageFolderPath, packageFolderPath.replace(SRC_PACKAGE, "package"), workingDirectory, excludedPackages);
``` 
## Lizenz

Siehe [Apache License, Version 2.0](LICENSE)