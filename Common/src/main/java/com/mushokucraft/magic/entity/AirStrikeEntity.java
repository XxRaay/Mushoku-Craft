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

public class AirStrikeEntity extends AbstractMagicProjectileEntity {

    public AirStrikeEntity(EntityType<? extends net.minecraft.world.entity.projectile.ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public AirStrikeEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.AIR_STRIKE.get(), pLevel, pShooter);
    }

    @Override
    protected void onChargingTick() {
        if (this.level().isClientSide) {
            float scale = this.getChargeScale();
            for(int i = 0; i < scale; i++) {
               this.level().addParticle(ParticleTypes.CLOUD, this.getX() + (this.random.nextDouble() - 0.5) * scale, this.getY() + (this.random.nextDouble() - 0.5) * scale, this.getZ() + (this.random.nextDouble() - 0.5) * scale, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        if (this.level().isClientSide) {
            float scale = this.getChargeScale();
            for(int i=0; i<3; i++) {
                this.level().addParticle(ParticleTypes.CLOUD, d0 + (this.random.nextDouble() - 0.5) * scale, d1 + (this.random.nextDouble() - 0.5) * scale, d2 + (this.random.nextDouble() - 0.5) * scale, vec3.x * 0.1, vec3.y * 0.1, vec3.z * 0.1);
            }
        }
    }

    @Override
    public com.mushokucraft.magic.MagicSchool getMagicSchool() {
        return com.mushokucraft.magic.MagicSchool.WIND;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            if (pResult.getEntity() instanceof LivingEntity target) {
                // Knockback scaled by charge and wind magic bonus
                double mult = com.mushokucraft.accessory.AccessoryHelper.getSchoolDamageMultiplier(this.getOwner(), this.getMagicSchool());
                double knockbackStrength = 1.5D * scale * mult;
                Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(knockbackStrength);
                if (vec3.lengthSqr() > 0.0D) {
                    target.push(vec3.x, 0.5D * scale * mult, vec3.z);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}





