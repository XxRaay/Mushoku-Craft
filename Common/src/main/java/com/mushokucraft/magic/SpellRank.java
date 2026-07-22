package com.mushokucraft.magic;

/**
 * Represents the rank/tier of a spell.
 * Higher ranks give more school XP but require more mana and have longer cast times.
 */
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

    SpellRank(String id, int tier, float schoolXpMultiplier) {
        this.id = id;
        this.tier = tier;
        this.schoolXpMultiplier = schoolXpMultiplier;
    }

    public String getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    /**
     * Higher rank spells contribute more XP to the school mastery.
     */
    public float getSchoolXpMultiplier() {
        return schoolXpMultiplier;
    }
}





