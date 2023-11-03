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

    ZULU("Zulu", "Azul Systems", "https://www.azul.com/downloads", new String[] { "zulu", "azul" }),
    LIBERICA("Liberica", "BellSoft", "https://bell-sw.com/libericajdk", new String[] { "liberica", "bellsoft" }),
    TEMURIN("Temurin", "Eclipse", "https://adoptium.net", new String[] { "temurin", "eclipse", "adoptium" }),
    CORRETTO("Corretto", "Amazon", "https://docs.aws.amazon.com/corretto", new String[] { "corretto", "amazon" }),
    MICROSOFT("Microsoft", "Microsoft", "https://learn.microsoft.com/en-us/java/openjdk", new String[] { "microsoft" }),
    SEMERU("Semeru", "IBM", "https://developer.ibm.com/languages/java/semeru-runtimes", new String[] { "ibm", "semeru" }),
    ORACLE("Oracle", "Oracle", "https://www.oracle.com/java/technologies/downloads", new String[] { "oracle" }),
    DRAGONWELL("DragonWell", "Alibaba", "https://dragonwell-jdk.io", new String[] { "alibaba", "dragonwell" }),
    NITRO("Nitro", "Fizzed", "https://github.com/fizzed/nitro", new String[] { "fizzed", "nitro" }),
    JBR("JBR", "JetBrains", "https://github.com/JetBrains/JetBrainsRuntime", new String[] { "jetbrains" }),
    SAPMACHINE("SapMachine", "SAP", "https://sap.github.io/SapMachine", new String[] { "sap", "sapmachine" }),

    // Tencent KONA
    // Trava

    //
    // these are provided by many package managers / os distros
    //
    REDHAT("OpenJDK", "RedHat", null, new String[] { "redhat" }),
    DEBIAN("OpenJDK", "Debian", null, new String[] { "debian" }),
    UBUNTU("OpenJDK", "Ubuntu", null, new String[] { "ubuntu" }),
    HOMEBREW("OpenJDK", "HomeBrew", null, new String[] { "homebrew" }),
    ;

    private final String descriptor;
    private final String vendor;
    private final String distroUrl;
    private final String[] keywords;

    JavaDistribution(String descriptor, String vendor, String distroUrl, String[] keywords) {
        this.descriptor = descriptor;
        this.vendor = vendor;
        this.distroUrl = distroUrl;
        this.keywords = keywords;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getVendor() {
        return vendor;
    }

    public String getDistroUrl() {
        return distroUrl;
    }

    public String[] getKeywords() {
        return keywords;
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