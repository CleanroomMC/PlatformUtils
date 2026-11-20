/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils;

import com.cleanroommc.platformutils.windows.WSL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An operating system and architecture pair, identified by ids such as {@code linux-arm64}, along with everything
 * else this library detects about the system.
 * <p>
 * {@link #current()} reads the running system through its system properties, environment variables, files and
 * commands. {@link #from(Map, Map)} and {@link #builder()} supply every input instead, so Windows or macOS detection
 * can be unit tested on a Linux CI runner. Inputs left out read as absent. {@link #of} and {@link #parse} describe a
 * platform with no inputs at all.
 * <p>
 * Each value is detected on first use and then kept, except {@link #hasWslInterop()}. Platforms are equal when their
 * operating system and architecture are.
 */
public final class Platform {

    private static final Platform CURRENT = new Platform(Inputs.SYSTEM, null, null, null);

    private final Inputs inputs;
    private final Lazy<OperatingSystem> operatingSystem;
    private final Lazy<Architecture> architecture;
    private final Lazy<LibC> libC;
    private final Lazy<Optional<String>> glibcVersion = new Lazy<>(() -> LibC.detectGlibcVersion(this));
    private final Lazy<Optional<OsRelease>> osRelease = new Lazy<>(() -> OsRelease.detect(this));
    private final Lazy<Architecture> machineArchitecture = new Lazy<>(() -> TranslationLayer.detectMachine(this));
    private final Lazy<WSL> wsl = new Lazy<>(() -> WSL.detect(this));

    private Platform(Inputs inputs, OperatingSystem operatingSystem, Architecture architecture, LibC libC) {
        this.inputs = inputs;
        this.operatingSystem = operatingSystem == null
                ? new Lazy<>(() -> OperatingSystem.detect(this))
                : new Lazy<>(() -> operatingSystem);
        this.architecture = architecture == null
                ? new Lazy<>(() -> Architecture.parse(property("os.arch").orElse("")))
                : new Lazy<>(() -> architecture);
        this.libC = libC == null ? new Lazy<>(() -> LibC.detect(this)) : new Lazy<>(() -> libC);
    }

    /**
     * The running system.
     */
    public static Platform current() {
        return CURRENT;
    }

    /**
     * A platform with no inputs, so only its operating system and architecture are known.
     *
     * @param operatingSystem the operating system
     * @param architecture the JVM's architecture
     */
    public static Platform of(OperatingSystem operatingSystem, Architecture architecture) {
        return new Platform(Inputs.NONE, operatingSystem, architecture, null);
    }

    /**
     * A platform with no inputs and a known C library.
     *
     * @param operatingSystem the operating system
     * @param architecture the JVM's architecture
     * @param libC the C library the JVM is linked against
     */
    public static Platform of(OperatingSystem operatingSystem, Architecture architecture, LibC libC) {
        return new Platform(Inputs.NONE, operatingSystem, architecture, libC);
    }

    /**
     * A platform described by system properties and environment variables alone, with no files or commands.
     *
     * @param properties system properties such as {@code os.name} and {@code os.arch}
     * @param environment environment variables such as {@code PROCESSOR_ARCHITEW6432}
     */
    public static Platform from(Map<String, String> properties, Map<String, String> environment) {
        return builder().properties(properties).environment(environment).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parses a platform id into a platform with no inputs.
     *
     * @param id a value of {@link #id()} or {@link #id(LibC)}, such as {@code macos-arm64} or {@code linux-x64-musl},
     *           where the C library suffix sets {@link #libC()}
     * @throws IllegalArgumentException when the operating system, architecture or C library is unknown
     */
    public static Platform parse(String id) {
        String[] parts = id.split("-", -1);
        OperatingSystem os = parts.length < 2 ? OperatingSystem.UNKNOWN : OperatingSystem.fromId(parts[0]);
        Architecture arch = parts.length < 2 ? Architecture.UNKNOWN : Architecture.fromId(parts[1]);
        LibC libC = parts.length == 3 ? LibC.fromId(parts[2]) : null;
        if (os == OperatingSystem.UNKNOWN || arch == Architecture.UNKNOWN || parts.length > 3 || libC == LibC.UNKNOWN) {
            throw new IllegalArgumentException("Unknown platform id " + id);
        }
        return new Platform(Inputs.NONE, os, arch, libC);
    }

    public OperatingSystem operatingSystem() {
        return operatingSystem.get();
    }

    /**
     * The JVM's architecture, which native libraries have to match.
     */
    public Architecture architecture() {
        return architecture.get();
    }

    /**
     * The architecture the hardware runs natively, which differs from {@link #architecture()} under a
     * {@link #translationLayer()}. May run {@code sysctl} on macOS or {@code reg} on Windows.
     */
    public Architecture machineArchitecture() {
        return machineArchitecture.get();
    }

    public TranslationLayer translationLayer() {
        return TranslationLayer.detect(this);
    }

    /**
     * The C library the JVM is linked against, read from {@code /proc/self/maps}.
     */
    public LibC libC() {
        return libC.get();
    }

    /**
     * The glibc version, such as {@code 2.39}, or empty when {@link #libC()} isn't glibc. Runs {@code getconf}.
     */
    public Optional<String> glibcVersion() {
        return glibcVersion.get();
    }

    /**
     * The distribution's {@code os-release}, or empty when the system has none.
     */
    public Optional<OsRelease> osRelease() {
        return osRelease.get();
    }

    public WSL wsl() {
        return wsl.get();
    }

    /**
     * True when Windows executables can run from WSL, such as {@code cmd.exe}. Users can turn interop off at any time,
     * so each call checks again.
     */
    public boolean hasWslInterop() {
        return WSL.hasInterop(this);
    }

    /**
     * {@link OperatingSystem#id()} and {@link Architecture#id()} joined by a hyphen, such as {@code windows-x64}.
     */
    public String id() {
        return operatingSystem().id() + "-" + architecture().id();
    }

    /**
     * {@link #id()} with a C library suffix, such as {@code linux-x64-musl}, for native libraries built per libc.
     *
     * @param libC usually {@link #libC()}, where {@link LibC#UNKNOWN} leaves the id as is
     */
    public String id(LibC libC) {
        return libC == LibC.UNKNOWN ? id() : id() + "-" + libC.id();
    }

    public boolean isWindows() {
        return operatingSystem().isWindows();
    }

    public boolean isMacOS() {
        return operatingSystem().isMacOS();
    }

    public boolean isLinux() {
        return operatingSystem().isLinux();
    }

    public boolean isAndroid() {
        return operatingSystem().isAndroid();
    }

    public boolean isBsd() {
        return operatingSystem().isBsd();
    }

    public boolean isUnixLike() {
        return operatingSystem().isUnixLike();
    }

    public boolean is64Bit() {
        return architecture().is64Bit();
    }

    public boolean isX86() {
        return architecture().isX86();
    }

    public boolean isArm() {
        return architecture().isArm();
    }

    public String executable(String name) {
        return operatingSystem().executable(name);
    }

    /**
     * Reads a system property through this platform's inputs, so code built on it stays testable too.
     *
     * @param key a property name such as {@code user.home}
     */
    public Optional<String> property(String key) {
        return Optional.ofNullable(inputs.properties.apply(key));
    }

    /**
     * Reads an environment variable through this platform's inputs.
     *
     * @param key a variable name such as {@code PATH}
     */
    public Optional<String> environment(String key) {
        return Optional.ofNullable(inputs.environment.apply(key));
    }

    /**
     * Reads a text file through this platform's inputs, or empty when it can't be read.
     *
     * @param path an absolute path
     */
    public Optional<List<String>> readLines(String path) {
        return Optional.ofNullable(inputs.files.apply(path));
    }

    /**
     * True when a regular file exists at the path.
     *
     * @param path an absolute path
     */
    public boolean exists(String path) {
        return inputs.fileExists.test(path);
    }

    /**
     * Runs a command through this platform's inputs, or empty when it fails to start or exits with an error.
     *
     * @param command the program and its arguments
     * @return the command's output, standard error included
     */
    public Optional<List<String>> run(String... command) {
        return Optional.ofNullable(inputs.commands.apply(Arrays.asList(command)));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Platform
                && ((Platform) other).operatingSystem() == operatingSystem()
                && ((Platform) other).architecture() == architecture();
    }

    @Override
    public int hashCode() {
        return 31 * operatingSystem().hashCode() + architecture().hashCode();
    }

    @Override
    public String toString() {
        return id();
    }

    /**
     * Builds a {@link Platform} from given inputs. The result keeps its own copy, so later builder calls don't change
     * it.
     */
    public static final class Builder {

        private final Map<String, String> properties = new HashMap<>();
        private final Map<String, String> environment = new HashMap<>();
        private final Map<String, List<String>> files = new HashMap<>();
        private final Map<List<String>, List<String>> commands = new HashMap<>();

        private Builder() { }

        public Builder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public Builder environment(Map<String, String> environment) {
            this.environment.putAll(environment);
            return this;
        }

        public Builder environment(String key, String value) {
            environment.put(key, value);
            return this;
        }

        /**
         * Adds a file, such as {@code /etc/os-release} or {@code /proc/self/maps}.
         *
         * @param path an absolute path
         * @param content the file's text
         */
        public Builder file(String path, String content) {
            files.put(path, Arrays.asList(content.split("\\R")));
            return this;
        }

        /**
         * Adds a command that exits successfully with the given output. Commands left out fail.
         *
         * @param command the program and its arguments, such as {@code [getconf, GNU_LIBC_VERSION]}
         * @param output the command's output
         */
        public Builder command(List<String> command, String output) {
            commands.put(new ArrayList<>(command), Arrays.asList(output.split("\\R")));
            return this;
        }

        public Platform build() {
            Map<String, String> properties = Collections.unmodifiableMap(new HashMap<>(this.properties));
            Map<String, String> environment = Collections.unmodifiableMap(new HashMap<>(this.environment));
            Map<String, List<String>> files = Collections.unmodifiableMap(new HashMap<>(this.files));
            Map<List<String>, List<String>> commands = Collections.unmodifiableMap(new HashMap<>(this.commands));
            Inputs inputs = new Inputs(properties::get, environment::get, files::get, files::containsKey,
                    commands::get);
            return new Platform(inputs, null, null, null);
        }

    }

    private static final class Inputs {

        static final Inputs SYSTEM = new Inputs(System::getProperty, System::getenv, Inputs::readFile,
                path -> Files.isRegularFile(Paths.get(path)), Inputs::runCommand);
        static final Inputs NONE = new Inputs(key -> null, key -> null, path -> null, path -> false, command -> null);

        final Function<String, String> properties;
        final Function<String, String> environment;
        final Function<String, List<String>> files;
        final Predicate<String> fileExists;
        final Function<List<String>, List<String>> commands;

        Inputs(Function<String, String> properties, Function<String, String> environment,
                Function<String, List<String>> files, Predicate<String> fileExists,
                Function<List<String>, List<String>> commands) {
            this.properties = properties;
            this.environment = environment;
            this.files = files;
            this.fileExists = fileExists;
            this.commands = commands;
        }

        private static List<String> readFile(String path) {
            try {
                return Arrays.asList(new String(Files.readAllBytes(Paths.get(path)),
                        StandardCharsets.UTF_8).split("\\R"));
            } catch (IOException e) {
                return null;
            }
        }

        private static List<String> runCommand(List<String> command) {
            try {
                Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
                List<String> lines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
                return process.waitFor() == 0 ? lines : null;
            } catch (IOException e) {
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

    }

    private static final class Lazy<T> {

        private Supplier<T> supplier;
        private volatile T value;

        Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        T get() {
            T result = value;
            if (result == null) {
                synchronized (this) {
                    result = value;
                    if (result == null) {
                        result = supplier.get();
                        value = result;
                        supplier = null;
                    }
                }
            }
            return result;
        }

    }

}
