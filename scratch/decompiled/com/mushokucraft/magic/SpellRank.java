/*
 * Decompiled with CFR 0.152.
 */
package com.mushokucraft.magic;

public enum SpellRank {
    ELEMENTARY("elementary", 1, 1.0f),
    INTERMEDIATE("intermediate", 2, 1.5f),
    ADVANCED("advanced", 3, 2.0f),
    SAINT("saint", 4, 3.0f),
    KING("king", 5, 4.0f),
    EMPEROR("emperor", 6, 5.0f),
    GOD("god", 7, 7.0f);

    private final String id;
    private final int tier;
    private final float schoolXpMultiplier;

    private SpellRank(String id, int tier, float schoolXpMultiplier) {
        this.id = id;
        this.tier = tier;
        this.schoolXpMultiplier = schoolXpMultiplier;
    }

    public String getId() {
        return this.id;
    }

    public int getTier() {
        return this.tier;
    }

    public float getSchoolXpMultiplier() {
        return this.schoolXpMultiplier;
    }
}

