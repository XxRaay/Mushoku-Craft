package com.mushokucraft.client.hud;

/**
 * Display modes for the mana HUD.
 * Configurable via mod settings.
 */
public enum ManaDisplayMode {
    /** Classic bar above the hotbar — full stats, numbers, school icon. */
    CLASSIC("options.mushokucraft.mana_display.classic"),

    /** Dynamic arc near the crosshair — clean screen, combat focus. */
    DYNAMIC("options.mushokucraft.mana_display.dynamic"),

    /** Hybrid — arc at crosshair for real-time + full bar above hotbar for stats. */
    HYBRID("options.mushokucraft.mana_display.hybrid"),

    /** Minimalist — UI hidden, fades in only when mana changes. */
    MINIMAL("options.mushokucraft.mana_display.minimal");

    private final String translationKey;

    ManaDisplayMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}





