package com.mushokucraft.magic.entity;

import com.mushokucraft.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterballEntity extends AbstractMagicProjectileEntity {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_TRAPPED_ENTITY_ID = net.minecraft.network.syncher.SynchedEntityData.defineId(WaterballEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    public int trappedTicks = 0;
    public int maxTrappedTicks = 60;

    public WaterballEntity(EntityType<? extends net.minecraft.world.entity.projectile.ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public WaterballEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.WATERBALL.get(), pLevel, pShooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_TRAPPED_ENTITY_ID, -1);
    }

    public void setTrappedEntityId(int id) {
        this.entityData.set(DATA_TRAPPED_ENTITY_ID, id);
    }
    
    public int getTrappedEntityId() {
        return this.entityData.get(DATA_TRAPPED_ENTITY_ID);
    }

    @Override
    protected boolean customTickLogic() {
        if (this.getTrappedEntityId() != -1) {
            net.minecraft.world.entity.Entity targetEntity = this.level().getEntity(this.getTrappedEntityId());
            if (targetEntity instanceof LivingEntity target && target.isAlive()) {
                double visualOffset = this.getChargeScale() * 0.125;
                this.setPos(target.getX(), target.getY() + target.getBbHeight() / 2.0 - visualOffset, target.getZ());
                this.setDeltaMovement(Vec3.ZERO);
                
                if (!this.level().isClientSide) {
                    this.trappedTicks++;
                    target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LEVITATION, 10, 0, false, false));
                    
                    if (this.trappedTicks % 10 == 0) {
                        target.hurt(this.damageSources().drown(), 4.0F);
                    }
                    
                    target.setAirSupply(Math.max(-20, target.getAirSupply() - 2));
                    
                    if (this.trappedTicks > this.maxTrappedTicks) {
                        this.discard();
                    }
                } else {
                    float scale = this.getChargeScale();
                    for (int i = 0; i < 3; i++) {
                        this.level().addParticle(ParticleTypes.BUBBLE, 
                            this.getX() + (this.random.nextDouble() - 0.5) * scale, 
                            this.getY() + (this.random.nextDouble() - 0.5) * scale, 
                            this.getZ() + (this.random.nextDouble() - 0.5) * scale, 
                            0.0D, 0.1D, 0.0D);
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
            this.level().addParticle(ParticleTypes.SPLASH, d0, d1 + (0.5D * scale), d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            if (pResult.getEntity() instanceof LivingEntity target) {
                if (scale >= 2.0f) {
                    // Initial impact damage before trapping
                    net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource(
                        this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.mushokucraft.init.ModDamageTypes.MAGIC),
                        this, this.getOwner()
                    );
                    target.hurt(source, 6.0F);
                    
                    this.setTrappedEntityId(target.getId());
                    this.setDeltaMovement(Vec3.ZERO);
                    this.setNoGravity(true);
                    
                    // Scale duration based on charge scale: 2.0 -> 1s (20 ticks), 3.0 -> 3s (60 ticks)
                    float durationSeconds = 1.0f + (scale - 2.0f) * 2.0f;
                    this.maxTrappedTicks = (int) (durationSeconds * 20.0f);
                    
                    // Visually scale up to envelop the entity.
                    // The base Waterball model is 4 pixels (0.25 blocks) wide. 
                    // To envelop an entity of height H, we need scale = H / 0.25 = H * 4.
                    // We use H * 5.0f for a nice comfortable bubble around the target.
                    this.setChargeScale(Math.max(5.0f, target.getBbHeight() * 5.0f));
                    return; // Do not discard
                } else {
                    // Normal heavy damage for non-trapping hits
                    net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource(
                        this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.mushokucraft.init.ModDamageTypes.MAGIC),
                        this, this.getOwner()
                    );
                    target.hurt(source, 5.0F * scale);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide && this.getTrappedEntityId() == -1) {
            this.discard();
        }
    }
}





