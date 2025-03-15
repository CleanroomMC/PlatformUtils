package com.cleanroommc.platformutils.windows;

public final class QueryParameter {

    public static QueryParameter valueFilter(HRegistryValueType valueType) {
        return new QueryParameter("/t", valueType.toString());
    }

    public static QueryParameter recursive() {
        return new QueryParameter("/s");
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
