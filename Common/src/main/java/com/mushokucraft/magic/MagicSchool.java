package com.mushokucraft.magic;

/**
 * Represents a school of magic (Water, Fire, Earth, Wind).
 * Each school has its own mastery level that reduces mana costs.
 */
public enum MagicSchool {
    WATER("water", 0x3498DB),
    FIRE("fire", 0xE74C3C),
    EARTH("earth", 0x8B4513),
    WIND("wind", 0x2ECC71),
    SWORD_ARTS("sword_arts", 0xCCCCCC);

    private final String id;
    private final int color;

    MagicSchool(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public int getColor() {
        return color;
    }
}





