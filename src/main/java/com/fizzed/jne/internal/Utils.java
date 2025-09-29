package com.fizzed.jne.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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

            // Wait for the process to complete and get its exit code
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Process exited with a non-zero code (actual " + exitCode + ")");
            }

            //System.out.println("Process exited with code: " + exitCode);
            //System.out.println("Process Output:");
            //System.out.println(output.toString());

            // You can also read the error stream if needed
            /*StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorOutput.append(errorLine).append(System.lineSeparator());
                }
            }
            if (errorOutput.length() > 0) {
                System.err.println("Process Error Output:");
                System.err.println(errorOutput.toString());
            }*/

        return output.toString();
    }

}