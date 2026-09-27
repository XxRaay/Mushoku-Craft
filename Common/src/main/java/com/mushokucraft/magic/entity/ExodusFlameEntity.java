package com.mushokucraft.magic.entity;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.MagicSchool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Exodus Flame (Advanced Fire Magic / 上級「エクソダスフレイム」).
 * A colossal, devastating rolling sphere of dense apocalyptic fire.
 * Scorches everything in its path and detonates in an incinerating area-of-effect blast.
 */
public class ExodusFlameEntity extends AbstractMagicProjectileEntity {

    private boolean hasExploded = false;
    private int trailTickCounter = 0;

    public ExodusFlameEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.maxFlightTicks = 120;
    }

    public ExodusFlameEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.EXODUS_FLAME.get(), pLevel, pShooter);
        this.maxFlightTicks = 120;
    }

    @Override
    public MagicSchool getMagicSchool() {
        return MagicSchool.FIRE;
    }

    @Override
    protected void onChargingTick() {
        if (this.level().isClientSide) {
            float scale = this.getChargeScale();
            for (int i = 0; i < 3; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double dist = 0.5 * scale + this.random.nextDouble() * 0.4;
                double px = this.getX() + Math.cos(angle) * dist;
                double py = this.getY() + (this.random.nextDouble() - 0.5) * 0.6 * scale;
                double pz = this.getZ() + Math.sin(angle) * dist;
                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, -Math.cos(angle) * 0.06, 0.02, -Math.sin(angle) * 0.06);
                if (this.random.nextFloat() < 0.35f) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.03, 0);
                }
            }
        }
    }

    @Override
    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
        float charge = this.getChargeScale();

        if (this.level().isClientSide) {
            double radius = 0.6 * charge;
            for (int i = 0; i < 5; i++) {
                double rx = (this.random.nextDouble() - 0.5) * radius * 2.0;
                double ry = (this.random.nextDouble() - 0.5) * radius * 2.0;
                double rz = (this.random.nextDouble() - 0.5) * radius * 2.0;
                this.level().addParticle(ParticleTypes.FLAME, d0 + rx, d1 + ry, d2 + rz, 0.0, 0.0, 0.0);
            }
            if (this.random.nextFloat() < 0.4f) {
                this.level().addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0, 0.0, 0.0);
            }
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, d0, d1, d2, 0.0, 0.05, 0.0);
        } else {
            trailTickCounter++;
            if (trailTickCounter % 3 == 0) {
                // Radiant heat scorching aura along flight path
                double scorchRadius = 2.0 * charge;
                AABB trailBox = this.getBoundingBox().inflate(scorchRadius);
                List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, trailBox,
                        e -> e != this.getOwner() && e.isAlive());

                DamageSource damageSource = new DamageSource(
                        this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                        this, this.getOwner()
                );
                float trailDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.FIRE,
                        MushokuConfig.EXODUS_FLAME_TRAIL_DAMAGE.get().floatValue() * charge);

                for (LivingEntity target : nearby) {
                    target.hurt(damageSource, trailDamage);
                    target.igniteForSeconds(4);
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            detonate(pResult.getLocation());
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            detonate(pResult.getLocation());
        }
    }

    private void detonate(Vec3 hitPos) {
        if (this.hasExploded) return;
        this.hasExploded = true;

        if (this.level() instanceof ServerLevel serverLevel) {
            float charge = this.getChargeScale();
            double radius = MushokuConfig.EXODUS_FLAME_RADIUS.get() * Math.min(1.8, 0.8 + 0.3 * charge);
            float baseDamage = MushokuConfig.EXODUS_FLAME_BASE_DAMAGE.get().floatValue();
            float finalMaxDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.FIRE, baseDamage * charge);

            // Explosive audio
            serverLevel.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 0.8f);
            serverLevel.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.5f, 0.65f);

            // Cataclysmic particles
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, hitPos.x, hitPos.y + 0.5, hitPos.z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.FLASH, hitPos.x, hitPos.y + 0.5, hitPos.z, 2, 0.3, 0.3, 0.3, 0);
            serverLevel.sendParticles(ParticleTypes.LAVA, hitPos.x, hitPos.y + 0.5, hitPos.z, 45, 1.5, 1.2, 1.5, 0.2);
            serverLevel.sendParticles(ParticleTypes.FLAME, hitPos.x, hitPos.y + 0.5, hitPos.z, 100, 2.2, 1.5, 2.2, 0.25);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, hitPos.x, hitPos.y + 0.5, hitPos.z, 25, 1.8, 1.8, 1.8, 0.08);

            // Area of Effect damage & outward shockwave
            AABB explosionBox = new AABB(
                    hitPos.x - radius, hitPos.y - radius, hitPos.z - radius,
                    hitPos.x + radius, hitPos.y + radius, hitPos.z + radius
            );
            List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, explosionBox,
                    e -> e != this.getOwner() && e.isAlive());

            DamageSource damageSource = new DamageSource(
                    this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                    this, this.getOwner()
            );

            for (LivingEntity victim : victims) {
                double dist = victim.position().distanceTo(hitPos);
                if (dist <= radius) {
                    double falloff = Math.max(0.4, 1.0 - (dist / radius) * 0.5);
                    victim.hurt(damageSource, (float) (finalMaxDamage * falloff));
                    victim.igniteForSeconds((int) (10 + 5 * charge));

                    // Radial outward blast knockback
                    Vec3 dir = victim.position().subtract(hitPos);
                    if (dir.lengthSqr() > 0.001) {
                        dir = dir.normalize();
                    } else {
                        dir = new Vec3(0, 1, 0);
                    }
                    victim.setDeltaMovement(victim.getDeltaMovement().add(dir.scale(1.2 * falloff)).add(0, 0.45, 0));
                    victim.hasImpulse = true;
                }
            }

            // Scorched ground: ignite a few surface blocks if gamerule permits
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                for (int i = 0; i < 6; i++) {
                    double ox = (this.random.nextDouble() - 0.5) * radius * 0.8;
                    double oz = (this.random.nextDouble() - 0.5) * radius * 0.8;
                    BlockPos bp = BlockPos.containing(hitPos.x + ox, hitPos.y, hitPos.z + oz);
                    BlockPos above = bp.above();
                    if (serverLevel.getBlockState(bp).isSolid() && serverLevel.isEmptyBlock(above)) {
                        serverLevel.setBlockAndUpdate(above, Blocks.FIRE.defaultBlockState());
                    }
                }
            }
        }

        this.discard();
    }
}
