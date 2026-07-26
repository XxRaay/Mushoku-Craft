package com.mushokucraft.magic;

import com.mushokucraft.config.MushokuConfig;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a specific spell that can be learned, cast, and mastered.
 * Uses a Builder pattern for flexible construction of projectile or action-based spells.
 */
public class Spell {
    private final ResourceLocation id;
    private final MagicSchool school;
    private final SpellRank rank;
    private final float baseCastTimeTicks;
    private final float baseManaCost;
    private final float baseFizzleChance;
    private final String incantationKey;

    @FunctionalInterface
    public interface ProjectileFactory {
        net.minecraft.world.entity.projectile.Projectile create(net.minecraft.world.level.Level level, net.minecraft.server.level.ServerPlayer player);
    }

    private final ProjectileFactory projectileFactory;
    private final SpellAction spellAction;
    private final float baseSpeed;
    private final float baseInaccuracy;
    private final boolean channeled;

    private Spell(Builder builder) {
        this.id = builder.id;
        this.school = builder.school;
        this.rank = builder.rank;
        this.baseCastTimeTicks = builder.baseCastTimeTicks;
        this.baseManaCost = builder.baseManaCost;
        this.baseFizzleChance = builder.baseFizzleChance;
        this.incantationKey = builder.incantationKey;
        this.projectileFactory = builder.projectileFactory;
        this.spellAction = builder.spellAction;
        this.baseSpeed = builder.baseSpeed;
        this.baseInaccuracy = builder.baseInaccuracy;
        this.channeled = builder.channeled;
    }

    public ResourceLocation getId() { return id; }
    public MagicSchool getSchool() { return school; }
    public SpellRank getRank() { return rank; }
    public float getBaseCastTimeTicks() { return baseCastTimeTicks; }
    public float getBaseManaCost() { return baseManaCost; }
    public float getBaseFizzleChance() { return baseFizzleChance; }
    public String getIncantationKey() { return incantationKey; }
    public ProjectileFactory getProjectileFactory() { return projectileFactory; }
    public SpellAction getSpellAction() { return spellAction; }
    public float getBaseSpeed() { return baseSpeed; }
    public float getBaseInaccuracy() { return baseInaccuracy; }
    public boolean isChanneled() { return channeled; }

    public float getCastTime(float mastery) {
        if (mastery >= 1.0f) return 0f; // Silent Casting
        return baseCastTimeTicks * (1.0f - mastery);
    }

    public float getFizzleChance(float mastery, com.mushokucraft.data.PlayerMasteryData data) {
        if (mastery >= 1.0f) return 0f;
        float chance = baseFizzleChance * (1.0f - mastery);
        if (data != null) {
            chance += com.mushokucraft.data.MasteryCalculator.calculateTierPenalty(this, data);
        }
        return Math.min(1.0f, chance);
    }

    public float getEffectiveManaCost(com.mushokucraft.data.PlayerMasteryData data) {
        float schoolMastery = data.getSchoolMastery(school);
        return com.mushokucraft.data.MasteryCalculator.calculateEffectiveManaCost(baseManaCost, schoolMastery);
    }

    public boolean canCharge(float mastery) {
        return mastery >= 1.0f;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final MagicSchool school;
        private final SpellRank rank;
        private float baseCastTimeTicks = 0f;
        private float baseManaCost = 0f;
        private float baseFizzleChance = 0f;
        private String incantationKey = "";
        private ProjectileFactory projectileFactory = null;
        private SpellAction spellAction = null;
        private float baseSpeed = 0f;
        private float baseInaccuracy = 0f;
        private boolean channeled = false;

        public Builder(ResourceLocation id, MagicSchool school, SpellRank rank) {
            this.id = id;
            this.school = school;
            this.rank = rank;
        }

        public Builder castTime(float ticks) { this.baseCastTimeTicks = ticks; return this; }
        public Builder manaCost(float cost) { this.baseManaCost = cost; return this; }
        public Builder fizzleChance(float chance) { this.baseFizzleChance = chance; return this; }
        public Builder incantation(String key) { this.incantationKey = key; return this; }
        
        public Builder projectile(ProjectileFactory factory, float speed, float inaccuracy) {
            this.projectileFactory = factory;
            this.baseSpeed = speed;
            this.baseInaccuracy = inaccuracy;
            this.spellAction = new ProjectileSpellAction(); // Default strategy
            return this;
        }
        
        public Builder action(SpellAction action) {
            this.spellAction = action;
            return this;
        }

        public Builder channeled(boolean channeled) {
            this.channeled = channeled;
            return this;
        }

        public Spell build() { return new Spell(this); }
    }
}





