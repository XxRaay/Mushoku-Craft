/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 */
package com.mushokucraft.magic;

import com.mushokucraft.data.MasteryCalculator;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.magic.MagicSchool;
import com.mushokucraft.magic.ProjectileSpellAction;
import com.mushokucraft.magic.SpellAction;
import com.mushokucraft.magic.SpellRank;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class Spell {
    private final ResourceLocation id;
    private final MagicSchool school;
    private final SpellRank rank;
    private final float baseCastTimeTicks;
    private final float baseManaCost;
    private final float baseFizzleChance;
    private final String incantationKey;
    private final ProjectileFactory projectileFactory;
    private final SpellAction spellAction;
    private final float baseSpeed;
    private final float baseInaccuracy;

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
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public MagicSchool getSchool() {
        return this.school;
    }

    public SpellRank getRank() {
        return this.rank;
    }

    public float getBaseCastTimeTicks() {
        return this.baseCastTimeTicks;
    }

    public float getBaseManaCost() {
        return this.baseManaCost;
    }

    public float getBaseFizzleChance() {
        return this.baseFizzleChance;
    }

    public String getIncantationKey() {
        return this.incantationKey;
    }

    public ProjectileFactory getProjectileFactory() {
        return this.projectileFactory;
    }

    public SpellAction getSpellAction() {
        return this.spellAction;
    }

    public float getBaseSpeed() {
        return this.baseSpeed;
    }

    public float getBaseInaccuracy() {
        return this.baseInaccuracy;
    }

    public float getCastTime(float mastery) {
        if (mastery >= 1.0f) {
            return 0.0f;
        }
        return this.baseCastTimeTicks * (1.0f - mastery);
    }

    public float getFizzleChance(float mastery) {
        if (mastery >= 1.0f) {
            return 0.0f;
        }
        return this.baseFizzleChance * (1.0f - mastery);
    }

    public float getEffectiveManaCost(PlayerMasteryData data) {
        float schoolMastery = data.getSchoolMastery(this.school);
        return MasteryCalculator.calculateEffectiveManaCost(this.baseManaCost, schoolMastery);
    }

    public boolean canCharge(float mastery) {
        return mastery >= 1.0f;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final MagicSchool school;
        private final SpellRank rank;
        private float baseCastTimeTicks = 0.0f;
        private float baseManaCost = 0.0f;
        private float baseFizzleChance = 0.0f;
        private String incantationKey = "";
        private ProjectileFactory projectileFactory = null;
        private SpellAction spellAction = null;
        private float baseSpeed = 0.0f;
        private float baseInaccuracy = 0.0f;

        public Builder(ResourceLocation id, MagicSchool school, SpellRank rank) {
            this.id = id;
            this.school = school;
            this.rank = rank;
        }

        public Builder castTime(float ticks) {
            this.baseCastTimeTicks = ticks;
            return this;
        }

        public Builder manaCost(float cost) {
            this.baseManaCost = cost;
            return this;
        }

        public Builder fizzleChance(float chance) {
            this.baseFizzleChance = chance;
            return this;
        }

        public Builder incantation(String key) {
            this.incantationKey = key;
            return this;
        }

        public Builder projectile(ProjectileFactory factory, float speed, float inaccuracy) {
            this.projectileFactory = factory;
            this.baseSpeed = speed;
            this.baseInaccuracy = inaccuracy;
            this.spellAction = new ProjectileSpellAction();
            return this;
        }

        public Builder action(SpellAction action) {
            this.spellAction = action;
            return this;
        }

        public Spell build() {
            return new Spell(this);
        }
    }

    @FunctionalInterface
    public static interface ProjectileFactory {
        public Projectile create(Level var1, ServerPlayer var2);
    }
}

