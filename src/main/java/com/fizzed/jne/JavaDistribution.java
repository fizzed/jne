package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Generally follows this: https://github.com/actions/setup-java#supported-distributions
 */
public enum JavaDistribution {

    ZULU("Azul Systems", "https://www.azul.com/downloads", new String[] { "zulu", "azul" }),
    LIBERICA("BellSoft", "https://bell-sw.com/libericajdk", new String[] { "liberica", "bellsoft" }),
    TEMURIN("Eclipse", "https://adoptium.net", new String[] { "temurin", "eclipse", "adoptium" }),
    CORRETTO("Amazon", "https://docs.aws.amazon.com/corretto", new String[] { "corretto", "amazon" }),
    MICROSOFT("Microsoft", "https://learn.microsoft.com/en-us/java/openjdk", new String[] { "microsoft" }),
    SEMERU("IBM", "https://developer.ibm.com/languages/java/semeru-runtimes", new String[] { "ibm", "semeru" }),
    ORACLE("Oracle", "https://www.oracle.com/java/technologies/downloads", new String[] { "oracle" }),
    DRAGONWELL("Alibaba", "https://dragonwell-jdk.io", new String[] { "alibaba", "dragonwell" }),
    NITRO("Fizzed", "https://github.com/fizzed/nitro", new String[] { "fizzed", "nitro" }),
    JETBRAINS("JetBrains", "https://github.com/JetBrains/JetBrainsRuntime", new String[] { "jetbrains" }),
    SAPMACHINE("SAP", "https://sap.github.io/SapMachine", new String[] { "sap", "sapmachine" }),
    //
    // these are provided by many package managers / os distros
    //
    REDHAT("RedHat", null, new String[] { "redhat" }),
    DEBIAN("Debian", null, new String[] { "debian" }),
    UBUNTU("Ubuntu", null, new String[] { "ubuntu" }),
    HOMEBREW("HomeBrew", null, new String[] { "homebrew" }),
    ;

    private final String vendor;
    private final String distroUrl;
    private final String[] keywords;

    JavaDistribution(String vendor, String distroUrl, String[] keywords) {
        this.vendor = vendor;
        this.distroUrl = distroUrl;
        this.keywords = keywords;
    }

    public String getVendor() {
        return vendor;
    }

    public String getDistroUrl() {
        return distroUrl;
    }

    static public JavaDistribution resolve(String value) {
        if (value == null) {
            return null;
        }

        // normalize value to search for
        value = value.toLowerCase();

        for (JavaDistribution distro : JavaDistribution.values()) {
            if (distro.keywords != null) {
                for (String keyword : distro.keywords) {
                    if (value.contains(keyword)) {
                        return distro;
                    }
                }
            }
        }

        return null;
    }

}