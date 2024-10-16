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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;

import java.io.IOException;
import java.util.Collection;

public class NpmPackageLoader {

    public IValidationSupport loadPackagesAndCreatePrePopulatedValidationSupport(FhirContext ctx,
                                                                                 Collection<String> packageFilesToLoad,
                                                                                 String dirPath) throws IOException {
        var result = new ValidationSupportChain();

        for(String p : packageFilesToLoad) {
            CustomNpmPackageValidationSupport npmPackageSupport = new CustomNpmPackageValidationSupport(ctx);
            npmPackageSupport.loadPackageFromPath(dirPath + p);
            result.addValidationSupport(npmPackageSupport);
        }

        return result;
    }
}

