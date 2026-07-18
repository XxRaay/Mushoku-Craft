package com.mushokucraft.data;

import com.mushokucraft.config.MushokuConfig;

/**
 * Centralized calculator for all mastery-derived stats and formulas.
 * Provides a single source of truth for progression scaling.
 */
public class MasteryCalculator {

    // --- Magic Mastery Calculations ---

    public static float calculateEffectiveManaCost(float baseCost, float schoolMastery) {
        float reduction = schoolMastery * MushokuConfig.MANA_COST_REDUCTION_PER_MASTERY.get().floatValue();
        return Math.max(0, baseCost - reduction);
    }

    public static double calculateSpellSuccessChance(float spellMastery, float schoolMastery) {
        // Base 50% chance, up to +10% from spell mastery, up to +5% from school mastery
        return Math.min(1.0, 0.50 + spellMastery * 0.10 + schoolMastery * 0.05);
    }

    public static float calculateQteSpeedModifier(float schoolMastery) {
        return Math.max(0.2f, 1.0f - (schoolMastery * MushokuConfig.QTE_SPEED_REDUCTION_PER_MASTERY.get().floatValue()));
    }

    public static float calculateQteSizeModifier(float schoolMastery) {
        return 1.0f + (schoolMastery * MushokuConfig.QTE_SIZE_INCREASE_PER_MASTERY.get().floatValue());
    }

    // --- Sword Mastery Calculations (Touki) ---

    public static double calculateToukiDamage(float stanceMastery) {
        return MushokuConfig.TOUKI_DAMAGE_BASE.get() + (stanceMastery * MushokuConfig.TOUKI_DAMAGE_SCALING.get());
    }

    public static double calculateToukiKnockbackResistance(float stanceMastery) {
        return MushokuConfig.TOUKI_KB_RES_BASE.get() + (stanceMastery * MushokuConfig.TOUKI_KB_RES_SCALING.get());
    }

    public static double calculateToukiSpeed(float stanceMastery) {
        return MushokuConfig.TOUKI_SPEED_BASE.get() + (stanceMastery * MushokuConfig.TOUKI_SPEED_SCALING.get());
    }

    public static double calculateToukiJump(float stanceMastery) {
        return MushokuConfig.TOUKI_JUMP_BASE.get() + (stanceMastery * MushokuConfig.TOUKI_JUMP_SCALING.get());
    }
}





