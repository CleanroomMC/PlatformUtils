/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils;

import java.nio.ByteOrder;
import java.util.Locale;

/**
 * Instruction set architectures a JVM reports through {@code os.arch}.
 * <p>
 * This is the JVM's architecture, which is what native libraries have to match, and not always the machine's.
 * An x64 JVM under Rosetta 2 or Windows on Arm emulation reports {@link #X64}.
 * <p>
 * Ids follow the names launchers already use, and otherwise the {@code os.arch} value itself.
 */
public enum Architecture {

    X86("x86", Family.X86, 32, ByteOrder.LITTLE_ENDIAN),
    X64("x64", Family.X86, 64, ByteOrder.LITTLE_ENDIAN),
    X32("x32", Family.X86, 32, ByteOrder.LITTLE_ENDIAN),
    ARM32("arm32", Family.ARM, 32, ByteOrder.LITTLE_ENDIAN),
    ARM64("arm64", Family.ARM, 64, ByteOrder.LITTLE_ENDIAN),
    PPC("ppc", Family.POWER_PC, 32, ByteOrder.BIG_ENDIAN),
    PPC64("ppc64", Family.POWER_PC, 64, ByteOrder.BIG_ENDIAN),
    PPC64LE("ppc64le", Family.POWER_PC, 64, ByteOrder.LITTLE_ENDIAN),
    RISCV32("riscv32", Family.RISC_V, 32, ByteOrder.LITTLE_ENDIAN),
    RISCV64("riscv64", Family.RISC_V, 64, ByteOrder.LITTLE_ENDIAN),
    LOONGARCH64("loongarch64", Family.LOONG_ARCH, 64, ByteOrder.LITTLE_ENDIAN),
    S390("s390", Family.Z, 32, ByteOrder.BIG_ENDIAN),
    S390X("s390x", Family.Z, 64, ByteOrder.BIG_ENDIAN),
    MIPS("mips", Family.MIPS, 32, ByteOrder.BIG_ENDIAN),
    MIPSEL("mipsel", Family.MIPS, 32, ByteOrder.LITTLE_ENDIAN),
    MIPS64("mips64", Family.MIPS, 64, ByteOrder.BIG_ENDIAN),
    MIPS64EL("mips64el", Family.MIPS, 64, ByteOrder.LITTLE_ENDIAN),
    SPARC("sparc", Family.SPARC, 32, ByteOrder.BIG_ENDIAN),
    SPARCV9("sparcv9", Family.SPARC, 64, ByteOrder.BIG_ENDIAN),
    IA64("ia64", Family.ITANIUM, 64, ByteOrder.LITTLE_ENDIAN),
    ALPHA("alpha", Family.ALPHA, 64, ByteOrder.LITTLE_ENDIAN),
    HPPA("hppa", Family.PA_RISC, 32, ByteOrder.BIG_ENDIAN),
    HPPA64("hppa64", Family.PA_RISC, 64, ByteOrder.BIG_ENDIAN),
    M68K("m68k", Family.M68K, 32, ByteOrder.BIG_ENDIAN),
    SH4("sh4", Family.SUPER_H, 32, ByteOrder.LITTLE_ENDIAN),
    E2K("e2k", Family.ELBRUS, 64, ByteOrder.LITTLE_ENDIAN),
    UNKNOWN("unknown", Family.UNKNOWN, 0, ByteOrder.nativeOrder());

    private final String id;
    private final Family family;
    private final int bits;
    private final ByteOrder byteOrder;

    Architecture(String id, Family family, int bits, ByteOrder byteOrder) {
        this.id = id;
        this.family = family;
        this.bits = bits;
        this.byteOrder = byteOrder;
    }

    /**
     * Parses a raw {@code os.arch} value.
     *
     * @param osArch a value of the {@code os.arch} system property
     */
    public static Architecture parse(String osArch) {
        String arch = osArch.toLowerCase(Locale.ROOT).trim();
        switch (arch) {
            case "x86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
            case "ia32":
            case "x86_32":
                return X86;
            case "x64":
            case "amd64":
            case "x86_64":
            case "x86-64":
            case "em64t":
                return X64;
            case "x32":
                return X32;
            case "aarch64":
            case "arm64":
            case "arm64e":
                return ARM64;
            case "ppc":
            case "powerpc":
                return PPC;
            case "ppc64":
            case "powerpc64":
                return PPC64;
            case "ppc64le":
            case "powerpc64le":
                return PPC64LE;
            case "riscv32":
                return RISCV32;
            case "riscv64":
                return RISCV64;
            case "loongarch64":
            case "loong64":
                return LOONGARCH64;
            case "s390":
                return S390;
            case "s390x":
                return S390X;
            case "mips":
                return MIPS;
            case "mipsel":
                return MIPSEL;
            case "mips64":
                return MIPS64;
            case "mips64el":
                return MIPS64EL;
            case "sparc":
                return SPARC;
            case "sparcv9":
            case "sparc64":
                return SPARCV9;
            case "ia64":
            case "ia64n":
            case "ia64w":
                return IA64;
            case "alpha":
                return ALPHA;
            case "hppa":
            case "pa_risc":
            case "pa_risc2.0":
                return HPPA;
            case "hppa64":
            case "pa_risc2.0w":
                return HPPA64;
            case "m68k":
                return M68K;
            case "sh":
            case "sh4":
                return SH4;
            case "e2k":
                return E2K;
            default:
                // arm, armhf, armel, armv7l, armv8l and armv9l are all 32-bit
                // the last two being 32-bit code on a 64-bit core
                // Other armv8 and armv9 names describe 64-bit cores, so they stay unknown
                // Big-endian ARM such as armeb has no JVM release, report it as unknown
                boolean arm32 = arch.startsWith("arm")
                        && !arch.contains("eb")
                        && (!(arch.startsWith("armv8") || arch.startsWith("armv9")) || arch.endsWith("l"));
                return arm32 || arch.equals("aarch32") ? ARM32 : UNKNOWN;
        }
    }

    /**
     * Looks up an architecture by its id.
     *
     * @param id a value of {@link #id()}
     * @return the architecture, or {@link #UNKNOWN} when no architecture has that id
     */
    public static Architecture fromId(String id) {
        for (Architecture arch : values()) {
            if (arch.id.equals(id)) {
                return arch;
            }
        }
        return UNKNOWN;
    }

    /**
     * A stable lowercase name such as {@code arm64}, used in {@link Platform#id()}.
     */
    public String id() {
        return id;
    }

    public Family family() {
        return family;
    }

    /**
     * The address width, or 0 for {@link #UNKNOWN}.
     */
    public int bits() {
        return bits;
    }

    public ByteOrder byteOrder() {
        return byteOrder;
    }

    public boolean is64Bit() {
        return bits == 64;
    }

    public boolean isX86() {
        return family == Family.X86;
    }

    public boolean isArm() {
        return family == Family.ARM;
    }

    public enum Family {

        X86,
        ARM,
        POWER_PC,
        RISC_V,
        LOONG_ARCH,
        Z,
        MIPS,
        SPARC,
        ITANIUM,
        ALPHA,
        PA_RISC,
        M68K,
        SUPER_H,
        ELBRUS,
        UNKNOWN

    }

}
