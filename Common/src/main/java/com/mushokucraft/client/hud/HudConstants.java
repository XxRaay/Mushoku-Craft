package com.mushokucraft.client.hud;

/**
 * Centralized constants for all HUD overlays in MushokuCraft.
 * Defines positioning, colors, and animation timings to ensure a consistent UI.
 */
public class HudConstants {

    // --- Global Positioning ---
    // Offsets from the bottom of the screen
    public static final int MASTERY_Y_OFFSET_FROM_BOTTOM = 85;
    public static final int INCANTATION_Y_OFFSET_FROM_BOTTOM = 80;
    public static final int MANA_BAR_Y_OFFSET_FROM_BOTTOM = 54;

    // --- Mastery Overlay ---
    public static final int MASTERY_SHOW_TICKS = 40; // 2 seconds
    public static final int MASTERY_TEXT_COLOR = 0xFFFFFF;
    public static final int MASTERY_FADE_START_TICK = 10;

    // --- Incantation Overlay ---
    public static final int INCANTATION_FADE_TICKS = 40;
    public static final int INCANTATION_COLOR_NORMAL = 0xAAAAAA;
    public static final int INCANTATION_COLOR_FIZZLED = 0xFF0000;

    // --- QTE Overlay ---
    public static final float QTE_CENTRAL_RADIUS_MULT = 15.0f;
    public static final float QTE_GREEN_RADIUS_MULT = 5.0f;
    public static final float QTE_SHRINKING_BASE_RADIUS = 30.0f;
    public static final float QTE_PERFECT_MULTIPLIER = 0.3f;
    
    public static final int QTE_COLOR_CENTRAL = 0xFFFFFFFF;
    public static final int QTE_COLOR_GREEN = 0xFF00FF00;
    public static final int QTE_COLOR_SHRINK_DEFAULT = 0xFFFFAA00;
    public static final int QTE_COLOR_SHRINKING_GOOD = 0xFF55FF55;
    public static final int QTE_COLOR_SHRINKING_PERFECT = 0xFF00FFFF;
    public static final int QTE_COLOR_TEXT = 0xFFFFFFFF;
}





