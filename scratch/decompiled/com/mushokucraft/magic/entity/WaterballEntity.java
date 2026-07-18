/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
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

import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.entity.AbstractMagicProjectileEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterballEntity
extends AbstractMagicProjectileEntity {
    private static final EntityDataAccessor<Integer> DATA_TRAPPED_ENTITY_ID = SynchedEntityData.defineId(WaterballEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    public int trappedTicks = 0;
    public int maxTrappedTicks = 60;

    public WaterballEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public WaterballEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.WATERBALL.get(), pLevel, pShooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_TRAPPED_ENTITY_ID, (Object)-1);
    }

    public void setTrappedEntityId(int id) {
        this.entityData.set(DATA_TRAPPED_ENTITY_ID, (Object)id);
    }

    public int getTrappedEntityId() {
        return (Integer)this.entityData.get(DATA_TRAPPED_ENTITY_ID);
    }

    @Override
    protected boolean customTickLogic() {
        if (this.getTrappedEntityId() != -1) {
            LivingEntity target;
            Entity targetEntity = this.level().getEntity(this.getTrappedEntityId());
            if (targetEntity instanceof LivingEntity && (target = (LivingEntity)targetEntity).isAlive()) {
                double visualOffset = (double)this.getChargeScale() * 0.125;
                this.setPos(target.getX(), target.getY() + (double)target.getBbHeight() / 2.0 - visualOffset, target.getZ());
                this.setDeltaMovement(Vec3.ZERO);
                if (!this.level().isClientSide) {
                    ++this.trappedTicks;
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 10, 0, false, false));
                    if (this.trappedTicks % 10 == 0) {
                        target.hurt(this.damageSources().drown(), 4.0f);
                    }
                    target.setAirSupply(Math.max(-20, target.getAirSupply() - 2));
                    if (this.trappedTicks > this.maxTrappedTicks) {
                        this.discard();
                    }
                } else {
                    float scale = this.getChargeScale();
                    for (int i = 0; i < 3; ++i) {
                        this.level().addParticle((ParticleOptions)ParticleTypes.BUBBLE, this.getX() + (this.random.nextDouble() - 0.5) * (double)scale, this.getY() + (this.random.nextDouble() - 0.5) * (double)scale, this.getZ() + (this.random.nextDouble() - 0.5) * (double)scale, 0.0, 0.1, 0.0);
                    }
                }
            } else if (!this.level().isClientSide) {
                this.discard();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        if (this.level().isClientSide) {
            float scale = this.getChargeScale();
            this.level().addParticle((ParticleOptions)ParticleTypes.SPLASH, d0, d1 + 0.5 * (double)scale, d2, 0.0, 0.0, 0.0);
        }
    }

    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            Entity entity = pResult.getEntity();
            if (entity instanceof LivingEntity) {
                DamageSource source;
                LivingEntity target = (LivingEntity)entity;
                if (scale >= 2.0f) {
                    source = new DamageSource((Holder)this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC), (Entity)this, this.getOwner());
                    target.hurt(source, 6.0f);
                    this.setTrappedEntityId(target.getId());
                    this.setDeltaMovement(Vec3.ZERO);
                    this.setNoGravity(true);
                    float durationSeconds = 1.0f + (scale - 2.0f) * 2.0f;
                    this.maxTrappedTicks = (int)(durationSeconds * 20.0f);
                    this.setChargeScale(Math.max(5.0f, target.getBbHeight() * 5.0f));
                    return;
                }
                source = new DamageSource((Holder)this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC), (Entity)this, this.getOwner());
                target.hurt(source, 5.0f * scale);
            }
            this.discard();
        }
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide && this.getTrappedEntityId() == -1) {
            this.discard();
        }
    }
}

