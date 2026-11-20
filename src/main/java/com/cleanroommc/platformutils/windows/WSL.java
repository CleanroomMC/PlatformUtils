/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils.windows;

import com.cleanroommc.platformutils.OperatingSystem;
import com.cleanroommc.platformutils.Platform;

import java.util.Collections;
import java.util.List;

/**
 * The Windows Subsystem for Linux generation the JVM runs under, read through {@link Platform#wsl()}.
 * Both report {@link OperatingSystem#LINUX}.
 */
public enum WSL {

    NONE,
    /**
     * Translates Linux syscalls on the Windows kernel. Windows drives under {@code /mnt} are fast.
     */
    WSL1,
    /**
     * Runs a real Linux kernel in a lightweight VM. Linux filesystem is native.
     */
    WSL2;

    /**
     * Prefer {@link Platform#wsl()}, which keeps the result.
     *
     * @param platform the platform to read
     */
    public static WSL detect(Platform platform) {
        if (platform.operatingSystem() != OperatingSystem.LINUX) {
            return NONE;
        }

        // WSL1 fakes a kernel release ending in "-Microsoft"
        // WSL2 kernels carry "-microsoft-standard-WSL2"
        // Custom WSL2 kernels may drop the marker

        String release = platform.property("os.version").orElse("");
        if (release.contains("Microsoft")) {
            return WSL1;
        }
        return release.contains("microsoft") || platform.environment("WSL_DISTRO_NAME").isPresent() ? WSL2 : NONE;
    }

    /**
     * Prefer {@link Platform#hasWslInterop()}.
     *
     * @param platform the platform to read
     * @return true when Windows executables can run from here
     */
    public static boolean hasInterop(Platform platform) {
        return platform.wsl() != NONE && (isEnabled(platform, "WSLInterop") || isEnabled(platform, "WSLInterop-late"));
    }

    // WSL registers its /init as the binfmt_misc handler for Windows executables
    // under the "-late" name when systemd has reset the table
    // Containers on WSL2 may share that table without /init
    private static boolean isEnabled(Platform platform, String entry) {
        List<String> lines = platform.readLines("/proc/sys/fs/binfmt_misc/" + entry).orElse(Collections.emptyList());
        return lines.contains("enabled")
                && lines.stream()
                        .filter(line -> line.startsWith("interpreter "))
                        .anyMatch(line -> platform.exists(line.substring("interpreter ".length())));
    }

}
