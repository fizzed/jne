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

    public NativeLanguageModel add(String from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.values.put(from, to);
        return this;
    }

    public NativeLanguageModel add(OperatingSystem from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.operatingSystems.put(from, to);
        return this;
    }

    public NativeLanguageModel add(HardwareArchitecture from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.hardwareArchitectures.put(from, to);
        return this;
    }

    public NativeLanguageModel add(ABI from, String to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.abis.put(from, to);
        return this;
    }

    public String format(String key) {
        if (key != null) {
            return this.values.get(key);
        }
        return null;
    }

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
     * Template is a strong where {os} {arch} {abi} is replaced with the model's values.
     *
     * "v17.0.1-{os}-{arch}"
     * "v17.0.1-{os}{_?abi}-{arch}"
     * "v{version}-{os}{_?abi}-{arch}"
     * 
     * @param template
     * @param nativeTarget
     * @return
     */
    public String format(String template, NativeTarget nativeTarget) {
        if (nativeTarget != null) {
            return format(template, nativeTarget.getOperatingSystem(), nativeTarget.getHardwareArchitecture(), nativeTarget.getAbi());
        }
        return null;
    }

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
