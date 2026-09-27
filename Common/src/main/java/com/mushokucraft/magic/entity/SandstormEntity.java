package com.mushokucraft.magic.entity;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.MagicSchool;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Sandstorm (Saint Earth Magic / 聖級「砂嵐」).
 * Ritual climatic spell that alters weather and atmospheric state over an immense multi-kilometer domain (50+ blocks).
 * Blinds, disorients, and physically drains hostile armies with continuous abrasive sand gales,
 * spawns localized tearing sand vortexes, and neutralizes incoming projectile barrages.
 */
public class SandstormEntity extends AbstractMagicProjectileEntity {

    private int activeTicks = 0;
    private int abrasionTimer = 0;
    private int vortexTimer = 0;
    private int soundTimer = 0;
    private boolean wasEverCharging = false;

    public SandstormEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public SandstormEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.SANDSTORM.get(), pLevel, pShooter);
        this.setNoGravity(true);
        this.setPos(pShooter.getX(), pShooter.getY(), pShooter.getZ());
    }

    @Override
    public MagicSchool getMagicSchool() {
        return MagicSchool.EARTH;
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

        // Anchor to the caster's position
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.setDeltaMovement(Vec3.ZERO);

        double radius = MushokuConfig.SANDSTORM_RADIUS.get();

        // 1. Saint-rank environmental climate control & Caster Camouflage
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.isRaining() || serverLevel.isThundering()) {
                serverLevel.setWeatherParameters(6000, 0, false, false);
            }

            // Grant caster semi-invisibility (vanilla invisibility effect to reduce mob aggro range while inside storm)
            if (owner instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false, true));
            }
        }

        // 2. Client-side audio, atmospheric particles, and camouflage tracker
        if (this.level().isClientSide) {
            com.mushokucraft.client.render.ClientSandstormTracker.register(this);
            soundTimer++;
            if (soundTimer >= 25) {
                soundTimer = 0;
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, 1.2f, 0.7f, false);
            }

            // Swirling sand ribbons and dust clouds
            for (int i = 0; i < 6; i++) {
                double dist = 2.0 + this.random.nextDouble() * (radius - 4.0);
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double h = (this.random.nextDouble() - 0.2) * 14.0;
                double px = this.getX() + Math.cos(angle) * dist;
                double py = this.getY() + h;
                double pz = this.getZ() + Math.sin(angle) * dist;

                // Swirling tangential velocity
                double vx = -Math.sin(angle) * 0.45;
                double vz = Math.cos(angle) * 0.45;

                this.level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        px, py, pz, vx, -0.05, vz);

                if (this.random.nextFloat() < 0.25f) {
                    this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            px, py, pz, vx * 0.5, 0.05, vz * 0.5);
                }
            }
        } else {
            ServerLevel serverLevel = (ServerLevel) this.level();
            AABB domainBox = this.getBoundingBox().inflate(radius, 25.0, radius);
            List<LivingEntity> enemies = serverLevel.getEntitiesOfClass(LivingEntity.class, domainBox,
                    e -> e != owner && e.isAlive());

            // 3. Continuous sand abrasion & army debuffs (every 20 ticks = 1 second)
            abrasionTimer++;
            if (abrasionTimer >= 20) {
                abrasionTimer = 0;
                float abrasionDamage = AccessoryHelper.applyMagicDamageBonus(owner, MagicSchool.EARTH,
                        MushokuConfig.SANDSTORM_ABRASION_DAMAGE.get().floatValue());
                DamageSource source = new DamageSource(
                        serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                        this, owner
                );

                for (LivingEntity enemy : enemies) {
                    if (enemy.position().distanceTo(this.position()) <= radius) {
                        enemy.hurt(source, abrasionDamage);
                        // "Ослепляет, дезориентирует и физически истощает целые армии на огромной территории"
                        enemy.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));
                        enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false, true));
                        enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
                    }
                }
            }

            // 4. Spontaneous Sand Vortexes / Dust Devils (every 30 ticks)
            vortexTimer++;
            int vInterval = MushokuConfig.SANDSTORM_VORTEX_INTERVAL_TICKS.get();
            if (vortexTimer >= vInterval) {
                vortexTimer = 0;
                triggerSandVortex(serverLevel, enemies, radius);
            }

            // 5. Projectile Drag / Gale suppression (deflects/slows enemy arrows in domain)
            List<Projectile> projectiles = serverLevel.getEntitiesOfClass(Projectile.class, domainBox,
                    p -> p != this && p.getOwner() != owner);
            for (Projectile proj : projectiles) {
                Vec3 pVel = proj.getDeltaMovement();
                proj.setDeltaMovement(pVel.x * 0.35, pVel.y * 0.5, pVel.z * 0.35);
                proj.hasImpulse = true;
                if (this.random.nextFloat() < 0.2f) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                            proj.getX(), proj.getY(), proj.getZ(), 4, 0.1, 0.1, 0.1, 0.05);
                }
            }

            // 6. Channeling and Duration management
            if (this.isCharging()) {
                this.wasEverCharging = true;
                this.activeTicks = 0;
            } else {
                if (this.wasEverCharging) {
                    // Lingers for 100 ticks (5 seconds) after channeling stops
                    this.activeTicks++;
                    if (this.activeTicks > 100) {
                        this.discard();
                    }
                } else {
                    // Normal cast without channeling lasts 240 ticks (12 seconds)
                    this.activeTicks++;
                    if (this.activeTicks > 240) {
                        this.discard();
                    }
                }
            }
        }
    }

    private void triggerSandVortex(ServerLevel serverLevel, List<LivingEntity> enemies, double radius) {
        Vec3 vPos;
        if (!enemies.isEmpty() && this.random.nextBoolean()) {
            LivingEntity target = enemies.get(this.random.nextInt(enemies.size()));
            vPos = target.position();
        } else {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 4.0 + this.random.nextDouble() * (radius - 8.0);
            vPos = this.position().add(Math.cos(a) * r, (this.random.nextDouble() - 0.2) * 4.0, Math.sin(a) * r);
        }

        // Visual and audio whirlwind burst
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState()),
                vPos.x, vPos.y + 1.0, vPos.z, 40, 0.8, 1.2, 0.8, 0.2);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                vPos.x, vPos.y + 1.5, vPos.z, 50, 1.0, 1.5, 1.0, 0.25);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                vPos.x, vPos.y + 0.8, vPos.z, 3, 0.3, 0.3, 0.3, 0.0);

        serverLevel.playSound(null, vPos.x, vPos.y, vPos.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 2.0f, 0.8f);
        serverLevel.playSound(null, vPos.x, vPos.y, vPos.z, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 2.0f, 0.6f);

        // Vortex blast damage and whirlwind lift
        double vRadius = 4.5;
        AABB vBox = new AABB(vPos.x - vRadius, vPos.y - 1.0, vPos.z - vRadius,
                vPos.x + vRadius, vPos.y + 4.0, vPos.z + vRadius);
        List<LivingEntity> vTargets = serverLevel.getEntitiesOfClass(LivingEntity.class, vBox,
                e -> e != this.getOwner() && e.isAlive());

        float vDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.EARTH,
                MushokuConfig.SANDSTORM_VORTEX_DAMAGE.get().floatValue());
        DamageSource source = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                this, this.getOwner()
        );

        for (LivingEntity victim : vTargets) {
            victim.hurt(source, vDamage);
            Vec3 vel = victim.getDeltaMovement();
            victim.setDeltaMovement(vel.x * 0.2 + (this.random.nextDouble() - 0.5) * 0.4,
                    0.65, vel.z * 0.2 + (this.random.nextDouble() - 0.5) * 0.4);
            victim.hasImpulse = true;
        }
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        if (this.level().isClientSide) {
            com.mushokucraft.client.render.ClientSandstormTracker.unregister(this);
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        if (this.level().isClientSide) {
            com.mushokucraft.client.render.ClientSandstormTracker.unregister(this);
        }
    }
}
