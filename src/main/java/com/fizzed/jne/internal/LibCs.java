package com.fizzed.jne.internal;

import com.fizzed.jne.LibC;

import java.util.Arrays;
import java.util.Objects;

public class LibCs {

    /**
     * Parses the output of "ldd /bin/ls" (or similar) to find the file path
     * to the system's primary C library (e.g., libc, musl, or uclibc).
     *
     * @param lddOutput The full string output from the ldd command.
     * @return An Optional containing the path (e.g., "/lib/x86_64-linux-gnu/libc.so.6"),
     * or Optional.empty() if no matching library path is found.
     */
    public static String parseLibCPath(String lddOutput) {
        if (lddOutput == null || lddOutput.isEmpty()) {
            return null;
        }

        // Use Arrays.stream(split()) for Java 8 compatibility (instead of .lines())
        return Arrays.stream(lddOutput.split("\n"))
            .map(String::trim)
            .filter(line -> line.contains("=>")) // We only care about lines with links
            .filter(line -> {
                // Split "libname.so.1 => /path/to/lib"
                String[] nameAndPath = line.split("=>");
                if (nameAndPath.length < 1) return false;

                String libName = nameAndPath[0].toLowerCase().trim();

                // Check for the most common C library names
                return libName.startsWith("libc.so") ||         // glibc (e.g., libc.so.6)
                    libName.startsWith("libuclibc.so") ||   // uclibc (e.g., libuClibc.so.1)
                    libName.startsWith("libc.musl");      // musl (e.g., libc.musl-x86_64.so.1)
            })
            .map(line -> {
                // Line is: [name] => [path] ([address])
                String[] nameAndPath = line.split("=>");
                if (nameAndPath.length < 2) return null;

                // pathPart: " /lib/x86_64-linux-gnu/libc.so.6 (0x...)"
                String pathPart = nameAndPath[1].trim();

                // Split by whitespace to get just the path
                // pathTokens[0] will be the path
                String[] pathTokens = pathPart.split("\\s+");
                if (pathTokens.length > 0) {
                    return pathTokens[0]; // This is the path
                }
                return null;
            })
            .filter(Objects::nonNull) // Filter out any lines that failed parsing
            .findFirst()
            .orElse(null);
    }

    static public LibC parseLibC(String lddOrSoOutput) {
        if (lddOrSoOutput == null || lddOrSoOutput.isEmpty()) {
            return null;
        }

        if (lddOrSoOutput.contains("gnu")) {
            return LibC.GLIBC;
        } else if (lddOrSoOutput.contains("musl")) {
            return LibC.MUSL;
        } else if (lddOrSoOutput.toLowerCase().contains("uclibc")) {
            return LibC.UCLIBC;
        }

        return null;
    }

    /**
     * Parses the output of executing the C library .so file directly
     * (e.g., the output of "/lib/x86_64-linux-gnu/libc.so.6").
     *
     * This method is designed to find the version for GLIBC and MUSL.
     *
     * @param soFileOutput The full string output from executing the .so file.
     * @return An Optional containing the version string (e.g., "2.35" or "1.2.3"),
     * or Optional.empty() if no version is found.
     */
    public static String parseLibCVersion(String soFileOutput) {
        if (soFileOutput == null || soFileOutput.isEmpty()) {
            return null;
        }

        String[] lines = soFileOutput.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // 1. Check for GLIBC
            // "GNU C Library (Ubuntu GLIBC 2.35-0ubuntu3.1) stable release version 2.35."
            if (line.startsWith("GNU C Library")) {
                String[] parts = line.split("\\s+");
                if (parts.length > 0) {
                    // The version is the last word on the line
                    String lastPart = parts[parts.length - 1];
                    // Remove trailing period if it exists
                    if (lastPart.endsWith(".")) {
                        lastPart = lastPart.substring(0, lastPart.length() - 1);
                    }
                    if (!lastPart.isEmpty()) {
                        return lastPart;
                    }
                }
            }

            // 2. Check for MUSL
            // "musl libc (x86_64)"
            // "Version 1.2.3"
            if (line.startsWith("musl libc")) {
                // Check the *next* line for the version
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();
                    if (nextLine.startsWith("Version ")) {
                        String[] parts = nextLine.split("\\s+");
                        if (parts.length > 1) {
                            return parts[1]; // "1.2.3"
                        }
                    }
                }
            }
        }

        return null; // No match
    }

}