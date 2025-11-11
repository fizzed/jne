package com.fizzed.jne.internal;

import com.fizzed.jne.NativeTarget;
import com.fizzed.jne.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class OsVersionDemo {
    static private final Logger log = LoggerFactory.getLogger(OsVersionDemo.class);

    static public void main(String[] args) throws Exception {
        NativeTarget nativeTarget = NativeTarget.detect();

        if (nativeTarget.getOperatingSystem() == OperatingSystem.LINUX || nativeTarget.getOperatingSystem() == OperatingSystem.FREEBSD) {

            // uname -a for kernel
            // e.g. Linux bmh-jjlauer-4 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC Tue Oct  7 13:34:17 UTC 2025 x86_64 GNU/Linux
            // e.g. FreeBSD bmh-dev-x64-freebsd15-1 15.0-ALPHA4 FreeBSD 15.0-ALPHA4 stable/15-n280334-d2b670b27f37 GENERIC amd64
            final String uname = runProcess("uname", "-a");
            log.info("uname: {}", uname);

            // /etc/os-release for distro
            final String osReleaseContent = catFile(Paths.get("/etc/os-release"));
            final OsReleaseFile osReleaseFile = OsReleaseFile.parse(osReleaseContent);

            log.info("os-release: {}", osReleaseFile.getPrettyName());

        } else if (nativeTarget.getOperatingSystem() == OperatingSystem.MACOS) {

            // uname -a for kernel
            // e.g. Linux bmh-jjlauer-4 6.17.0-6-generic #6-Ubuntu SMP PREEMPT_DYNAMIC Tue Oct  7 13:34:17 UTC 2025 x86_64 GNU/Linux
            // e.g. FreeBSD bmh-dev-x64-freebsd15-1 15.0-ALPHA4 FreeBSD 15.0-ALPHA4 stable/15-n280334-d2b670b27f37 GENERIC amd64
            final String uname = runProcess("uname", "-a");
            log.info("uname: {}", uname);

            // sw_vers for distro (or it could be a .plist file we simply read)
            final String swVersContent = runProcess("sw_vers");

            log.info("sw_vers: {}", swVersContent);

        } else if (nativeTarget.getOperatingSystem() == OperatingSystem.WINDOWS) {

            final String currentVersionRegQuery = runProcess("reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
            log.info("currentVersionRegQuery: {}", currentVersionRegQuery);

        }
    }


    static public String catFile(Path file) throws IOException {
        // 1. Read all bytes from the file.
        byte[] bytes = Files.readAllBytes(file);

        // 2. Convert the byte array to a String using a specific charset.
        //    UTF-8 is a safe default.
        return new String(bytes, StandardCharsets.UTF_8);
    }


    /**
     * Runs an external process and captures its combined standard output and error.
     *
     * @param command The command and its arguments to execute (e.g., "cmd.exe", "/C", "dir").
     * @return The combined standard output and standard error as a single String.
     * @throws IOException          if an I/O error occurs (e.g., command not found).
     * @throws InterruptedException if the process is interrupted while waiting.
     * @throws RuntimeException     if the command exits with a non-zero status code.
     */
    static public String runProcess(String... command) throws IOException, InterruptedException {
        // 1. Create the ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder(command);

        // 2. Merge stdout and stderr. This is crucial.
        // It prevents buffer hangs and captures all output.
        pb.redirectErrorStream(true);

        // 3. Start the process
        Process process = pb.start();

        String output;

        // 4. Read the output stream (Java 8 try-with-resources)
        // This ensures the reader is closed even if an exception occurs.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            // 5. Use Java 8 streams to read all lines and join them
            output = reader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
        }

        // 6. Wait for the process to complete
        int exitCode = process.waitFor();

        // 7. Check the exit code. Throw an exception if it's non-zero.
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode
                + ". Command: " + String.join(" ", command)
                + "\nOutput: \n" + output);
        }

        // 8. Return the captured output
        return output;
    }

}