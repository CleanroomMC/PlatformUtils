package com.cleanroommc.platformutils.windows;

import java.util.Arrays;
import java.util.Collection;

public class QueryResult {

    static QueryResult.Entry parse(HKey hKey, String key, String[] split) {
        if (key == null) {
            throw new IllegalArgumentException(String.format("Unable to parse QueryResult with a null key: {%s}", Arrays.toString(split)));
        }
        if (split.length != 4) {
            throw new IllegalArgumentException(String.format("Unable to parse QueryResult from: {%s}", Arrays.toString(split)));
        }
        return new QueryResult.Entry(hKey, key, split[1], HRegistryValueType.valueOf(split[2]), split[3]);
    }

    static QueryResult success(Collection<QueryResult.Entry> entries) {
        return new QueryResult(entries);
    }

    static QueryResult error(String message) {
        return new QueryResult(message);
    }

    private final Collection<QueryResult.Entry> entries;
    private final String message;

    private QueryResult(Collection<QueryResult.Entry> entries) {
        this.entries = entries;
        this.message = null;
    }

    private QueryResult(String message) {
        this.entries = null;
        this.message = message;
    }

    public boolean successful() {
        return this.entries != null;
    }

    public boolean errored() {
        return this.message != null;
    }

    public Collection<QueryResult.Entry> entries() {
        return this.entries;
    }

    public static class Entry {

        private final HKey hKey;
        private final String key, valueKey, valueString;
        private final HRegistryValueType valueType;

        private Entry(HKey hKey, String key, String valueKey, HRegistryValueType valueType, String valueString) {
            this.hKey = hKey;
            this.key = key;
            this.valueKey = valueKey;
            this.valueType = valueType;
            this.valueString = valueString;
        }

        public HKey hKey() {
            return hKey;
        }

        public String key() {
            return key;
        }

        public String valueKey() {
            return valueKey;
        }

        public HRegistryValueType valueType() {
            return valueType;
        }

        public String value() {
            return valueString;
        }

        @Override
        public String toString() {
            return "QueryResult$Entry{" +
                    "hKey=" + hKey +
                    ", key='" + key + '\'' +
                    ", valueKey='" + valueKey + '\'' +
                    ", valueString='" + valueString + '\'' +
                    ", valueType=" + valueType +
                    '}';
        }

    }


}
