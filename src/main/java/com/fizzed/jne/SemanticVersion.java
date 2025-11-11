package com.fizzed.jne;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Java 8 class to parse and compare semantic version strings,
 * especially the 'release' strings from a Uname object, and
 * Java System Properties (e.g., "java.version").
 * <p>
 * This class supports:
 * - Up to 4 version parts (major, minor, patch, revision).
 * - A "flavor" string (e.g., "RELEASE", "ea", "lts").
 * - A "buildMetadata" string (e.g., "8-LTS", "12").
 * - A static `parse()` method.
 * - `equals()`, `hashCode()`, and `compareTo()` for sorting.
 * <p>
 * The sorting logic follows standard SemVer precedence:
 * - Numeric parts are compared first.
 * - Build metadata (after a "+") is ignored for comparison.
 * - A version with no flavor (e.g., "1.2.3") is considered
 * GREATER than a version with a flavor (e.g., "1.2.3-beta").
 * - If both versions have flavors, they are compared alphabetically.
 */
public class SemanticVersion implements Comparable<SemanticVersion> {

    private final int major;
    private final int minor;
    private final int patch;
    private final int revision; // The optional 4th part
    private final String flavor; // e.g., "RELEASE", "88-generic", "0-lts"
    private final String buildMetadata; // e.g. "12", "8-LTS-191"
    private final String source;

    /**
     * Private constructor. Use the static `parse()` method.
     */
    private SemanticVersion(int major, int minor, int patch, int revision,
                            String flavor, String buildMetadata, String source) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.revision = revision;
        this.flavor = (flavor == null || flavor.isEmpty()) ? null : flavor;
        this.buildMetadata = (buildMetadata == null || buildMetadata.isEmpty()) ? null : buildMetadata;
        this.source = source;
    }

    /**
     * Helper to safely parse an integer from a string.
     *
     * @param s The string to parse.
     * @return The integer value, or 0 if parsing fails.
     */
    private static int parseInt(String s) {
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Helper to check if a string is purely numeric.
     *
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
     * Parses a version string (like a Uname 'release' or 'java.version')
     * into a SemanticVersion object.
     *
     * @param versionString The input string (e.g., "5.15.0-88-generic", "1.8.0_311", "17.0.5+8-LTS").
     * @return A populated SemanticVersion object.
     * @throws IllegalArgumentException if the input is null or empty.
     */
    public static SemanticVersion parse(String versionString) {
        if (versionString == null || versionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty.");
        }

        String originalSource = versionString.trim();
        String buildMetadata = null;
        String mainPart = originalSource;

        // 1. Separate build metadata (anything after "+")
        // This is ignored by compareTo/equals, as per SemVer 2.0
        int plusIndex = mainPart.indexOf('+');
        if (plusIndex != -1) {
            buildMetadata = mainPart.substring(plusIndex + 1);
            mainPart = mainPart.substring(0, plusIndex);
        }

        // 2. Tokenize the main part by all common delimiters
        // This handles "1.8.0_311", "5.15.61-0-lts", and "17.0.5"
        String[] components = mainPart.split("[.\\-_]");

        List<Integer> numParts = new ArrayList<>();
        List<String> flavorParts = new ArrayList<>();

        // 3. Iterate and classify components
        for (String component : components) {
            if (component.isEmpty()) {
                continue; // Skip empty parts (e.g., "1..2")
            }

            // If we are still collecting numbers (up to 4) and it's numeric...
            if (flavorParts.isEmpty() && isNumeric(component) && numParts.size() < 4) {
                numParts.add(parseInt(component));
            } else {
                // ...otherwise, it's part of the flavor.
                flavorParts.add(component);
            }
        }

        // 4. Populate version numbers
        int major = (numParts.size() > 0) ? numParts.get(0) : 0;
        int minor = (numParts.size() > 1) ? numParts.get(1) : 0;
        int patch = (numParts.size() > 2) ? numParts.get(2) : 0;
        int revision = (numParts.size() > 3) ? numParts.get(3) : 0;

        // 5. Assemble the final flavor string
        String flavor = (flavorParts.isEmpty()) ? null : String.join("-", flavorParts);

        return new SemanticVersion(major, minor, patch, revision, flavor, buildMetadata, originalSource);
    }

    // --- Getters ---

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public int getRevision() {
        return revision;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getBuildMetadata() {
        return buildMetadata;
    }

    public String getSource() {
        return source;
    }

    @Override
    public int compareTo(SemanticVersion that) {
        if (that == null) {
            return 1; // This object is greater than null
        }

        // Compare numeric parts first
        if (this.major != that.major) return Integer.compare(this.major, that.major);
        if (this.minor != that.minor) return Integer.compare(this.minor, that.minor);
        if (this.patch != that.patch) return Integer.compare(this.patch, that.patch);
        if (this.revision != that.revision) return Integer.compare(this.revision, that.revision);

        // Numeric parts are equal, now compare by flavor (pre-release)
        // A version with no flavor (null) is "final" and GREATER than one with a flavor.
        if (this.flavor == null && that.flavor != null) {
            return 1; // this (e.g., 1.2.3) > that (e.g., 1.2.3-beta)
        }
        if (this.flavor != null && that.flavor == null) {
            return -1; // this (e.g., 1.2.3-beta) < that (e.g., 1.2.3)
        }
        if (this.flavor == null && that.flavor == null) {
            return 0; // Both are null, they are equal
        }

        // Both have flavors, compare them alphabetically
        return this.flavor.compareTo(that.flavor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticVersion that = (SemanticVersion) o;
        // Build metadata is NOT included in equals(), per SemVer spec
        return major == that.major &&
            minor == that.minor &&
            patch == that.patch &&
            revision == that.revision &&
            Objects.equals(flavor, that.flavor);
    }

    @Override
    public int hashCode() {
        // Build metadata is NOT included in hashCode(), per SemVer spec
        return Objects.hash(major, minor, patch, revision, flavor);
    }

    @Override
    public String toString() {
        return "SemanticVersion {" +
            "major=" + major +
            ", minor=" + minor +
            ", patch=" + patch +
            ", revision=" + revision +
            ", flavor='" + (flavor != null ? flavor : "") + '\'' +
            ", buildMetadata='" + (buildMetadata != null ? buildMetadata : "") + '\'' +
            ", original='" + source + '\'' +
            '}';
    }

    /**
     * Main method to demonstrate parsing and sorting.
     */
    public static void main(String[] args) {
        List<String> releaseStrings = new ArrayList<>();
        // --- Uname Examples ---
        releaseStrings.add("5.15.0-88-generic"); // Ubuntu
        releaseStrings.add("23.1.0");           // macOS
        releaseStrings.add("13.2-RELEASE");      // FreeBSD
        releaseStrings.add("7.4");               // OpenBSD
        releaseStrings.add("9.2_STABLE");        // NetBSD
        releaseStrings.add("6.2.1-RELEASE");     // DragonFlyBSD
        releaseStrings.add("5.11");              // Solaris
        releaseStrings.add("4.19.113-gbe0c0b1122a2"); // Android
        releaseStrings.add("5.15.61-0-lts");     // Alpine (The one you asked about)

        // --- Sorting Demos ---
        releaseStrings.add("13.2-BETA");
        releaseStrings.add("13.2");

        // --- Java Version Examples ---
        releaseStrings.add("1.8.0_311");         // Java 8
        releaseStrings.add("11.0.1");            // Java 11
        releaseStrings.add("17.0.5+8-LTS-191");  // Java 17 (with build meta)
        releaseStrings.add("21.0.1+12-LTS");     // Java 21 (with build meta)
        releaseStrings.add("23-ea+20");          // Java 23 Early Access

        List<SemanticVersion> versions = new ArrayList<>();
        System.out.println("--- Parsing... ---");
        for (String s : releaseStrings) {
            SemanticVersion v = SemanticVersion.parse(s);
            versions.add(v);
            System.out.println("Parsed: " + v);
        }

        System.out.println("\n--- Sorting... ---");
        Collections.sort(versions);

        System.out.println("\n--- Sorted Results (Original Strings) ---");
        for (SemanticVersion v : versions) {
            System.out.println(v.getSource());
        }
    }
}