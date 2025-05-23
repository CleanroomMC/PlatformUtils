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

    public static QueryResult query(HKey hKey, String key, QueryParameter... parameters) {
        return buildAndParseQuery(hKey, key, parameters);
    }

    private static QueryResult buildAndParseQuery(HKey hKey, String key, QueryParameter... parameters) {
        List<String> results;
        try {
            results = buildQuery(hKey, key, parameters);
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
            if (firstSplit.startsWith(hKey.toString())) {
                currentKey = split[0].substring(split[0].indexOf('\\') + 1);
            } else if (firstSplit.isEmpty() && split.length == 4) {
                entries.add(QueryResult.parse(hKey, currentKey, split));
            }
        }

        return QueryResult.success(entries);
    }

    private static List<String> buildQuery(HKey hKey, String key, QueryParameter... parameters) throws IOException, InterruptedException {
        key = normalizeKey(key);

        String compiledParameters = Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining(" "));
        String query = String.format("reg query \"%s\\%s\" %s", hKey.getAbbreviation(), key, compiledParameters);

        Process process = Runtime.getRuntime().exec(query);
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        process.waitFor();

        List<String> result = new ArrayList<>();
        String str;
        while ((str = err.readLine()) != null) {
            result.add(str);
        }
        if (!result.isEmpty()) {
            throw new IOException(String.join("\n", result));
        }
        while ((str = in.readLine()) != null) {
            result.add(str);
        }
        return result;
    }

    private static String normalizeKey(String key) {
        return key.replace("/", "\\");
    }

    private WindowsRegistry() { }

}
