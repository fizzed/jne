package com.fizzed.jne.internal;

/**
 * Parses the output of the "uname -a" command from various
 * UNIX-like operating systems (Linux, macOS, BSDs, Android).
 * <p>
 * This class is instantiated using the static `parse()` method.
 * <p>
 * The "uname -a" command output is not perfectly consistent.
 * This parser uses a more robust logic by checking the last field
 * to determine if it's a GNU-style output or a 5-field POSIX/BSD-style output.
 * <p>
 * - GNU/Linux: 8+ fields, ends with "GNU/Linux" or similar.
 * (s n r [greedy v] m [p] [i] o)
 * - POSIX/BSD/Android/Solaris: 5 fields, ends with 'machine'.
 * (s n r [greedy v] m)
 * <p>
 * This class implements a hybrid parsing strategy to handle these cases.
 */
public class Uname {

    // Standard POSIX fields
    private final String sysname;          // Kernel name (e.g., "Linux", "Darwin")
    private final String nodename;         // Network node hostname
    private final String version;          // Kernel release (e.g., "5.15.0-88-generic")
    private final String flavor;          // Kernel version (a long, descriptive string)
    private final String machine;          // Machine hardware name (e.g., "x86_64")

    // GNU-specific fields (will be null on non-GNU systems)
    private final String processor;        // (Linux only) e.g., "x86_64" or "unknown"
    private final String hardwarePlatform; // (Linux only) e.g., "x86_64" or "unknown"
    private final String operatingSystem;  // (Linux only) e.g., "GNU/Linux"

    // The original un-parsed input string
    private final String source;

    /**
     * Private constructor to build the Uname object.
     * Use the static `parse()` method to create an instance.
     *
     * @param sysname          Kernel name
     * @param nodename         Hostname
     * @param version          Kernel version
     * @param flavor          Kernel flavor
     * @param machine          Hardware name
     * @param processor        Processor type (GNU only)
     * @param hardwarePlatform Hardware platform (GNU only)
     * @param operatingSystem  OS name (GNU only)
     * @param source           The original "uname -a" string
     */
    private Uname(String sysname, String nodename, String version, String flavor,
                  String machine, String processor, String hardwarePlatform,
                  String operatingSystem, String source) {
        this.sysname = sysname;
        this.nodename = nodename;
        this.version = version;
        this.flavor = flavor;
        this.machine = machine;
        this.processor = processor;
        this.hardwarePlatform = hardwarePlatform;
        this.operatingSystem = operatingSystem;
        this.source = source;
    }

    /**
     * Parses the full output of "uname -a" and returns a Uname object.
     *
     * @param output The complete string returned by the "uname -a" command.
     * @return A populated Uname object.
     * @throws IllegalArgumentException if the input string is invalid.
     */
    public static Uname parse(String output) {
        if (output == null || output.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }

        String originalInput = output;
        String[] parts = output.trim().split("\\s+");

        if (parts.length < 5) {
            throw new IllegalArgumentException(
                "Invalid uname -a output: expected at least 5 fields.");
        }

        // The first 3 fields are always consistent
        String sysname = parts[0];
        String nodename = parts[1];
        String release = parts[2];

        // Declare local vars for remaining fields
        String version;
        String machine;
        String processor;
        String hardwarePlatform;
        String operatingSystem;

        // --- NEW ROBUST LOGIC ---
        // Check if this is a GNU-style output (e.g., ends with "GNU/Linux")
        // The last field is the Operating System.
        String lastField = parts[parts.length - 1];

        if ("Linux".equals(sysname) && lastField.startsWith("GNU")) {
            // GNU/Linux: s n r [greedy v] m [p] [i] o
            operatingSystem = parts[parts.length - 1];

            // The 'v' string ends with a timestamp, which ends with a year.
            // We search backwards from the end to find the year (e.g., "2025").
            // This marks the end of 'v' and the start of 'm'.
            int yearIndex = -1;
            for (int i = parts.length - 2; i >= 3; i--) {
                if (parts[i].length() == 4 && isNumeric(parts[i])) {
                    // Found a 4-digit year. Assume this is the end of 'v'.
                    yearIndex = i;
                    break;
                }
            }

            if (yearIndex == -1) {
                // Can't find year, fall back to old minimal logic
                machine = parts[parts.length - 2];
                version = joinParts(parts, 3, parts.length - 3);
                processor = "unknown";
                hardwarePlatform = "unknown";
            } else {
                // Found the year. 'v' is everything up to and including it.
                version = joinParts(parts, 3, yearIndex);

                // The fields after the year are m, p, i
                // (parts.length - 2) is the index of the field before 'o'
                int fieldsAfterYear = (parts.length - 2) - yearIndex;

                if (fieldsAfterYear == 3) {
                    // m p i
                    machine = parts[yearIndex + 1];
                    processor = parts[yearIndex + 2];
                    hardwarePlatform = parts[yearIndex + 3];
                } else if (fieldsAfterYear == 2) {
                    // m p (or m i)
                    machine = parts[yearIndex + 1];
                    processor = parts[yearIndex + 2];
                    hardwarePlatform = "unknown"; // Assume 'i' is missing
                } else if (fieldsAfterYear == 1) {
                    // m
                    machine = parts[yearIndex + 1];
                    processor = "unknown";
                    hardwarePlatform = "unknown";
                } else {
                    // No fields after year? Fallback.
                    machine = parts[parts.length - 2];
                    version = joinParts(parts, 3, parts.length - 3);
                    processor = "unknown";
                    hardwarePlatform = "unknown";
                }
            }
        } else {
            // 5-Field Format (POSIX/BSD/Solaris/Android-Linux)
            // (s n r [greedy v] m)
            // The last field is the Machine.
            machine = parts[parts.length - 1];
            version = joinParts(parts, 3, parts.length - 2);

            // GNU fields are not applicable
            processor = null;
            hardwarePlatform = null;
            operatingSystem = null;
        }

        return new Uname(sysname, nodename, release, version, machine,
            processor, hardwarePlatform, operatingSystem, originalInput);
    }

    /**
     * Helper to check if a string is purely numeric.
     * @param s The string to check.
     * @return true if all characters are digits, false otherwise.
     */
    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper to join a range of an array into a space-separated string.
     *
     * @param parts The array of string parts.
     * @param start The index of the first part (inclusive).
     * @param end   The index of the last part (inclusive).
     * @return A single, space-separated string.
     */
    private static String joinParts(String[] parts, int start, int end) {
        if (start > end) {
            return "";
        }
        // Java 8 equivalent of a simple loop
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            sb.append(parts[i]);
            if (i < end) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    // --- Getters ---

    /**
     * @return The kernel name (e.g., "Linux", "Darwin").
     */
    public String getSysname() {
        return sysname;
    }

    /**
     * @return The network node hostname.
     */
    public String getNodename() {
        return nodename;
    }

    /**
     * @return The kernel release (e.g., "5.15.0-88-generic").
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return The kernel version (long, descriptive string).
     */
    public String getFlavor() {
        return flavor;
    }

    /**
     * @return The machine hardware name (e.g., "x86_64").
     */
    public String getMachine() {
        return machine;
    }

    /**
     * @return (Linux only) The processor type (e.g., "x86_64") or null.
     */
    public String getProcessor() {
        return processor;
    }

    /**
     * @return (Linux only) The hardware platform (e.g., "x86_64") or null.
     */
    public String getHardwarePlatform() {
        return hardwarePlatform;
    }

    /**
     * @return (Linux only) The operating system name (e.g., "GNU/Linux") or null.
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * @return The original, un-parsed input string.
     */
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Uname (Parsed):\n" +
            "  sysname          : " + sysname + "\n" +
            "  nodename         : " + nodename + "\n" +
            "  release          : " + version + "\n" +
            "  version          : " + flavor + "\n" +
            "  machine          : " + machine + "\n" +
            "  processor        : " + (processor != null ? processor : "N/A") + "\n" +
            "  hardwarePlatform : " + (hardwarePlatform != null ? hardwarePlatform : "N/A") + "\n" +
            "  operatingSystem  : " + (operatingSystem != null ? operatingSystem : "N/A");
    }

}