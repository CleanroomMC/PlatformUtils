package com.cleanroommc.platformutils;

public final class Architecture {

    static final Architecture CURRENT = determine();

    private static Architecture determine() {
        final String arch = System.getProperty("os.arch");
        boolean is64Bit = arch.contains("64") || arch.startsWith("armv8");
        boolean isArm = arch.startsWith("arm") || arch.startsWith("aarch64");
        return new Architecture(is64Bit, isArm);
    }

    private final boolean is64Bit, isArm;

    private Architecture(boolean is64Bit, boolean isArm) {
        this.is64Bit = is64Bit;
        this.isArm = isArm;
    }

    boolean is64Bit() {
        return is64Bit;
    }

    boolean isArm() {
        return isArm;
    }

}
