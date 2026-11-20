/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils;

import java.util.Locale;

/**
 * Operating systems a JVM reports through {@code os.name}.
 */
public enum OperatingSystem {

    WINDOWS("windows", false, false),
    MAC_OS("macos", true, false),
    LINUX("linux", true, false),
    ANDROID("android", true, false),
    FREE_BSD("freebsd", true, true),
    OPEN_BSD("openbsd", true, true),
    NET_BSD("netbsd", true, true),
    DRAGONFLY_BSD("dragonfly", true, true),
    SOLARIS("solaris", true, false),
    AIX("aix", true, false),
    HP_UX("hpux", true, false),
    Z_OS("zos", true, false),
    IBM_I("ibmi", false, false),
    MIDNIGHT_BSD("midnightbsd", true, true),
    HAIKU("haiku", true, false),
    IOS("ios", true, false),
    QNX("qnx", true, false),
    HURD("hurd", true, false),
    OPEN_VMS("openvms", false, false),
    UNKNOWN("unknown", false, false);

    private final String id;
    private final boolean unixLike;
    private final boolean bsd;

    OperatingSystem(String id, boolean unixLike, boolean bsd) {
        this.id = id;
        this.unixLike = unixLike;
        this.bsd = bsd;
    }

    /**
     * Parses a raw {@code os.name} value.
     *
     * @param osName a value of the {@code os.name} system property
     */
    public static OperatingSystem parse(String osName) {
        String name = osName.toLowerCase(Locale.ROOT).trim();
        if (name.startsWith("windows")) {
            return WINDOWS;
        }
        if (name.startsWith("mac") || name.startsWith("darwin")) {
            return MAC_OS;
        }
        if (name.startsWith("linux")) {
            return LINUX;
        }
        if (name.startsWith("freebsd")) {
            return FREE_BSD;
        }
        if (name.startsWith("openbsd")) {
            return OPEN_BSD;
        }
        if (name.startsWith("netbsd")) {
            return NET_BSD;
        }
        if (name.startsWith("dragonfly")) {
            return DRAGONFLY_BSD;
        }
        // illumos distributions report SunOS too
        if (name.startsWith("sunos") || name.startsWith("solaris") || name.startsWith("illumos")) {
            return SOLARIS;
        }
        if (name.startsWith("aix")) {
            return AIX;
        }
        if (name.startsWith("hp-ux")) {
            return HP_UX;
        }
        if (name.startsWith("z/os") || name.startsWith("os/390")) {
            return Z_OS;
        }
        if (name.startsWith("os/400")) {
            return IBM_I;
        }
        if (name.startsWith("midnightbsd")) {
            return MIDNIGHT_BSD;
        }
        if (name.startsWith("haiku")) {
            return HAIKU;
        }
        if (name.startsWith("ios")) {
            return IOS;
        }
        if (name.startsWith("qnx")) {
            return QNX;
        }
        // GNU/Hurd reports its uname sysname, while GNU/kFreeBSD and friends carry a suffix
        if (name.equals("gnu")) {
            return HURD;
        }
        if (name.startsWith("openvms")) {
            return OPEN_VMS;
        }
        return UNKNOWN;
    }

    /**
     * Looks up an operating system by its id.
     *
     * @param id a value of {@link #id()}
     * @return the operating system, or {@link #UNKNOWN} when no operating system has that id
     */
    public static OperatingSystem fromId(String id) {
        for (OperatingSystem os : values()) {
            if (os.id.equals(id)) {
                return os;
            }
        }
        return UNKNOWN;
    }

    static OperatingSystem detect(Platform platform) {
        // Android reports its kernel as os.name, so only the runtime tells it apart from Linux
        String runtime = platform.property("java.runtime.name").orElse("")
                + platform.property("java.vm.vendor").orElse("");
        if (runtime.toLowerCase(Locale.ROOT).contains("android")) {
            return ANDROID;
        }
        return parse(platform.property("os.name").orElse(""));
    }

    /**
     * A stable lowercase name such as {@code macos}, used in {@link Platform#id()}.
     */
    public String id() {
        return id;
    }

    public boolean isWindows() {
        return this == WINDOWS;
    }

    public boolean isMacOS() {
        return this == MAC_OS;
    }

    /**
     * True for desktop and server Linux. Android runs a Linux kernel but reports {@link #ANDROID}.
     */
    public boolean isLinux() {
        return this == LINUX;
    }

    public boolean isAndroid() {
        return this == ANDROID;
    }

    public boolean isBsd() {
        return bsd;
    }

    /**
     * True for POSIX systems with a Unix shell and file layout, macOS and Android included.
     */
    public boolean isUnixLike() {
        return unixLike;
    }

    /**
     * The file name of an executable, such as {@code java.exe} on Windows and {@code java} elsewhere.
     *
     * @param name the executable name without an extension
     */
    public String executable(String name) {
        return this == WINDOWS ? name + ".exe" : name;
    }

}
