package com.cleanroommc.platformutils.windows;

public enum HKey {

    HKEY_LOCAL_MACHINE("HKLM"),
    HKEY_CURRENT_USER("HKCU"),
    HKEY_CLASSES_ROOT("HKCR"),
    HKEY_USERS("HKU"),
    HKEY_CURRENT_CONFIG("HKCC");

    private final String abbreviation;

    HKey(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

}
