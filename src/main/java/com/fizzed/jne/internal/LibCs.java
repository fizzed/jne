package com.fizzed.jne.internal;

import com.fizzed.jne.LibC;

import java.util.regex.Pattern;

public class LibCs {

    static public class PathResult {
        private final LibC libC;
        private final String path;

        public PathResult(LibC libC, String path) {
            this.libC = libC;
            this.path = path;
        }

        public LibC getLibC() {
            return libC;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * Parses the output of "ldd /bin/ls" (or similar) to find the file path
     * to the system's primary C library (e.g., libc, musl, or uclibc).
     *
     * @param content The full string output from the ldd command.
     * @return An Optional containing the path (e.g., "/lib/x86_64-linux-gnu/libc.so.6"),
     * or Optional.empty() if no matching library path is found.
     */
    static public PathResult parsePath(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();

            // We only care about lines with links
            if (!line.contains("=>")) {
                continue;
            }

            // Split "libname.so.1 => /path/to/lib" 
            String[] nameAndPath = line.split("=>");
            if (nameAndPath.length < 1) {
                continue;
            }

            String libName = nameAndPath[0].toLowerCase().trim();

            // Check for the most common C library names
            LibC libC = null;
            if (libName.startsWith("libc.so")) {
                // glibc (e.g., libc.so.6)
                libC = LibC.GLIBC;
            } else if (libName.startsWith("libuclibc.so")) {
                // uclibc (e.g., libuClibc.so.1)
                libC = LibC.UCLIBC;
            } else if (libName.contains("libc") && libName.contains("musl")) {
                // musl (e.g., libc.musl-x86_64.so.1)
                libC = LibC.MUSL;
            }

            if (libC == null) {
                continue;
            }

            if (nameAndPath.length < 2) {
                continue;
            }

            // pathPart: " /lib/x86_64-linux-gnu/libc.so.6 (0x...)"
            String pathPart = nameAndPath[1].trim();

            // Split by whitespace to get just the path
            // pathTokens[0] will be the path  
            String[] pathTokens = pathPart.split("\\s+");
            if (pathTokens.length > 0) {
                // This is the path
                return new PathResult(libC, pathTokens[0]);
            }
        }

        return null;
    }

    static final Pattern GLIBC_VERSION_PATTERN = java.util.regex.Pattern.compile("(\\d+\\.\\d+)");
    static final Pattern MUSL_VERSION_PATTERN = java.util.regex.Pattern.compile("(\\d+\\.\\d+\\.\\d+)");

    /**
     * Parses the output of executing the C library .so file directly
     * (e.g., the output of "/lib/x86_64-linux-gnu/libc.so.6").
     *
     * This method is designed to find the version for GLIBC and MUSL.
     *
     * @param content The full string output from executing the .so file.
     * @return An Optional containing the version string (e.g., "2.35" or "1.2.3"),
     * or Optional.empty() if no version is found.
     */
    public static String parseVersion(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // we will use a regex to extract the version
        if (content.contains("GNU C")) {
            java.util.regex.Matcher matcher = GLIBC_VERSION_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else if (content.contains("musl libc")) {
            java.util.regex.Matcher matcher = MUSL_VERSION_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;    // No match
    }

}