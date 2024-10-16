<img align="right" width="250" height="47" src="docs/img/Gematik_Logo_Flag.png"/> <br/> 

# Release Notes FHIR Snapshots Package Generator

## Release 0.5.0 (2024-10)

### added
- Bumped HAPI to 7.4.3 and other dependencies to the latest versions.

### changed
- snapshot generator doesn't throw DefinitionException if snapshot generation routine produced messages with severity ERROR. Instead, it logs the messages and continues the snapshot generation (some of the messages can be ignored - the decision should be made by user or by calling application).

### fixed
- snapshot generator threw an exception for packages, which do not have any dependencies. The behavior has been fixed (cf. [GitHub Issue #5](https://github.com/gematik/app-fhir-snapshots-package-generator/pull/5)) 

## Release 0.4.0 (2024-04-02)

### added
- Support for [wildcards in package versions](https://confluence.hl7.org/display/FHIR/NPM+Package+Specification#NPMPackageSpecification-Versionreferences)

## Release 0.3.0 (2024-01-25)

### changed
- Removed excludedPackages argument from CLI and the corresponding parameter from Java lib

## Release 0.2.1 (2024-01-24)

### added
- Transfer of the project from https://github.com/gematik/app-referencevalidator
- Separation of CLI and Java lib
