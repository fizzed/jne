package com.fizzed.jne.internal;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2025 Fizzed, Inc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Arrays.asList;

public class WindowsRegistry {
    static private final Logger log = LoggerFactory.getLogger(WindowsRegistry.class);

    private final Map<String,String> values;

    public WindowsRegistry(Map<String,String> values) {
        this.values = values;
    }

    public Map<String,String> getValues() {
        return this.values;
    }

    public String get(String key) {
        return this.values.get(key);
    }

    // helper methods

    static public WindowsRegistry queryUserEnvironmentVariables(SystemExecutor systemExecutor) throws Exception {
        return query(systemExecutor, "HKEY_CURRENT_USER\\Environment");
    }

    static public WindowsRegistry querySystemEnvironmentVariables(SystemExecutor systemExecutor) throws Exception {
        return query(systemExecutor, "HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment");
    }

    static public WindowsRegistry queryCurrentVersion(SystemExecutor systemExecutor) throws Exception {
        return query(systemExecutor, "HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
    }

    static public WindowsRegistry queryComputerName(SystemExecutor systemExecutor) throws Exception {
        return query(systemExecutor, "HKLM\\SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ComputerName");
    }

    static public WindowsRegistry query(SystemExecutor systemExecutor, String key) throws Exception {
        final String output = systemExecutor.execProcess(asList(0), "reg.exe", "query", "\"" + key + "\"");
        return parse(output);
    }

    static public WindowsRegistry parse(String output) throws IOException {
        final Map<String,String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // process output line by line
        int pos = 0;
        while (pos < output.length()) {
            // we want to read the next line
            int nextNewlinePos = output.indexOf('\n', pos);
            String line = output.substring(pos, nextNewlinePos >= 0 ? nextNewlinePos : output.length());
            pos += line.length() + 1;

            // trim it to make it easier to parse
            line = line.trim();

            // it may only be whitespace, which we can skip
            if (line.isEmpty()) {
                // safely skip it
            } else if (line.startsWith("HKEY_")) {
                // we can skip this line
            } else if (line.contains("REG_")) {
                // this is a line with a value we will want to process
                // e.g. Path    REG_EXPAND_SZ    C:\Opt\bin;%PATH%
                int regStartPos = line.indexOf("REG_");
                if (regStartPos >= 0) {
                    int regEndPos = Utils.indexOfAny(line, regStartPos+1, ' ', '\t', '\r', '\n');   // any whitespace works
                    // correct endPos in case REG_ part IS the last part of the line
                    regEndPos = regEndPos >= 0 ? regEndPos : line.length();

                    final String name = line.substring(0, regStartPos).trim();
                    final String type = line.substring(regStartPos, regEndPos).trim();

                    // the value may or may not exist
                    final String value;
                    if (regEndPos < line.length()-1) {
                        value = parseType(type, line.substring(regEndPos+1).trim());
                    } else {
                        value = null;
                    }

                    values.put(name, value);
                } else {
                    log.warn("Unable to parse reg query output line: {}", line);
                }
            } else {
                // hmmm... this should really never happen
                log.warn("Unexpected windows req query line: {}", line);
            }
        }

        return new WindowsRegistry(values);
    }

    static public String parseType(String type, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        if ("REG_DWORD".equalsIgnoreCase(type)) {
            String hex = value.substring(2);
            // 2. Parse the hex string into an integer
            int dwordValue = Integer.parseInt(hex, 16); // 16 = Radix 16 (hex)
            // 3. Convert the integer to a final decimal string
            return Integer.toString(dwordValue);
        }

        return value;
    }

}
