package com.fizzed.jne.internal;

public class MacReleases {

    /**
     * Returns the macOS codename based on the major and minor version numbers.
     *
     * @param major The major version number (e.g., 14 for 14.1.1).
     * @param minor The minor version number (e.g., 1 for 14.1.1).
     * @return The codename as a String (e.g., "Sonoma"), or "Unknown" if not found.
     */
    public static String getVersionName(int major, int minor) {
        switch (major) {
            case 26:
                return "Tahoe";
            case 15:
                return "Sequoia";
            case 14:
                return "Sonoma";
            case 13:
                return "Ventura";
            case 12:
                return "Monterey";
            case 11:
                return "Big Sur";
            case 10:
                // Handle pre-11.x versions
                switch (minor) {
                    case 15:
                        return "Catalina";
                    case 14:
                        return "Mojave";
                    case 13:
                        return "High Sierra";
                    case 12:
                        return "Sierra";
                    case 11:
                        return "El Capitan";
                    case 10:
                        return "Yosemite";
                    case 9:
                        return "Mavericks";
                    case 8:
                        return "Mountain Lion";
                    case 7:
                        return "Lion";
                    case 6:
                        return "Snow Leopard";
                    case 5:
                        return "Leopard";
                    case 4:
                        return "Tiger";
                    case 3:
                        return "Panther";
                    case 2:
                        return "Jaguar";
                    case 1:
                        return "Puma";
                    case 0:
                        return "Cheetah";
                    default:
                        return "Mac OS X 10." + minor;
                }
            default:
                return null;
        }
    }

    /**
     * A convenience method that parses a "ProductVersion" string (e.g., "14.1.1")
     * and returns the macOS codename.
     *
     * @param productVersion The version string, typically from "sw_vers".
     * @return The codename as a String (e.g., "Sonoma"), or "Unknown" if parsing fails.
     */
    public static String getVersionName(String productVersion) {
        if (productVersion == null || productVersion.isEmpty()) {
            return null;
        }

        try {
            String[] parts = productVersion.split("\\.");
            if (parts.length >= 1) {
                int major = Integer.parseInt(parts[0]);

                // For versions 11 and up, minor is "0" by default if not present
                // For version 10, the minor version is critical
                int minor = 0;
                if (parts.length > 1) {
                    minor = Integer.parseInt(parts[1]);
                }

                return getVersionName(major, minor);
            }
        } catch (NumberFormatException e) {
            // Failed to parse the version string
            return null;
        }

        return null;
    }

}