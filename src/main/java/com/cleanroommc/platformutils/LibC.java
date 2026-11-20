/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils;

import java.util.List;
import java.util.Optional;

/**
 * The C library the JVM is linked against, read through {@link Platform#libC()}. Native libraries built for Linux
 * have to match it, as a glibc build fails to load on musl systems such as Alpine, and the other way around.
 */
public enum LibC {

    GLIBC("glibc"),
    MUSL("musl"),
    BIONIC("bionic"),
    UNKNOWN("unknown");

    private final String id;

    LibC(String id) {
        this.id = id;
    }

    /**
     * Looks up a C library by its id.
     *
     * @param id a value of {@link #id()}
     * @return the C library, or {@link #UNKNOWN} when no C library has that id
     */
    public static LibC fromId(String id) {
        for (LibC libC : values()) {
            if (libC.id.equals(id)) {
                return libC;
            }
        }
        return UNKNOWN;
    }

    static LibC detect(Platform platform) {
        if (platform.operatingSystem() == OperatingSystem.ANDROID) {
            return BIONIC;
        }
        if (platform.operatingSystem() != OperatingSystem.LINUX) {
            return UNKNOWN;
        }

        // The mapped libraries show what this process loaded, which probing /lib can't tell when gcompat or a
        // glibc runtime is installed next to musl. Musl wins because a glibc shim on musl still runs on musl.

        Optional<List<String>> maps = platform.readLines("/proc/self/maps");
        if (!maps.isPresent()) {
            return UNKNOWN;
        }
        boolean glibc = false;
        for (String line : maps.get()) {
            if (line.contains("/ld-musl-") || line.contains("/libc.musl-")) {
                return MUSL;
            }
            glibc |= line.contains("/libc.so.6") || line.contains("/libc-2.");
        }
        return glibc ? GLIBC : UNKNOWN;
    }

    static Optional<String> detectGlibcVersion(Platform platform) {
        if (platform.libC() != GLIBC) {
            return Optional.empty();
        }

        // getconf prints "glibc 2.39"

        return platform.run("getconf", "GNU_LIBC_VERSION")
                .filter(output -> !output.isEmpty() && output.get(0).startsWith("glibc "))
                .map(output -> output.get(0).substring("glibc ".length()).trim());
    }

    /**
     * A stable lowercase name such as {@code musl}, used in {@link Platform#id(LibC)}.
     */
    public String id() {
        return id;
    }

}
