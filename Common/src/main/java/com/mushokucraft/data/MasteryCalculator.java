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

    public static float calculateQteSpeedModifier(float schoolMastery, int spellTier) {
        float baseSpeed = 1.0f + (spellTier - 1) * 0.2f;
        return Math.max(0.2f, baseSpeed - (schoolMastery * MushokuConfig.QTE_SPEED_REDUCTION_PER_MASTERY.get().floatValue()));
    }

    public static float calculateQteTargetSize(int spellTier) {
        // Base size is 1.0, decreases by 0.1 for each tier above 1
        return Math.max(0.3f, 1.0f - (spellTier - 1) * 0.1f);
    }

    public static float calculateQteSizeModifier(float schoolMastery) {
        return 1.0f + (schoolMastery * MushokuConfig.QTE_SIZE_INCREASE_PER_MASTERY.get().floatValue());
    }

    public static float calculateTierPenalty(com.mushokucraft.magic.Spell targetSpell, PlayerMasteryData data) {
        float spellMastery = data.getSpellMastery(targetSpell.getId());
        if (spellMastery > 0f) {
            return 0f; // No penalty if already learned
        }
        
        int maxLearnedTier = 0;
        for (com.mushokucraft.magic.Spell s : com.mushokucraft.init.ModSpells.SPELLS.values()) {
            if (s.getSchool() == targetSpell.getSchool() && data.getSpellMastery(s.getId()) > 0f) {
                if (s.getRank().getTier() > maxLearnedTier) {
                    maxLearnedTier = s.getRank().getTier();
                }
            }
        }
        
        int tierGap = targetSpell.getRank().getTier() - maxLearnedTier;
        if (tierGap == 2) {
            return 0.5f; // 50% penalty for skipping one tier
        } else if (tierGap >= 3) {
            return 1.0f; // 100% penalty for skipping two or more tiers
        }
        return 0f;
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





