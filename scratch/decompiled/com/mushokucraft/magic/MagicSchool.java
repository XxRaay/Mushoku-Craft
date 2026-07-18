/*
 * Decompiled with CFR 0.152.
 */
package com.mushokucraft.magic;

public enum MagicSchool {
    WATER("water", 3447003),
    FIRE("fire", 15158332),
    EARTH("earth", 9127187),
    WIND("wind", 3066993),
    SWORD_ARTS("sword_arts", 0xCCCCCC);

    private final String id;
    private final int color;

    private MagicSchool(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return this.id;
    }

    public int getColor() {
        return this.color;
    }
}

