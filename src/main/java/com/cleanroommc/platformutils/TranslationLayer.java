/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The layer that runs the JVM on a machine of another architecture, read through
 * {@link Platform#translationLayer()}.
 * <p>
 * {@link Platform#architecture()} describes the JVM, while {@link Platform#machineArchitecture()} describes the
 * hardware. They differ exactly when a translation layer is active.
 */
public enum TranslationLayer {

    /**
     * The JVM runs natively.
     */
    NONE,
    /**
     * Rosetta 2 runs an x64 JVM on Apple Silicon.
     */
    ROSETTA,
    /**
     * WOW64 runs a 32-bit JVM on 64-bit Windows, and emulates it as well on ARM64.
     */
    WOW64,
    /**
     * Prism runs an x64 JVM on ARM64 Windows.
     */
    PRISM;

    static TranslationLayer detect(Platform platform) {
        Architecture jvm = platform.architecture();
        if (platform.machineArchitecture() == jvm) {
            return NONE;
        }
        if (platform.operatingSystem() == OperatingSystem.MAC_OS) {
            return ROSETTA;
        }
        return jvm.bits() == 32 ? WOW64 : PRISM;
    }

    static Architecture detectMachine(Platform platform) {
        Architecture jvm = platform.architecture();
        if (platform.operatingSystem() == OperatingSystem.MAC_OS) {
            return jvm == Architecture.X64 && hasArm64Hardware(platform) ? Architecture.ARM64 : jvm;
        }
        if (platform.operatingSystem() == OperatingSystem.WINDOWS) {
            Optional<String> wow64 = platform.environment("PROCESSOR_ARCHITEW6432");
            if (wow64.isPresent()) {
                return Architecture.parse(wow64.get());
            }
            return jvm == Architecture.X64 ? windowsArchitecture(platform, jvm) : jvm;
        }
        return jvm;
    }

    // Rosetta hides the hardware from most sysctls
    // hw.optional.arm64 still reports Apple Silicon
    // sysctl.proc_translated would describe the sysctl child, which may not run translated like the JVM
    private static boolean hasArm64Hardware(Platform platform) {
        return platform.run("sysctl", "-n", "hw.optional.arm64")
                .filter(output -> !output.isEmpty() && output.get(0).trim().equals("1"))
                .isPresent();
    }

    // Prism shows the process AMD64 in its environment
    // but the system environment keeps the native value
    // reg prints it as "    PROCESSOR_ARCHITECTURE    REG_SZ    ARM64".
    private static Architecture windowsArchitecture(Platform platform, Architecture fallback) {
        List<String> output = platform.run("reg", "query",
                "HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment", "/v",
                "PROCESSOR_ARCHITECTURE")
                .orElse(Collections.emptyList());
        for (String line : output) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 3 && parts[0].equals("PROCESSOR_ARCHITECTURE")) {
                return Architecture.parse(parts[2]);
            }
        }
        return fallback;
    }

}
