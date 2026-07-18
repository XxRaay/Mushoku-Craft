/*
 * Decompiled with CFR 0.152.
 */
package com.mushokucraft.data;

import com.mushokucraft.config.MushokuConfig;

public class MasteryCalculator {
    public static float calculateEffectiveManaCost(float baseCost, float schoolMastery) {
        float reduction = schoolMastery * ((Double)MushokuConfig.MANA_COST_REDUCTION_PER_MASTERY.get()).floatValue();
        return Math.max(0.0f, baseCost - reduction);
    }

    public static double calculateSpellSuccessChance(float spellMastery, float schoolMastery) {
        return Math.min(1.0, 0.5 + (double)spellMastery * 0.1 + (double)schoolMastery * 0.05);
    }

    public static float calculateQteSpeedModifier(float schoolMastery) {
        return Math.max(0.2f, 1.0f - schoolMastery * ((Double)MushokuConfig.QTE_SPEED_REDUCTION_PER_MASTERY.get()).floatValue());
    }

    public static float calculateQteSizeModifier(float schoolMastery) {
        return 1.0f + schoolMastery * ((Double)MushokuConfig.QTE_SIZE_INCREASE_PER_MASTERY.get()).floatValue();
    }

    public static double calculateToukiDamage(float stanceMastery) {
        return (Double)MushokuConfig.TOUKI_DAMAGE_BASE.get() + (double)stanceMastery * (Double)MushokuConfig.TOUKI_DAMAGE_SCALING.get();
    }

    public static double calculateToukiKnockbackResistance(float stanceMastery) {
        return (Double)MushokuConfig.TOUKI_KB_RES_BASE.get() + (double)stanceMastery * (Double)MushokuConfig.TOUKI_KB_RES_SCALING.get();
    }

    public static double calculateToukiSpeed(float stanceMastery) {
        return (Double)MushokuConfig.TOUKI_SPEED_BASE.get() + (double)stanceMastery * (Double)MushokuConfig.TOUKI_SPEED_SCALING.get();
    }

    public static double calculateToukiJump(float stanceMastery) {
        return (Double)MushokuConfig.TOUKI_JUMP_BASE.get() + (double)stanceMastery * (Double)MushokuConfig.TOUKI_JUMP_SCALING.get();
    }
}

