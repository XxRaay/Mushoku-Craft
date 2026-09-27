package com.mushokucraft.magic.entity;

import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class WaterSliceEntity extends AbstractMagicProjectileEntity {

    public WaterSliceEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.maxFlightTicks = 60; // Shorter lifetime, high speed
    }

    public WaterSliceEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.WATER_SLICE.get(), pLevel, pShooter);
        this.maxFlightTicks = 60;
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        if (this.level().isClientSide) {
            float widthScale = 1.0f + (this.getChargeScale() - 1.0f) * MushokuConfig.WATER_SLICE_CHARGE_WIDTH_MULT.get().floatValue();
            for (int i = 0; i < 5; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * widthScale * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (this.random.nextDouble() - 0.5) * widthScale * 0.5;
                this.level().addParticle(ParticleTypes.SPLASH, d0 + offsetX, d1 + offsetY, d2 + offsetZ, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public com.mushokucraft.magic.MagicSchool getMagicSchool() {
        return com.mushokucraft.magic.MagicSchool.WATER;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float charge = this.getChargeScale();
            float widthScale = 1.0f + (charge - 1.0f) * MushokuConfig.WATER_SLICE_CHARGE_WIDTH_MULT.get().floatValue();
            
            // Damage scaling: base + charge multiplier + accessory bonus
            float baseDamage = MushokuConfig.WATER_SLICE_BASE_DAMAGE.get().floatValue();
            float finalDamage = com.mushokucraft.accessory.AccessoryHelper.applyMagicDamageBonus(this.getOwner(), this.getMagicSchool(), baseDamage * charge);
            
            if (pResult.getEntity() instanceof LivingEntity target) {
                net.minecraft.world.damagesource.DamageSource source = new net.minecraft.world.damagesource.DamageSource(
                    this.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                    this, this.getOwner()
                );
                target.hurt(source, finalDamage);
            }
            this.discard();
        }
    }
}
