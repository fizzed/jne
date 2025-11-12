package com.fizzed.jne.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the output of the macOS "sw_vers" command.
 *
 * This class is instantiated using the static `parse()` method,
 * which parses the key-value string output from the command.
 */
public class MacSwVers {

    private final String productName;
    private final String productVersion;
    private final String buildVersion;

    /**
     * Private constructor. Use the static `parse()` method.
     */
    private MacSwVers(String productName, String productVersion, String buildVersion) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.buildVersion = buildVersion;
    }

    /**
     * Parses the multi-line string output from the "sw_vers" command.
     *
     * @param content The complete string output from "sw_vers".
     * @return A populated SwVer object.
     * @throws IllegalArgumentException if the input is null or empty.
     */
    public static MacSwVers parse(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("sw_vers output cannot be null or empty.");
        }

        // Use a map to collect values in a way that is
        // not dependent on the order of lines.
        Map<String, String> values = new HashMap<>();

        // Use Java 8 compatible stream by splitting on newline characters
        Arrays.stream(content.split("\n"))
            .map(String::trim)
            .filter(line -> line.contains(":")) // Ensure the line is a K/V pair
            .forEach(line -> {
                int colonIndex = line.indexOf(':');
                if (colonIndex == -1) {
                    return; // Should be filtered out, but as a safeguard
                }

                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();

                if (!key.isEmpty()) {
                    values.put(key, value);
                }
            });

        // Create the object from the parsed values
        return new MacSwVers(
            values.get("ProductName"),
            values.get("ProductVersion"),
            values.get("BuildVersion")
        );
    }

    // --- Getters ---

    /** @return The product name, e.g., "macOS". */
    public String getProductName() {
        return productName;
    }

    /** @return The product version, e.g., "14.1.1". */
    public String getProductVersion() {
        return productVersion;
    }

    /** @return The build version, e.g., "23B81". */
    public String getBuildVersion() {
        return buildVersion;
    }

    @Override
    public String toString() {
        return "SwVer {" +
            "productName='" + productName + '\'' +
            ", productVersion='" + productVersion + '\'' +
            ", buildVersion='" + buildVersion + '\'' +
            '}';
    }

}