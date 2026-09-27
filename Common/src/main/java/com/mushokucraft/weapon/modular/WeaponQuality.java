package com.mushokucraft.weapon.modular;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum WeaponQuality {
    TERRIBLE(
            "terrible",
            0.00f, 0.29f,
            0.75f,
            -0.20f,
            -0.40f,
            0.0f,
            ChatFormatting.DARK_RED,
            "⚠ Ужасное качество",
            "Неровная ковка и микротрещины (-25% урона, -20% скор. атаки, -40% прочности)"
    ),
    POOR(
            "poor",
            0.30f, 0.54f,
            0.90f,
            -0.05f,
            -0.15f,
            0.0f,
            ChatFormatting.RED,
            "⚠ Посредственное качество",
            "Грубая работа начинающего кузнеца (-10% урона, -15% прочности)"
    ),
    STANDARD(
            "standard",
            0.55f, 0.74f,
            1.00f,
            0.00f,
            0.00f,
            0.0f,
            ChatFormatting.WHITE,
            "✦ Добротное качество",
            "Надежное оружие без дефектов (Базовые характеристики)"
    ),
    EXQUISITE(
            "exquisite",
            0.75f, 0.89f,
            1.18f,
            +0.10f,
            +0.25f,
            +0.2f,
            ChatFormatting.AQUA,
            "✦ Превосходное качество",
            "Тонкая работа мастера (+18% урона, +10% скор. атаки, +25% прочности, +0.2 дальности)"
    ),
    MASTERPIECE(
            "masterpiece",
            0.90f, 1.00f,
            1.35f,
            +0.20f,
            +0.50f,
            +0.5f,
            ChatFormatting.GOLD,
            "★ Шедевр кузнеца ★",
            "Вершина кузнечного мастерства! (+35% урона, +20% скор. атаки, +50% прочности, +0.5 дальности)"
    );

    private final String id;
    private final float minScore;
    private final float maxScore;
    private final float damageMultiplier;
    private final float speedBonus;
    private final float durabilityBonus;
    private final float reachBonus;
    private final ChatFormatting color;
    private final String defaultTitle;
    private final String defaultDesc;

    WeaponQuality(
            String id,
            float minScore,
            float maxScore,
            float damageMultiplier,
            float speedBonus,
            float durabilityBonus,
            float reachBonus,
            ChatFormatting color,
            String defaultTitle,
            String defaultDesc
    ) {
        this.id = id;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.damageMultiplier = damageMultiplier;
        this.speedBonus = speedBonus;
        this.durabilityBonus = durabilityBonus;
        this.reachBonus = reachBonus;
        this.color = color;
        this.defaultTitle = defaultTitle;
        this.defaultDesc = defaultDesc;
    }

    public String getId() {
        return id;
    }

    public float getMinScore() {
        return minScore;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getSpeedBonus() {
        return speedBonus;
    }

    public float getDurabilityBonus() {
        return durabilityBonus;
    }

    public float getReachBonus() {
        return reachBonus;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public Component getTitleComponent() {
        return Component.literal(defaultTitle).withStyle(color, this == MASTERPIECE ? ChatFormatting.BOLD : ChatFormatting.RESET);
    }

    public Component getDescComponent() {
        return Component.literal(defaultDesc).withStyle(ChatFormatting.GRAY);
    }

    public static WeaponQuality fromScore(float score) {
        float clamped = Math.clamp(score, 0.0f, 1.0f);
        if (clamped >= 0.90f) return MASTERPIECE;
        if (clamped >= 0.75f) return EXQUISITE;
        if (clamped >= 0.55f) return STANDARD;
        if (clamped >= 0.30f) return POOR;
        return TERRIBLE;
    }

    public static WeaponQuality fromMultiplier(float multiplier) {
        if (multiplier >= 1.25f) return MASTERPIECE;
        if (multiplier >= 1.10f) return EXQUISITE;
        if (multiplier >= 0.95f) return STANDARD;
        if (multiplier >= 0.80f) return POOR;
        return TERRIBLE;
    }

    public static WeaponQuality byId(String id) {
        for (WeaponQuality q : values()) {
            if (q.id.equalsIgnoreCase(id)) return q;
        }
        return STANDARD;
    }
}
