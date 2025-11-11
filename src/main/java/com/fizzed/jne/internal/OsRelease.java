package com.fizzed.jne.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Parses the /etc/os-release file on linux, freebsd, and potentially other operating systems.
 *
 * PRETTY_NAME="Ubuntu 25.10"
 * NAME="Ubuntu"
 * VERSION_ID="25.10"
 * VERSION="25.10 (Questing Quokka)"
 * VERSION_CODENAME=questing
 * ID=ubuntu
 * ID_LIKE=debian
 * HOME_URL="https://www.ubuntu.com/"
 * SUPPORT_URL="https://help.ubuntu.com/"
 * BUG_REPORT_URL="https://bugs.launchpad.net/ubuntu/"
 * PRIVACY_POLICY_URL="https://www.ubuntu.com/legal/terms-and-policies/privacy-policy"
 * UBUNTU_CODENAME=questing
 * LOGO=ubuntu-logo
 *
 */
public class OsRelease {

    private final Map<String,String> values;

    public OsRelease(Map<String,String> values) {
        this.values = values;
    }

    public String get(String key) {
        return this.values.get(key);
    }

    // --- Getters for common properties ---

    public String getName() {
        return this.get("NAME");
    }

    public String getVersion() {
        return this.get("VERSION");
    }

    public String getId() {
        return this.get("ID");
    }

    public String getIdLike() {
        return this.get("ID_LIKE");
    }

    public String getPrettyName() {
        return this.get("PRETTY_NAME");
    }

    public String getVersionId() {
        return this.get("VERSION_ID");
    }

    /**
     * Parses the given os-release file from a Path.
     *
     * @param path The path to the file (e.g., Paths.get("/etc/os-release")).
     * @return An OsRelease object.
     * @throws IOException If the file cannot be read.
     */
    public static OsRelease parse(Path path) throws IOException {
        // Use try-with-resources to ensure the stream is closed
        try (Stream<String> stream = Files.lines(path)) {
            Map<String, String> properties = parseLines(stream);
            return new OsRelease(properties);
        }
    }

    /**
     * Parses the given os-release content from a String.
     *
     * @param content The full string content of the file.
     * @return An OsRelease object.
     */
    public static OsRelease parse(String content) {
        // Use Java 8 Stream.of() to split the string by newlines
        // This stream does not need to be in a try-with-resources block
        Stream<String> stream = Stream.of(content.split("\\r?\\n"));
        Map<String, String> properties = parseLines(stream);
        return new OsRelease(properties);
    }

    /**
     * Core parsing logic that processes a Stream of lines.
     */
    private static Map<String, String> parseLines(Stream<String> stream) {
        Map<String,String> values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        stream
            .filter(line -> line != null && !line.trim().isEmpty() && !line.trim().startsWith("#"))
            .map(line -> line.split("=", 2))
            .filter(parts -> parts.length == 2)
            .forEach(parts -> {
                String key = parts[0].trim();
                String value = unquote(parts[1].trim());
                values.put(key, value);
            });
        return values;
    }

    /**
     * Helper method to remove quotes from the beginning and end of a value.
     * The os-release standard specifies that values may be quoted.
     */
    private static String unquote(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        // Check if the string starts and ends with a quote
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            // Return the substring between the quotes
            return trimmed.substring(1, trimmed.length() - 1);
        }
        // Return the original trimmed string if not quoted
        return trimmed;
    }

}