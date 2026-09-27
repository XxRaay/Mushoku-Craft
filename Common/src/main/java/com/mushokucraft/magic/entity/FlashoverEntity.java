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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Flashover (Saint Fire Magic / 聖級「フラッシュオーバー」).
 * Alters atmospheric thermodynamics over a colossal domain (45+ blocks),
 * superheating oxygen and combustible particles to cause spontaneous self-ignition
 * and explosive detonations throughout the battlefield.
 */
public class FlashoverEntity extends AbstractMagicProjectileEntity {

    private int activeTicks = 0;
    private int detonationTimer = 0;
    private int auraTimer = 0;
    private boolean wasEverCharging = false;

    public FlashoverEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public FlashoverEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.FLASHOVER.get(), pLevel, pShooter);
        this.setNoGravity(true);
        this.setPos(pShooter.getX(), pShooter.getY(), pShooter.getZ());
    }

    @Override
    public MagicSchool getMagicSchool() {
        return MagicSchool.FIRE;
    }

    @Override
    protected boolean customTickLogic() {
        return true; // Atmospheric persistent entity
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity owner = (LivingEntity) this.getOwner();
        if (owner == null || !owner.isAlive()) {
            if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        // Keep anchored to the caster's feet (bottom of player model)
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.setDeltaMovement(Vec3.ZERO);

        double radius = MushokuConfig.FLASHOVER_RADIUS.get();

        // 1. Atmosphere and Weather manipulation (Saint rank alters environment)
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.isRaining() || serverLevel.isThundering()) {
                serverLevel.setWeatherParameters(6000, 0, false, false); // Evaporate storm
            }
        }

        // 2. Client visual heatwave & floating embers
        if (this.level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                double dist = this.random.nextDouble() * radius;
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double h = (this.random.nextDouble() - 0.2) * 12.0;
                double px = this.getX() + Math.cos(angle) * dist;
                double py = this.getY() + h;
                double pz = this.getZ() + Math.sin(angle) * dist;

                this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0.04, 0);
                if (this.random.nextFloat() < 0.25f) {
                    this.level().addParticle(ParticleTypes.LAVA, px, py, pz, 0, 0, 0);
                }
            }
        } else {
            ServerLevel serverLevel = (ServerLevel) this.level();
            AABB domainBox = this.getBoundingBox().inflate(radius, 25.0, radius);
            List<LivingEntity> enemies = serverLevel.getEntitiesOfClass(LivingEntity.class, domainBox,
                    e -> e != owner && e.isAlive());

            // 3. Thermal aura damage every 20 ticks
            auraTimer++;
            if (auraTimer >= 20) {
                auraTimer = 0;
                float thermalDamage = AccessoryHelper.applyMagicDamageBonus(owner, MagicSchool.FIRE,
                        MushokuConfig.FLASHOVER_THERMAL_DAMAGE.get().floatValue());
                DamageSource source = new DamageSource(
                        serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                        this, owner
                );

                for (LivingEntity enemy : enemies) {
                    if (enemy.position().distanceTo(this.position()) <= radius) {
                        enemy.hurt(source, thermalDamage);
                        enemy.igniteForSeconds(6);
                    }
                }
            }

            // 4. Spontaneous thermodynamic flashover detonations
            detonationTimer++;
            int interval = MushokuConfig.FLASHOVER_DETONATION_INTERVAL_TICKS.get();
            if (detonationTimer >= interval) {
                detonationTimer = 0;
                triggerFlashoverDetonations(serverLevel, enemies, radius);
            }

            // 5. Channeling / Duration management
            if (this.isCharging()) {
                this.wasEverCharging = true;
                this.activeTicks = 0;
            } else {
                if (this.wasEverCharging) {
                    // Released after channeling -> linger for 50 ticks (2.5s) heat dissipation
                    this.activeTicks++;
                    if (this.activeTicks > 50) {
                        this.discard();
                    }
                } else {
                    // Normal cast without channeling -> lasts 200 ticks (10s)
                    this.activeTicks++;
                    if (this.activeTicks > 200) {
                        this.discard();
                    }
                }
            }
        }
    }

    private void triggerFlashoverDetonations(ServerLevel serverLevel, List<LivingEntity> enemies, double radius) {
        int explosionsToSpawn = enemies.isEmpty() ? 1 : Math.min(2, enemies.size());

        for (int i = 0; i < explosionsToSpawn; i++) {
            Vec3 detPos;
            if (!enemies.isEmpty() && this.random.nextBoolean()) {
                LivingEntity target = enemies.get(this.random.nextInt(enemies.size()));
                detPos = target.position().add((this.random.nextDouble() - 0.5) * 2.0, 0.5, (this.random.nextDouble() - 0.5) * 2.0);
            } else {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 4.0 + this.random.nextDouble() * (radius - 6.0);
                detPos = this.position().add(Math.cos(a) * r, (this.random.nextDouble() - 0.3) * 6.0, Math.sin(a) * r);
            }

            // Visual and audio detonation
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, detPos.x, detPos.y + 0.5, detPos.z, 2, 0.3, 0.3, 0.3, 0);
            serverLevel.sendParticles(ParticleTypes.FLASH, detPos.x, detPos.y + 0.5, detPos.z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.LAVA, detPos.x, detPos.y + 0.5, detPos.z, 20, 0.8, 0.5, 0.8, 0.15);
            serverLevel.sendParticles(ParticleTypes.FLAME, detPos.x, detPos.y + 0.5, detPos.z, 40, 1.2, 0.8, 1.2, 0.2);

            serverLevel.playSound(null, detPos.x, detPos.y, detPos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.5f, 0.65f);
            serverLevel.playSound(null, detPos.x, detPos.y, detPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.6f, 1.3f);

            // Detonation blast damage to nearby entities
            double blastRadius = 4.5;
            AABB blastBox = new AABB(detPos.x - blastRadius, detPos.y - blastRadius, detPos.z - blastRadius,
                    detPos.x + blastRadius, detPos.y + blastRadius, detPos.z + blastRadius);
            List<LivingEntity> blastTargets = serverLevel.getEntitiesOfClass(LivingEntity.class, blastBox,
                    e -> e != this.getOwner() && e.isAlive());

            float blastDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.FIRE,
                    MushokuConfig.FLASHOVER_DETONATION_DAMAGE.get().floatValue());
            DamageSource source = new DamageSource(
                    serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                    this, this.getOwner()
            );

            for (LivingEntity victim : blastTargets) {
                double dist = victim.position().distanceTo(detPos);
                if (dist <= blastRadius) {
                    double falloff = Math.max(0.5, 1.0 - (dist / blastRadius) * 0.4);
                    victim.hurt(source, (float) (blastDamage * falloff));
                    victim.igniteForSeconds(8);

                    Vec3 push = victim.position().subtract(detPos);
                    if (push.lengthSqr() > 0.01) {
                        push = push.normalize().scale(0.8 * falloff).add(0, 0.35, 0);
                        victim.setDeltaMovement(victim.getDeltaMovement().add(push));
                        victim.hasImpulse = true;
                    }
                }
            }
        }
    }
}
