/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package com.mushokucraft.magic.entity;

import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.entity.AbstractMagicProjectileEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AirStrikeEntity
extends AbstractMagicProjectileEntity {
    public AirStrikeEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public AirStrikeEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.AIR_STRIKE.get(), pLevel, pShooter);
    }

    @Override
    protected void onChargingTick() {
        if (this.level().isClientSide) {
            float scale = this.getChargeScale();
            int i = 0;
            while ((float)i < scale) {
                this.level().addParticle((ParticleOptions)ParticleTypes.CLOUD, this.getX() + (this.random.nextDouble() - 0.5) * (double)scale, this.getY() + (this.random.nextDouble() - 0.5) * (double)scale, this.getZ() + (this.random.nextDouble() - 0.5) * (double)scale, 0.0, 0.0, 0.0);
                ++i;
            }
        }
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        if (this.level().isClientSide) {
            float scale = this.getChargeScale();
            for (int i = 0; i < 3; ++i) {
                this.level().addParticle((ParticleOptions)ParticleTypes.CLOUD, d0 + (this.random.nextDouble() - 0.5) * (double)scale, d1 + (this.random.nextDouble() - 0.5) * (double)scale, d2 + (this.random.nextDouble() - 0.5) * (double)scale, vec3.x * 0.1, vec3.y * 0.1, vec3.z * 0.1);
            }
        }
    }

    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            Entity entity = pResult.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity)entity;
                double knockbackStrength = 1.5 * (double)scale;
                Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(knockbackStrength);
                if (vec3.lengthSqr() > 0.0) {
                    target.push(vec3.x, 0.5 * (double)scale, vec3.z);
                }
            }
            this.discard();
        }
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}

