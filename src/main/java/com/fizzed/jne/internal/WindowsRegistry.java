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
        final String output = systemExecutor.execProcess("reg.exe", "query", "HKEY_CURRENT_USER\\Environment");

        return parse(output);
    }

    static public WindowsRegistry querySystemEnvironmentVariables(SystemExecutor systemExecutor) throws Exception {
        final String output = systemExecutor.execProcess("reg.exe", "query", "\"HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment\"");

        return parse(output);
    }

    static public WindowsRegistry queryCurrentVersion(SystemExecutor systemExecutor) throws Exception {
        final String output = systemExecutor.execProcess("reg.exe", "query", "\"HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\"");

        return parse(output);
    }

    static public WindowsRegistry queryComputerName(SystemExecutor systemExecutor) throws Exception {
        final String output = systemExecutor.execProcess("reg.exe", "query", "\"HKLM\\SYSTEM\\CurrentControlSet\\Control\\ComputerName\\ComputerName\"");

        return parse(output);
    }

    static public WindowsRegistry parse(String output) throws IOException {
        final Map<String,String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // process output line by line
        int pos = 0;
        int nextNewlinePos = output.indexOf('\n', pos);
        while (nextNewlinePos > 0) {
            String line = output.substring(pos, nextNewlinePos);
            // trim it to make it easier to parse
            line = line.trim();

//            log.debug("parsing environment variable line: {}", line);

            // it may only be whitespace, which we can skip
            if (line.isEmpty()) {
                // safely skip it
            } else if (line.startsWith("HKEY_CURRENT_USER\\") || line.startsWith("HKEY_LOCAL_MACHINE\\")) {
                // we can skip this line
            } else if (line.contains("REG_")) {
                // this is a line with a value we will want to process
                // e.g. Path    REG_EXPAND_SZ    C:\Opt\bin;%PATH%
                final String[] parts = line.split("\\s+(REG_[_\\w]+)\\s+");
                if (parts.length == 2) {
                    final String name =  parts[0].trim();
                    final String type = line.substring(parts[0].length(), line.length() - parts[1].length()).trim();
                    final String value = parseType(type, parts[1]);
                    values.put(name, value);
                } else if (parts.length == 1) {
                    values.put(parts[0], null);     // empty value
                } else {
                    log.warn("Unable to parse reg query output line: {}", line);
                }
//                log.debug("part0: {}", parts[0]);
//                log.debug("part1: {}", parts[1]);
            } else {
                // hmmm... this should really never happen
                log.warn("Unexpected windows req query line: {}", line);
            }

            pos = nextNewlinePos + 1;
            nextNewlinePos = output.indexOf('\n', pos);
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
