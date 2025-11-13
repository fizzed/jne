package com.fizzed.jne.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class SystemExecutorLocal implements SystemExecutor {
    protected final Logger log = LoggerFactory.getLogger(getClass());

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
        if (log.isTraceEnabled()) {
            final String cmd = String.join(" ", command);
            log.trace("Executing command: {}", cmd);
        }

        Process process = pb.start();

        // close input stream?
        process.getOutputStream().close();

        // 4. Read the output stream using a separate thread
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Thread outputThread = new Thread(() -> {
            try (InputStream input = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = input.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, n);
                }
            } catch (Exception e) {
                // Ignore exceptions during stream reading
            }
        });
        outputThread.start();

        // Wait for the process to complete
        int exitCode = process.waitFor();

        // wait for output thread to finish
        outputThread.join();

        if (exitValues != null && !exitValues.isEmpty() && !exitValues.contains(exitCode)) {
            throw new Exception("Unexpected exit code " + exitCode);
        }

        // 8. Return the captured output
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

}