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

    // --- Mana Progression (Mushoku Tensei Anime System) ---

    public static float calculateMaxManaGrowth(float manaSpent, com.mushokucraft.magic.SpellRank rank, float currentMana, float currentMaxMana) {
        if (manaSpent <= 0f) {
            return 0f;
        }

        double baseRate = MushokuConfig.MANA_GROWTH_RATE.get();
        if (baseRate <= 0.0) {
            return 0f;
        }

        // 1. Tier multiplier: higher rank spells stretch mana channels more effectively
        float tierMultiplier = rank != null ? rank.getSchoolXpMultiplier() : 1.0f;

        // 2. Exhaustion multiplier (Limit Break / Overexertion):
        // In Mushoku Tensei, draining mana to near zero or completely emptying the pool
        // causes mana veins to adapt and expand much faster.
        float ratio = currentMaxMana > 0f ? (currentMana / currentMaxMana) : 1.0f;
        double exhaustionMultiplier = 1.0;
        double threshold = MushokuConfig.MANA_EXHAUSTION_THRESHOLD.get();
        if (ratio <= threshold) {
            double exhaustionFactor = (threshold - Math.max(0.0, ratio)) / Math.max(0.01, threshold);
            exhaustionMultiplier = 1.0 + exhaustionFactor * (MushokuConfig.MANA_EXHAUSTION_BONUS_MULT.get() - 1.0);
        }

        // 3. Progressive soft-scaling curve (decay):
        double decayFactor = 1.0;
        if (MushokuConfig.ENABLE_MANA_GROWTH_DECAY.get()) {
            double defaultMax = MushokuConfig.DEFAULT_MAX_MANA.get();
            if (currentMaxMana > defaultMax) {
                decayFactor = Math.pow(defaultMax / currentMaxMana, MushokuConfig.MANA_GROWTH_DECAY_POWER.get());
                decayFactor = Math.max(MushokuConfig.MIN_MANA_GROWTH_FACTOR.get(), decayFactor);
            }
        }

        float growth = (float) (manaSpent * baseRate * tierMultiplier * exhaustionMultiplier * decayFactor);

        // 4. Upper Cap enforcement
        double cap = MushokuConfig.MAX_MANA_CAP.get();
        if (cap > 0.0 && currentMaxMana + growth > cap) {
            growth = (float) Math.max(0.0, cap - currentMaxMana);
        }

        return Math.max(0f, growth);
    }

    public static float calculateManaRegenPerSecond(float baseRegenRatePerTick, float currentMaxMana) {
        float baseRegenPerSec = baseRegenRatePerTick * 20.0f;
        double defaultMax = MushokuConfig.DEFAULT_MAX_MANA.get();
        double extraMaxMana = Math.max(0.0, currentMaxMana - defaultMax);

        // Subtle increase from max mana, leaving accessories as the primary source of regen
        double bonusFromMaxMana = extraMaxMana * MushokuConfig.MANA_REGEN_FROM_MAX_MANA_RATE.get();
        double cap = MushokuConfig.MANA_REGEN_FROM_MAX_MANA_CAP.get();
        if (cap > 0.0) {
            bonusFromMaxMana = Math.min(cap, bonusFromMaxMana);
        }

        return baseRegenPerSec + (float) bonusFromMaxMana;
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





