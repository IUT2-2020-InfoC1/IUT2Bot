package com.jesus_crie.iut2_bot.timetable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum IUTGroup {

    A1("INFO1A1", "a1"),
    A2("INFO1A2", "a2"),
    B1("INFO1B1", "b1"),
    B2("INFO1B2", "b2"),
    C1("INFO1C1", "c1"),
    C2("INFO1C2", "c2"),
    D1("INFO1D1", "d1"),
    D2("INFO1D2", "d2"),

    NONE(null, null);

    private final String name;
    private final String shortName;

    IUTGroup(@Nullable final String name, @Nullable final String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    @Nullable
    public static IUTGroup fromShortName(@Nonnull final String name) {
        for (IUTGroup group : values()) {
            if (group.shortName.equalsIgnoreCase(name))
                return group;
        }

        return null;
    }

    @Nonnull
    public static IUTGroup fromName(@Nonnull final String name) {
        for (IUTGroup group : values()) {
            if (group.name.equalsIgnoreCase(name))
                return group;
        }

        return NONE;
    }
}
