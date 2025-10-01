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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Utils {

    static public String trimToNull(String value) {
        if (value != null) {
            value = value.trim();
            if (value.equals("")) {
                return null;
            }
        }
        return value;
    }

    static public Path which(String nameOfExeOrBin) {
        // search PATH for existence of an executable
        final String path = System.getenv("PATH");
        if (path != null) {
            final String[] paths = path.split(File.pathSeparator);
            for (String p : paths) {
                Path pathDir = Paths.get(p);
                Path exeFile = pathDir.resolve(nameOfExeOrBin);
                if (Files.exists(exeFile)) {
                    return exeFile;
                }
            }
        }
        return null;
    }

    static public String execAndGetOutput(List<String> commands) throws IOException, InterruptedException {
        // Create a ProcessBuilder instance
        final ProcessBuilder processBuilder = new ProcessBuilder(commands.toArray(new String[0]));

        // Start the process
        final Process process = processBuilder.start();

        // Read the output from the process's input stream (standard output)
        final StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        // Read the error from the process's input stream (standard error)
        //final StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //output.append(line).append(System.lineSeparator());
                System.err.println(line);
            }
        }

        // Wait for the process to complete and get its exit code
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Process exited with a non-zero code (actual " + exitCode + ")");
        }

        return output.toString();
    }

    static public boolean searchEnvVar(String name, String value) {
        return searchEnvVar(System.getenv(), name, value);
    }

    static public boolean searchEnvVar(Map<String,String> env, String name, String value) {
        final String currentEnvVar = trimToNull(env.get(name));

        return currentEnvVar != null && currentEnvVar.equals(value);
    }

    static public boolean searchEnvPath(Path path) {
        return searchEnvPath(System.getenv(), path);
    }

    static public boolean searchEnvPath(Map<String,String> env, Path path) {
        return searchEnvPath(trimToNull(env.get("PATH")), path);
    }

    static public boolean searchEnvPath(String pathValueInEnv, Path path) {
        final String[] currentPathVarParts = pathValueInEnv != null ? pathValueInEnv.split(File.pathSeparator) : new String[0];

        for (String currentPathVarPart : currentPathVarParts) {
            if (currentPathVarPart != null && currentPathVarPart.equalsIgnoreCase(path.toString())) {
                return true;
            }
        }

        return false;
    }

    static public String joinIfDelimiterMissing(String v1, String v2, String delimiter) {
        if (v1 == null) {
            return v2;
        } else if (v2 == null) {
            return v1;
        }
        if (v1.endsWith(delimiter)) {
            return v1 + v2;
        }
        return v1 + delimiter + v2;
    }

}
