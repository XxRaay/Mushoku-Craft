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

public class RockBulletEntity extends AbstractMagicProjectileEntity {
    
    public int spinTicks = 0;

    public RockBulletEntity(EntityType<? extends net.minecraft.world.entity.projectile.ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.maxFlightTicks = 100;
    }

    public RockBulletEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.ROCK_BULLET.get(), pLevel, pShooter);
        this.maxFlightTicks = 100;
    }

    @Override
    public void tick() {
        float scale = this.getChargeScale();
        // Only spin when flying, not while charging
        if (!this.isCharging()) {
            this.spinTicks += 10 + (int)(scale * 20);
        }
        super.tick();
    }

    @Override
    public float getSpin(float partialTicks) {
        if (this.isCharging()) {
            return 0f; // No spin while charging — face forward like a bullet
        }
        return this.spinTicks + partialTicks * (10 + this.getChargeScale() * 20);
    }

    @Override
    public boolean scalesWithCharge() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            if (pResult.getEntity() instanceof LivingEntity target) {
                // Rock bullet has high physical damage
                net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource(
                    this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.mushokucraft.init.ModDamageTypes.MAGIC),
                    this, this.getOwner()
                );
                target.hurt(source, 4.0F * scale);
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





