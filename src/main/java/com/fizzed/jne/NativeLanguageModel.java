package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2024 Fizzed, Inc
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The NativeLanguageModel class provides a mechanism for mapping and formatting
 * data related to operating systems, hardware architectures, and ABIs using
 * customizable key-value mappings. It allows dynamic transformation of templates
 * to generate strings with context-sensitive replacements based on the provided
 * inputs.
 */
public class NativeLanguageModel {

    private final Map<String,String> values;
    private final Map<OperatingSystem,String> operatingSystems;
    private final Map<HardwareArchitecture,String> hardwareArchitectures;
    private final Map<ABI,String> abis;

    public NativeLanguageModel() {
        this.values = new HashMap<>();
        this.operatingSystems = new HashMap<>();
        this.hardwareArchitectures = new HashMap<>();
        this.abis = new HashMap<>();
    }

    /**
     * Adds a mapping between the provided {@code from} string and {@code to} string into the model.
     * The mapping is stored in an internal map, where {@code from} acts as the key and {@code to} as the value.
     * Both {@code from} and {@code to} must be non-null; if either is null, a {@link NullPointerException} is thrown.
     *
     * Example usage:
     *
     * <pre>
     * NativeLanguageModel model = new NativeLanguageModel();
     * model.add("en", "English").add("es", "Spanish");
     * </pre>
     *
     * After adding the mappings "en" -> "English" and "es" -> "Spanish",
     * the {@code values} map in the model will contain these entries.
     *
     * @param from the source string that acts as the key in the mapping (must not be null)
     * @param to the target string that acts as the value in the mapping (must not be null)
     * @return the current instance of {@code NativeLanguageModel} to allow method chaining
     * @throws NullPointerException if {@code from} or {@code to} is null
     */
    public NativeLanguageModel add(String from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.values.put(from, to);
        return this;
    }

    /**
     * Adds a mapping between the provided {@code from} operating system and {@code to} string
     * into the model. The mapping is stored in an internal map, where {@code from} acts as the key
     * and {@code to} as the value. Both {@code from} and {@code to} must be non-null; if either
     * is null, a {@link NullPointerException} is thrown.
     *
     * Example usage:
     *
     * NativeLanguageModel model = new NativeLanguageModel();
     * model.add(OperatingSystem.LINUX, "Linux-based").add(OperatingSystem.WINDOWS, "Windows-based");
     *
     * After adding the mappings, the operating systems map in the model will contain:
     * - LINUX -> "Linux-based"
     * - WINDOWS -> "Windows-based"
     *
     * @param from the operating system to be used as the key in the mapping (must not be null)
     * @param to the string value to be associated with the operating system key (must not be null)
     * @return the current instance of {@code NativeLanguageModel} to allow method chaining
     * @throws NullPointerException if {@code from} or {@code to} is null
     */
    public NativeLanguageModel add(OperatingSystem from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.operatingSystems.put(from, to);
        return this;
    }

    /**
     * Adds a mapping between the provided {@code from} hardware architecture and {@code to} string
     * into the model. The mapping is stored in an internal map, where {@code from} acts as the key
     * and {@code to} as the value. Both {@code from} and {@code to} must be non-null; if either
     * is null, a {@link NullPointerException} is thrown.
     *
     * Example usage:
     *
     * NativeLanguageModel model = new NativeLanguageModel();
     * model.add(HardwareArchitecture.X86_64, "64-bit architecture")
     *      .add(HardwareArchitecture.ARM, "ARM architecture");
     *
     * After adding the mappings, the hardware architectures map in the model will contain:
     * - X86_64 -> "64-bit architecture"
     * - ARM -> "ARM architecture"
     *
     * @param from the hardware architecture to be used as the key in the mapping (must not be null)
     * @param to the string value to be associated with the hardware architecture key (must not be null)
     * @return the current instance of {@code NativeLanguageModel} to allow method chaining
     * @throws NullPointerException if {@code from} or {@code to} is null
     */
    public NativeLanguageModel add(HardwareArchitecture from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.hardwareArchitectures.put(from, to);
        return this;
    }

    /**
     * Adds a mapping between the provided {@code from} ABI and {@code to} string into the model.
     * The mapping is stored in an internal map, where {@code from} acts as the key and {@code to} as the value.
     * Both {@code from} and {@code to} must be non-null; if either is null, a {@link NullPointerException} is thrown.
     * This method allows method chaining by returning the current {@code NativeLanguageModel} instance.
     *
     * Example usage:
     * NativeLanguageModel model = new NativeLanguageModel();
     * model.add(ABI.MUSL, "Musl-based ABI").add(ABI.GNU, "GNU-based ABI");
     *
     * After adding the mappings, the internal map will contain entries like:
     * - MUSL -> "Musl-based ABI"
     * - GNU -> "GNU-based ABI"
     *
     * @param from the ABI (Application Binary Interface) used as the key in the mapping (must not be null)
     * @param to the string value to be associated with the provided ABI (must not be null)
     * @return the current instance of {@code NativeLanguageModel} to allow method chaining
     * @throws NullPointerException if {@code from} or {@code to} is null
     */
    public NativeLanguageModel add(ABI from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.abis.put(from, to);
        return this;
    }

    /**
     * Retrieves and formats the string value associated with the provided key.
     * If the key exists in the internal collection, its corresponding value is returned.
     * If the key is null or does not exist, null is returned.
     *
     * @param key the key whose associated string value needs to be retrieved
     * @return the string value associated with the given key, or null if the key is null or not found
     */
    public String format(String key) {
        if (key != null) {
            return this.values.get(key);
        }
        return null;
    }

    /**
     * Formats the provided operating system into its corresponding string representation.
     * If the operating system has a predefined mapping, it returns the mapped value.
     * Otherwise, it returns the lowercase name of the operating system.
     * Returns null if the input is null.
     *
     * @param os the operating system to format
     * @return the formatted string representation of the operating system, or null if the input is null
     */
    public String format(OperatingSystem os) {
        if (os != null) {
            String v = this.operatingSystems.get(os);
            if (v == null) {
                return os.name().toLowerCase();
            }
            return v;
        }
        return null;
    }

    /**
     * Formats the provided hardware architecture into its corresponding string representation.
     * If a specific mapping is found for the given hardware architecture, it is returned.
     * Otherwise, the architecture's name is converted to lowercase and returned.
     * If the input architecture is null, the method returns null.
     *
     * Example usage:
     * - If the input `arch` maps to a value in `hardwareArchitectures`, the mapped value is returned.
     * - If the input `arch` does not map to a value, the lowercase name of `arch` is returned.
     * - If `arch` is null, the method returns null.
     *
     * @param arch the hardware architecture to format, can be null
     * @return the formatted string representation of the hardware architecture, or null if the input is null
     */
    public String format(HardwareArchitecture arch) {
        if (arch != null) {
            String v = this.hardwareArchitectures.get(arch);
            if (v == null) {
                return arch.name().toLowerCase();
            }
            return v;
        }
        return null;
    }

    /**
     * Formats the provided ABI object into its corresponding string representation.
     * If the `abi` parameter is found in the `abis` mapping, the associated value is returned.
     * Otherwise, the `name()` of the `abi` is converted to lowercase and returned.
     * Returns null if the provided `abi` parameter is null.
     *
     * Example usage:
     * - If `abis` contains a mapping for the provided ABI object, this method returns the mapped value.
     * - If the `abis` map does not contain a mapping for the provided ABI, the lowercase name of the ABI is returned.
     * - If the `abi` is null, the method returns null.
     *
     * @param abi the ABI object to format; can be null
     * @return the formatted string representation of the ABI or null if the ABI is null
     */
    public String format(ABI abi) {
        if (abi != null) {
            String v = this.abis.get(abi);
            if (v == null) {
                return abi.name().toLowerCase();
            }
            return v;
        }
        return null;
    }

    /**
     * Formats a string template based on the provided {@code NativeTarget} parameter.
     * This method substitutes placeholders in the given template with the corresponding
     * attributes (operating system, hardware architecture, and ABI) from the provided
     * {@code NativeTarget}. If the {@code NativeTarget} is {@code null}, the method
     * returns {@code null}.
     *
     * @param template the template string containing placeholders to be substituted.
     * @param nativeTarget the {@code NativeTarget} object containing the operating system,
     *                     hardware architecture, and ABI information used for formatting.
     *                     If {@code null}, no substitutions occur, and {@code null} is returned.
     * @return a formatted string with placeholders in the template replaced by attributes
     *         from the {@code NativeTarget}, or {@code null} if {@code nativeTarget} is {@code null}.
     *
     * Example usage:
     * Given a template such as:
     *   "OS: {os}, Architecture: {arch}, ABI: {abi}"
     * If the {@code nativeTarget} has:
     *   Operating System: "Linux", Architecture: "x86_64", ABI: "gnu",
     * then the method would return:
     *   "OS: Linux, Architecture: x86_64, ABI: gnu"
     */
    public String format(String template, NativeTarget nativeTarget) {
        if (nativeTarget != null) {
            return format(template, nativeTarget.getOperatingSystem(), nativeTarget.getHardwareArchitecture(), nativeTarget.getAbi());
        }
        return null;
    }

    /**
     * Formats the given template string by replacing placeholders with the corresponding values
     * from the provided operating system, hardware architecture, and ABI.
     * Placeholders in the template string must be enclosed in curly braces, e.g., {os}, {arch}, {abi}.
     * An optional prefix can also be included before the placeholder using a question mark, e.g., ?{os}.
     *
     * Supported placeholders:
     * - {os}: Replaced with the string representation of the provided operating system.
     * - {arch}: Replaced with the string representation of the provided hardware architecture.
     * - {abi}: Replaced with the string representation of the provided ABI.
     *
     * If a placeholder is not recognized, an IllegalArgumentException is thrown.
     *
     * @param template the template string containing placeholders that need substitution.
     * @param os the operating system to be substituted into the {os} placeholder.
     * @param arch the hardware architecture to be substituted into the {arch} placeholder.
     * @param abi the ABI (Application Binary Interface) to be substituted into the {abi} placeholder.
     * @return the formatted string where placeholders in the template are replaced with the corresponding values.
     *         If no placeholders exist, the method returns the original template unmodified.
     *
     * Example usage:
     * Given:
     *   template = "Operating System: {os}, Architecture: {arch}, ABI: {abi}"
     *   os = OperatingSystem.LINUX, arch = HardwareArchitecture.X86_64, abi = ABI.GNU
     * The method would return:
     *   "Operating System: Linux, Architecture: x86_64, ABI: gnu"
     */
    public String format(String template, OperatingSystem os, HardwareArchitecture arch, ABI abi) {
        // loop thru template where { marks the start of a replacement
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        String optFront = null;
        String key = null;
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            if (!started) {
                if (c == '{') {
                    started = true;
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '}') {
                    started = false;
                    String v = null;
                    // what was the key that was provided, we'll look it up
                    if ("os".equals(key)) {
                        v = this.format(os);
                    } else if ("arch".equals(key)) {
                        v = this.format(arch);
                    } else if ("abi".equals(key)) {
                        v = this.format(abi);
                    } else {
                        v = this.format(key);
                        if (v == null) {
                            throw new IllegalArgumentException("Unknown key: " + key + " in template: " + template);
                        }
                    }
                    if (v != null) {
                        if (optFront != null) {
                            sb.append(optFront);
                        }
                        sb.append(v);
                    }
                    optFront = null;
                    key = null;
                } else {
                    if (c == '?') {
                        optFront = key;
                        key = null;
                    } else {
                        // we will assume this is part of the key, unless we get a ?
                        key = key != null ? key + c : "" + c;
                    }
                }
            }
        }

        return sb.toString();
    }

}
