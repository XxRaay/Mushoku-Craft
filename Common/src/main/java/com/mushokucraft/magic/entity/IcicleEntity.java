package com.mushokucraft.magic.entity;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class IcicleEntity extends AbstractMagicProjectileEntity {

    public IcicleEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public IcicleEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.ICICLE_ENTITY.get(), pLevel, pShooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
    }
    
    @Override
    protected boolean customTickLogic() {
        if (!this.level().isClientSide) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.05, 0)); // Gravity
            if (this.tickCount > 100) {
                this.discard();
                return true;
            }
        }
        return false;
    }

    @Override
    public com.mushokucraft.magic.MagicSchool getMagicSchool() {
        return com.mushokucraft.magic.MagicSchool.WATER;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            if (pResult.getEntity() instanceof LivingEntity target) {
                float damage = com.mushokucraft.accessory.AccessoryHelper.applyMagicDamageBonus(this.getOwner(), this.getMagicSchool(), MushokuConfig.ICICLE_BREAK_ICICLE_DAMAGE.get().floatValue());
                net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource(
                    this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.mushokucraft.init.ModDamageTypes.MAGIC),
                    this, this.getOwner()
                );
                target.hurt(source, damage);
                
                // Maybe apply slowness?
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            }
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
    
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
