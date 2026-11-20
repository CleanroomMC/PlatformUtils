/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The distribution described by {@code /etc/os-release}, present on most Linux distributions and on FreeBSD, read
 * through {@link Platform#osRelease()}.
 *
 * @see <a href="https://www.freedesktop.org/software/systemd/man/latest/os-release.html">os-release</a>
 */
public final class OsRelease {

    private final Map<String, String> fields;

    private OsRelease(Map<String, String> fields) {
        this.fields = Collections.unmodifiableMap(fields);
    }

    /**
     * Parses the lines of an os-release file.
     *
     * @param lines the {@code KEY=value} lines, comments and blank lines included
     */
    public static OsRelease parse(List<String> lines) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : lines) {
            line = line.trim();
            int separator = line.indexOf('=');
            if (line.startsWith("#") || separator <= 0) {
                continue;
            }
            fields.put(line.substring(0, separator), unquote(line.substring(separator + 1)));
        }
        return new OsRelease(fields);
    }

    static Optional<OsRelease> detect(Platform platform) {
        if (!platform.operatingSystem().isUnixLike()) {
            return Optional.empty();
        }
        for (String path : new String[] { "/etc/os-release", "/usr/lib/os-release" }) {
            Optional<List<String>> lines = platform.readLines(path);
            if (lines.isPresent()) {
                return lines.map(OsRelease::parse);
            }
        }
        return Optional.empty();
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '\'' && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.length() >= 2 && value.charAt(0) == '"' && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replaceAll("\\\\(.)", "$1");
    }

    /**
     * A lowercase distribution id such as {@code ubuntu}, {@code alpine} or {@code arch}, {@code linux} when unset.
     */
    public String id() {
        return fields.getOrDefault("ID", "linux");
    }

    /**
     * Ids of the distributions this one derives from, closest first, such as {@code [ubuntu, debian]} for Mint.
     */
    public List<String> idLike() {
        String idLike = fields.getOrDefault("ID_LIKE", "").trim();
        return idLike.isEmpty() ? Collections.emptyList() : Arrays.asList(idLike.split("\\s+"));
    }

    /**
     * True when this distribution is, or derives from, the distribution with the given id.
     *
     * @param id a distribution id such as {@code debian}
     */
    public boolean isLike(String id) {
        return id().equals(id) || idLike().contains(id);
    }

    /**
     * A version such as {@code 24.04} or {@code 3.20.3}, empty on rolling releases.
     */
    public Optional<String> versionId() {
        return get("VERSION_ID");
    }

    public String prettyName() {
        return fields.getOrDefault("PRETTY_NAME", "Linux");
    }

    /**
     * Any os-release field.
     *
     * @param key a field name such as {@code VERSION_CODENAME}
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(fields.get(key));
    }

    public Map<String, String> fields() {
        return fields;
    }

    @Override
    public String toString() {
        return prettyName();
    }

}
