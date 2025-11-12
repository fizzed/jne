package com.fizzed.jne.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class LocalSystemExecutor implements SystemExecutor {

    @Override
    public String catFile(String file) throws Exception {
        final byte[] bytes = Files.readAllBytes(Paths.get(file));

        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public String execProcess(List<Integer> exitValues, String... command) throws Exception {
        final ProcessBuilder pb = new ProcessBuilder(command);

        // 2. Merge stdout and stderr. This is crucial.
        // It prevents buffer hangs and captures all output.
        pb.redirectErrorStream(true);

        // 3. Start the process
        Process process = pb.start();

        String output;

        // 4. Read the output stream (Java 8 try-with-resources)
        // This ensures the reader is closed even if an exception occurs.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();

        if (exitValues != null && !exitValues.isEmpty() && !exitValues.contains(exitCode)) {
            throw new Exception("Unexpected exit code " + exitCode);
        }

        // 8. Return the captured output
        return output;
    }

}