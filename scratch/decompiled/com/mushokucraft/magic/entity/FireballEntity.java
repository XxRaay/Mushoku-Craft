/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.mushokucraft.magic.entity;

import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.entity.AbstractMagicProjectileEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireballEntity
extends AbstractMagicProjectileEntity {
    public FireballEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public FireballEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.FIREBALL.get(), pLevel, pShooter);
    }

    @Override
    protected void onChargingTick() {
        if (this.level().isClientSide) {
            this.level().addParticle((ParticleOptions)ParticleTypes.FLAME, this.getX() + (Math.random() - 0.5) * 0.5, this.getY() + (Math.random() - 0.5) * 0.5, this.getZ() + (Math.random() - 0.5) * 0.5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        if (this.level().isClientSide) {
            this.level().addParticle((ParticleOptions)ParticleTypes.FLAME, d0, d1, d2, 0.0, 0.0, 0.0);
            this.level().addParticle((ParticleOptions)ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
        }
    }

    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            Entity entity = pResult.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity)entity;
                DamageSource source = new DamageSource((Holder)this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC), (Entity)this, this.getOwner());
                target.hurt(source, 6.0f * scale);
                target.igniteForSeconds((float)((int)(5.0f * scale)));
            }
            if (scale > 2.0f) {
                this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), scale, true, Level.ExplosionInteraction.BLOCK);
            }
            this.discard();
        }
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            if (scale > 2.0f && pResult.getType() == HitResult.Type.BLOCK) {
                this.level().explode((Entity)this, this.getX(), this.getY(), this.getZ(), scale, true, Level.ExplosionInteraction.BLOCK);
            }
            this.discard();
        }
    }
}

