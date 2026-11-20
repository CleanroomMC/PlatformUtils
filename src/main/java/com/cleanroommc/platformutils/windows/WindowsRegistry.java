/*
 * Copyright (c) 2025-2026 CleanroomMC contributors
 * SPDX-License-Identifier: MIT
 */

package com.cleanroommc.platformutils.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class to query Windows' Registry.
 */
public final class WindowsRegistry {

    private WindowsRegistry() { }

    public static QueryResult query(HKey hkey, String key, QueryParameter... parameters) {
        return buildAndParseQuery(hkey, key, parameters);
    }

    private static QueryResult buildAndParseQuery(HKey hkey, String key, QueryParameter... parameters) {
        List<String> results;
        try {
            results = buildQuery(hkey, key, parameters);
        } catch (IOException e) {
            return QueryResult.error(e.getMessage());
        } catch (InterruptedException e) {
            throw new IllegalStateException("Unable to query!", e);
        }

        List<QueryResult.Entry> entries = new ArrayList<>();

        if (results.isEmpty() || results.stream().map(String::trim).allMatch(String::isEmpty)) {
            return QueryResult.success(Collections.emptyList());
        }

        String currentKey = null;
        for (String result : results) {
            String[] split = result.split(" {4}");
            if (split.length == 0) {
                continue;
            }
            String firstSplit = split[0];
            if (firstSplit.startsWith(hkey.toString())) {
                currentKey = split[0].substring(split[0].indexOf('\\') + 1);
            } else if (firstSplit.isEmpty() && split.length == 4) {
                entries.add(QueryResult.parse(hkey, currentKey, split));
            }
        }

        return QueryResult.success(entries);
    }

    private static List<String> buildQuery(HKey hkey, String key, QueryParameter... parameters) throws IOException,
            InterruptedException {
        key = normalizeKey(key);

        String compiledParameters = Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining(" "));
        String query = String.format("reg query \"%s\\%s\" %s", hkey.getAbbreviation(), key, compiledParameters);

        Process process = Runtime.getRuntime().exec(query);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // Drain the output before waiting, as reg blocks once the pipe fills. reg only writes a short line to stderr,
        // so reading it second can't fill its pipe.

        List<String> result = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        String str;
        while ((str = in.readLine()) != null) {
            result.add(str);
        }
        while ((str = err.readLine()) != null) {
            errors.add(str);
        }
        process.waitFor();

        if (!errors.isEmpty()) {
            throw new IOException(String.join("\n", errors));
        }
        return result;
    }

    private static String normalizeKey(String key) {
        return key.replace("/", "\\");
    }

}
