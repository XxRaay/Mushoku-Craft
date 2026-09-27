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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Typhoon / Squall (Saint Wind Magic / 聖級「タイフーン」・「スコール」).
 * Ritual atmospheric domain magic altering regional weather across a settlement-wide scale (48-block radius).
 * Generates an overpowering gale-force tempest that paralyzes enemy movements, deflects incoming projectile barrages,
 * and bombards the battlefield with devastating high-pressure atmospheric microburst slams.
 */
public class TyphoonEntity extends AbstractMagicProjectileEntity {

    private int activeTicks = 0;
    private int galeTimer = 0;
    private int microburstTimer = 0;
    private int soundTimer = 0;
    private boolean wasEverCharging = false;

    public TyphoonEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public TyphoonEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.TYPHOON.get(), pLevel, pShooter);
        this.setNoGravity(true);
        this.setPos(pShooter.getX(), pShooter.getY(), pShooter.getZ());
    }

    @Override
    public MagicSchool getMagicSchool() {
        return MagicSchool.WIND;
    }

    @Override
    protected boolean customTickLogic() {
        return true;
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

        // Anchor directly to the caster's epicenter
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.setDeltaMovement(Vec3.ZERO);

        double radius = MushokuConfig.TYPHOON_RADIUS.get();

        // 1. Saint-Rank Climate & Weather Manipulation
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (!serverLevel.isRaining()) {
                serverLevel.setWeatherParameters(0, 6000, true, true);
            }

            // Eye of the Hurricane: Caster receives atmospheric ward and movement enhancement
            if (owner instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false, true));
                player.resetFallDistance();
            }
        }

        // 2. Client-side howling wind audio and atmospheric storm particles
        if (this.level().isClientSide) {
            soundTimer++;
            if (soundTimer >= 22) {
                soundTimer = 0;
                this.level().playLocalSound(this.getX(), this.getY() + 3.0, this.getZ(),
                        SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, 1.6f, 0.55f, false);
                this.level().playLocalSound(this.getX(), this.getY() + 1.0, this.getZ(),
                        SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.WEATHER, 0.8f, 0.6f, false);
            }

            // Swirling atmospheric vapor ribbons across entire domain
            for (int i = 0; i < 7; i++) {
                double dist = 3.0 + this.random.nextDouble() * (radius - 5.0);
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double h = (this.random.nextDouble() - 0.1) * 16.0;

                double px = this.getX() + Math.cos(angle) * dist;
                double py = this.getY() + h;
                double pz = this.getZ() + Math.sin(angle) * dist;

                // Cyclonic tangential hurricane velocity
                double vx = -Math.sin(angle) * 0.6;
                double vz = Math.cos(angle) * 0.6;

                this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, vx, 0.05, vz);
                if (this.random.nextFloat() < 0.25f) {
                    this.level().addParticle(ParticleTypes.GUST, px, py, pz, 0, 0, 0);
                }
            }
        } else {
            ServerLevel serverLevel = (ServerLevel) this.level();
            AABB domainBox = this.getBoundingBox().inflate(radius, 30.0, radius);
            List<LivingEntity> enemies = serverLevel.getEntitiesOfClass(LivingEntity.class, domainBox,
                    e -> e != owner && e.isAlive());

            // 3. Continuous Gale-Force Push & Army Paralysis (every 5 ticks)
            galeTimer++;
            if (galeTimer >= 5) {
                galeTimer = 0;
                boolean dealGaleDmg = (this.tickCount % 20 == 0);
                float galeDmg = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.WIND,
                        MushokuConfig.TYPHOON_GALE_DAMAGE.get().floatValue());
                DamageSource galeSource = dealGaleDmg ? new DamageSource(
                        serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                        this, this.getOwner()
                ) : null;

                for (LivingEntity enemy : enemies) {
                    double dist = enemy.position().distanceTo(this.position());
                    if (dist <= radius) {
                        // Gale-force outward & cyclonic resistance
                        Vec3 away = enemy.position().subtract(this.position()).normalize();
                        Vec3 tan = new Vec3(-away.z, 0, away.x).normalize();
                        double pushPower = 0.22;
                        enemy.push(away.x * pushPower + tan.x * 0.15, 0.05, away.z * pushPower + tan.z * 0.15);
                        enemy.hasImpulse = true;

                        // Severe mobility debuffs simulating impossible headwind
                        enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, true));
                        enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, false, true));

                        if (dealGaleDmg && galeSource != null) {
                            enemy.hurt(galeSource, galeDmg);
                        }
                    }
                }
            }

            // 4. Anti-Projectile Gale Barrier (All hostile projectiles deflected or halted)
            List<Projectile> projectiles = serverLevel.getEntitiesOfClass(Projectile.class, domainBox,
                    p -> p != this && p.getOwner() != owner);
            for (Projectile proj : projectiles) {
                Vec3 pVel = proj.getDeltaMovement();
                // Severe deceleration and chaotic wind deflection
                double randX = (this.random.nextDouble() - 0.5) * 0.3;
                double randZ = (this.random.nextDouble() - 0.5) * 0.3;
                proj.setDeltaMovement(pVel.x * 0.25 + randX, pVel.y * 0.35 + 0.1, pVel.z * 0.25 + randZ);
                proj.hasImpulse = true;
                if (this.random.nextFloat() < 0.3f) {
                    serverLevel.sendParticles(ParticleTypes.GUST, proj.getX(), proj.getY(), proj.getZ(),
                            2, 0.1, 0.1, 0.1, 0.05);
                }
            }

            // 5. Atmospheric Microburst Gale Slams (every 25 ticks)
            microburstTimer++;
            int mInterval = MushokuConfig.TYPHOON_STRIKE_INTERVAL_TICKS.get();
            if (microburstTimer >= mInterval) {
                microburstTimer = 0;
                triggerMicroburstSlam(serverLevel, enemies, radius);
            }

            // 6. Channeling and Duration management
            if (this.isCharging()) {
                this.wasEverCharging = true;
                this.activeTicks = 0;
            } else {
                if (this.wasEverCharging) {
                    // Lingers for 100 ticks (5 seconds) after channeling ceases
                    this.activeTicks++;
                    if (this.activeTicks > 100) {
                        this.discard();
                    }
                } else {
                    // Normal non-channeled duration (12 seconds)
                    this.activeTicks++;
                    if (this.activeTicks > 240) {
                        this.discard();
                    }
                }
            }
        }
    }

    private void triggerMicroburstSlam(ServerLevel serverLevel, List<LivingEntity> enemies, double radius) {
        Vec3 slamPos;
        if (!enemies.isEmpty() && this.random.nextBoolean()) {
            LivingEntity target = enemies.get(this.random.nextInt(enemies.size()));
            slamPos = target.position();
        } else {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 4.0 + this.random.nextDouble() * (radius - 8.0);
            double sx = this.getX() + Math.cos(a) * r;
            double sz = this.getZ() + Math.sin(a) * r;
            BlockPos ground = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos((int) sx, (int) this.getY(), (int) sz));
            slamPos = new Vec3(sx, ground.getY(), sz);
        }

        // Visual and audio air-burst shockwave
        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, slamPos.x, slamPos.y + 1.0, slamPos.z, 1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.GUST, slamPos.x, slamPos.y + 1.5, slamPos.z, 25, 1.5, 1.0, 1.5, 0.15);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, slamPos.x, slamPos.y + 0.5, slamPos.z, 2, 0.5, 0.5, 0.5, 0.0);

        serverLevel.playSound(null, slamPos.x, slamPos.y, slamPos.z,
                SoundEvents.WIND_CHARGE_BURST, SoundSource.WEATHER, 3.0f, 0.55f);
        serverLevel.playSound(null, slamPos.x, slamPos.y, slamPos.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0f, 1.2f);

        // Microburst impact damage & crush knockdown
        double burstRadius = 5.5;
        AABB burstBox = new AABB(slamPos.x - burstRadius, slamPos.y - 2.0, slamPos.z - burstRadius,
                slamPos.x + burstRadius, slamPos.y + 6.0, slamPos.z + burstRadius);

        List<LivingEntity> burstVictims = serverLevel.getEntitiesOfClass(LivingEntity.class, burstBox,
                e -> e != this.getOwner() && e.isAlive());

        DamageSource source = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                this, this.getOwner()
        );

        float baseDmg = MushokuConfig.TYPHOON_STRIKE_DAMAGE.get().floatValue();
        float finalDmg = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.WIND, baseDmg);

        for (LivingEntity victim : burstVictims) {
            victim.hurt(source, finalDmg);
            // Downward crush / outward blast
            Vec3 push = victim.position().subtract(slamPos).normalize();
            victim.push(push.x * 1.4, -0.4, push.z * 1.4);
            victim.hasImpulse = true;
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false, true));
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        // Channeled domain storm anchored to player, do not launch
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(6000, 0, false, false);
        }
        super.remove(reason);
    }
}
