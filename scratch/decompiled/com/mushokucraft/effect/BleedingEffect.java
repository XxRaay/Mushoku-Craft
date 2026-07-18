/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.LivingEntity
 */
package com.mushokucraft.effect;

import com.mushokucraft.config.MushokuConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect
extends MobEffect {
    public BleedingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().magic(), ((Double)MushokuConfig.BLEEDING_BASE_DAMAGE.get()).floatValue() + (float)amplifier);
        return true;
    }

    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = (Integer)MushokuConfig.BLEEDING_TICK_INTERVAL.get() >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        }
        return true;
    }
}

