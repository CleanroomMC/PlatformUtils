package com.cleanroommc.platformutils.windows;

import java.util.Arrays;

public class QueryResult {

    static QueryResult parse(HKey hKey, String key, String[] split) {
        if (key == null) {
            throw new IllegalArgumentException(String.format("Unable to parse QueryResult: {%s}", Arrays.toString(split)));
        }
        if (split.length != 4) {
            throw new IllegalArgumentException(String.format("Unable to parse QueryResult: {%s}", Arrays.toString(split)));
        }
        return new QueryResult(hKey, key, split[1], HRegistryValueType.valueOf(split[2]), split[3]);
    }

    private final HKey hKey;
    private final String key, valueKey, valueString;
    private final HRegistryValueType valueType;

    private QueryResult(HKey hKey, String key, String valueKey, HRegistryValueType valueType, String valueString) {
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
        return "QueryResult{" +
                "hKey=" + hKey +
                ", key='" + key + '\'' +
                ", valueKey='" + valueKey + '\'' +
                ", valueString='" + valueString + '\'' +
                ", valueType=" + valueType +
                '}';
    }
}
