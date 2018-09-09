package com.jesus_crie.iut2_bot.timetable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum IUTGroup {

    A1("INFO1A1", "A1", 0x1c5212),
    A2("INFO1A2", "A2", 0x9b59b6),
    B1("INFO1B1", "B1", 0x2ecc71),
    B2("INFO1B2", "B2", 0xc50000),
    C1("INFO1C1", "C1", 0x3498db),
    C2("INFO1C2", "C2", 0xffffff),
    D1("INFO1D1", "D1", 0xf1c40f),
    D2("INFO1D2", "D2", 0xc48800),

    NONE(null, null, 0xffffff);

    private final String name;
    private final String shortName;
    private final int color;

    IUTGroup(@Nullable final String name, @Nullable final String shortName, int color) {
        this.name = name;
        this.shortName = shortName;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public int getColor() {
        return color;
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
