package com.cleanroommc.platformutils;

public final class Platform {

    private static final Platform CURRENT = new Platform();

    public static Platform current() {
        return CURRENT;
    }

    private final OperatingSystem operatingSystem;
    private final Architecture architecture;

    private Platform() {
        this.operatingSystem = OperatingSystem.CURRENT;
        this.architecture = Architecture.CURRENT;
    }

    public OperatingSystem operatingSystem() {
        return operatingSystem;
    }

    public Architecture architecture() {
        return architecture;
    }

    public boolean isWindows() {
        return this.operatingSystem.isWindows();
    }

    public boolean isMacOS() {
        return this.operatingSystem.isMacOS();
    }

    public boolean isLinux() {
        return this.operatingSystem.isLinux();
    }

    public boolean is64Bit() {
        return this.architecture.is64Bit();
    }

    public boolean isArm() {
        return this.architecture.isArm();
    }

}


