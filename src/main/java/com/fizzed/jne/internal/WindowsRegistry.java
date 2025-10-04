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

    static public Map<String,String> queryUserEnvironmentVariables() throws IOException, InterruptedException {
        // reg query HKEY_CURRENT_USER\Environment
        final String output = Utils.execAndGetOutput(asList("reg.exe", "query", "HKEY_CURRENT_USER\\Environment"));

        return parseEnvironmentVariableRegQueryOutput(output);
    }

    static public Map<String,String> querySystemEnvironmentVariables() throws IOException, InterruptedException {
        // reg query HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment
        final String output = Utils.execAndGetOutput(asList("reg.exe", "query", "HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"));

        return parseEnvironmentVariableRegQueryOutput(output);
    }

    static public Map<String,String> parseEnvironmentVariableRegQueryOutput(String output) throws IOException {
        final Map<String,String> env = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

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
                final String[] parts = line.split("\\s+(REG_\\w+)\\s+");
                if (parts.length == 2) {
                    env.put(parts[0], parts[1]);
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

        return env;
    }

}
