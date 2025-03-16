package com.cleanroommc.platformutils.windows;

/**
 * <a href="https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/reg-query">reg query documentation</a>
 */
public final class QueryParameter {

    public static QueryParameter valueName(String valueName) {
        return new QueryParameter("/v", valueName);
    }

    public static QueryParameter emptyValues() {
        return new QueryParameter("/ve");
    }

    public static QueryParameter recursive() {
        return new QueryParameter("/s");
    }

    public static QueryParameter valueWithNullSeparator() {
        return new QueryParameter("/se");
    }

    public static QueryParameter valueWithSeparator(String separator) {
        return new QueryParameter("/se", separator);
    }

    public static QueryParameter pattern(String pattern) {
        return new QueryParameter("/f", pattern);
    }

    public static QueryParameter key() {
        return new QueryParameter("/f");
    }

    public static QueryParameter data() {
        return new QueryParameter("/d");
    }

    public static QueryParameter caseSensitive() {
        return new QueryParameter("/c");
    }

    public static QueryParameter exactMatches() {
        return new QueryParameter("/e");
    }

    public static QueryParameter valueFilter(HRegistryValueType valueType) {
        return new QueryParameter("/t", valueType.toString());
    }

    private final String parameter;
    private final String parameterArgument;

    private QueryParameter(String parameter) {
        this.parameter = parameter;
        this.parameterArgument = null;
    }

    private QueryParameter(String parameter, String parameterArgument) {
        this.parameter = parameter;
        this.parameterArgument = parameterArgument;
    }

    @Override
    public String toString() {
        if (this.parameterArgument == null) {
            return this.parameter;
        }
        return this.parameter + " " + this.parameterArgument;
    }

}
