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

import com.mushokucraft.magic.entity.IMagicProjectile;

public class FireballEntity extends AbstractMagicProjectileEntity {

    public FireballEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public FireballEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.FIREBALL.get(), pLevel, pShooter);
    }

    @Override
    protected void onChargingTick() {
        if (this.level().isClientSide) {
             this.level().addParticle(ParticleTypes.FLAME, this.getX() + (Math.random() - 0.5)*0.5, this.getY() + (Math.random() - 0.5)*0.5, this.getZ() + (Math.random() - 0.5)*0.5, 0, 0, 0);
        }
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            if (pResult.getEntity() instanceof LivingEntity target) {
                net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource(
                    this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.mushokucraft.init.ModDamageTypes.MAGIC),
                    this, this.getOwner()
                );
                target.hurt(source, 6.0F * scale);
                target.igniteForSeconds((int)(5 * scale)); // Ignite target
            }
            if (scale > 2.0f) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), scale, true, Level.ExplosionInteraction.BLOCK);
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            if (scale > 2.0f && pResult.getType() == HitResult.Type.BLOCK) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), scale, true, Level.ExplosionInteraction.BLOCK);
            }
            this.discard();
        }
    }
}





