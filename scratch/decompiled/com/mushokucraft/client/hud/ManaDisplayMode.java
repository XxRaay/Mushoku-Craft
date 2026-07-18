/*
 * Decompiled with CFR 0.152.
 */
package com.mushokucraft.client.hud;

public enum ManaDisplayMode {
    CLASSIC("options.mushokucraft.mana_display.classic"),
    DYNAMIC("options.mushokucraft.mana_display.dynamic"),
    HYBRID("options.mushokucraft.mana_display.hybrid"),
    MINIMAL("options.mushokucraft.mana_display.minimal");

    private final String translationKey;

    private ManaDisplayMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }
}

