package com.cleanroommc.platformutils;

public enum OperatingSystem {

    WINDOWS,
    MAC_OS,
    LINUX;

    static final OperatingSystem CURRENT = determine();

    private static OperatingSystem determine() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MAC_OS;
        }
        // Or unknown
        return OperatingSystem.LINUX;
    }

    boolean isWindows() {
        return this == WINDOWS;
    }

    boolean isMacOS() {
        return this == MAC_OS;
    }

    boolean isLinux() {
        return this == LINUX;
    }

}
