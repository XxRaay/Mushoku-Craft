package com.mushokucraft.magic.entity;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.MagicSchool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Tornado (Advanced Wind Magic / 上級「トルネード」).
 * Conjures a massive, raging atmospheric vortex across the battlefield.
 * Sucks in enemies, spins them violently, lifts them high into the air,
 * shreds them with debris and fall impact, and shatters defensive formations.
 */
public class TornadoEntity extends AbstractMagicProjectileEntity {

    private int activeTicks = 0;
    private int damageTimer = 0;
    private int soundTimer = 0;
    private Vec3 glideDirection = Vec3.ZERO;

    public TornadoEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public TornadoEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.TORNADO.get(), pLevel, pShooter);
        this.setNoGravity(true);
        Vec3 look = pShooter.getLookAngle();
        this.glideDirection = new Vec3(look.x, 0.0, look.z).normalize();
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

        if (this.isCharging()) {
            if (this.getOwner() instanceof LivingEntity owner) {
                Vec3 look = owner.getLookAngle();
                Vec3 pos = owner.getEyePosition().add(look.scale(3.5)).subtract(0, 1.0, 0);
                this.setPos(pos.x, pos.y, pos.z);
                this.glideDirection = new Vec3(look.x, 0.0, look.z).normalize();
                this.onChargingTick();
            } else if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        // Active battlefield glide logic
        float charge = this.getChargeScale();
        int maxLifetime = (int) (MushokuConfig.TORNADO_LIFETIME_TICKS.get() * (0.8f + 0.35f * charge));

        // Advance along glide direction
        if (this.glideDirection.lengthSqr() > 0.01) {
            double speed = 0.28;
            double nextX = this.getX() + this.glideDirection.x * speed;
            double nextZ = this.getZ() + this.glideDirection.z * speed;

            // Follow terrain elevation smoothly
            BlockPos currentPos = this.blockPosition();
            BlockPos ground = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos((int) nextX, (int) this.getY(), (int) nextZ));
            double targetY = ground.getY();
            double smoothY = this.getY() + (targetY - this.getY()) * 0.2;

            this.setPos(nextX, smoothY, nextZ);
        }

        double radius = MushokuConfig.TORNADO_RADIUS.get() * Math.min(1.6, 0.85 + 0.3 * charge);
        double height = MushokuConfig.TORNADO_HEIGHT.get() * Math.min(1.5, 0.9 + 0.25 * charge);

        // Client-side ambient swirling particles & roaring wind audio
        if (this.level().isClientSide) {
            soundTimer++;
            if (soundTimer >= 18) {
                soundTimer = 0;
                this.level().playLocalSound(this.getX(), this.getY() + 2.0, this.getZ(),
                        SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, 1.4f, 0.65f, false);
            }

            // Swirling multi-height atmospheric wind bands
            for (int i = 0; i < 8; i++) {
                double h = this.random.nextDouble() * height;
                // Conical taper: wider at top
                double rAtH = (1.2 + (h / height) * (radius - 1.2)) * 0.9;
                double angle = (this.tickCount * 0.25) + this.random.nextDouble() * Math.PI * 2.0;

                double px = this.getX() + Math.cos(angle) * rAtH;
                double py = this.getY() + h;
                double pz = this.getZ() + Math.sin(angle) * rAtH;

                // Tangential velocity + upward lift
                double vx = -Math.sin(angle) * 0.45;
                double vy = 0.20;
                double vz = Math.cos(angle) * 0.45;

                this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, vx, vy, vz);
                if (this.random.nextFloat() < 0.3f) {
                    this.level().addParticle(ParticleTypes.GUST, px, py, pz, 0, 0, 0);
                }
                if (this.random.nextFloat() < 0.2f) {
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()),
                            px, py, pz, vx * 0.3, 0.1, vz * 0.3);
                }
            }
        } else {
            ServerLevel serverLevel = (ServerLevel) this.level();
            AABB vortexBox = new AABB(this.getX() - radius, this.getY() - 1.0, this.getZ() - radius,
                    this.getX() + radius, this.getY() + height, this.getZ() + radius);

            List<LivingEntity> trapped = serverLevel.getEntitiesOfClass(LivingEntity.class, vortexBox,
                    e -> e != this.getOwner() && e.isAlive());

            // Vortex Physics: Pull towards center, swirl cyclonically, and levitate upwards
            for (LivingEntity entity : trapped) {
                double dx = this.getX() - entity.getX();
                double dz = this.getZ() - entity.getZ();
                double horizDist = Math.sqrt(dx * dx + dz * dz);

                if (horizDist > 0.1) {
                    // Normalize horizontal direction to center
                    double normX = dx / horizDist;
                    double normZ = dz / horizDist;

                    // Tangential swirl vector (counter-clockwise)
                    double tanX = -normZ;
                    double tanZ = normX;

                    double inwardStrength = Math.min(0.45, 0.15 + (horizDist / radius) * 0.3);
                    double swirlStrength = 0.55;
                    double relativeY = entity.getY() - this.getY();
                    double liftStrength = Math.min(0.42, 0.18 + Math.max(0.0, (height - relativeY) * 0.035));

                    entity.setDeltaMovement(
                            normX * inwardStrength + tanX * swirlStrength,
                            liftStrength,
                            normZ * inwardStrength + tanZ * swirlStrength
                    );
                    entity.hasImpulse = true;
                    entity.resetFallDistance(); // Keep them flying without pre-emptively taking fall damage
                }
            }

            // Periodic slicing wind & debris collision damage (every 10 ticks)
            damageTimer++;
            if (damageTimer >= 10) {
                damageTimer = 0;
                if (!trapped.isEmpty()) {
                    DamageSource source = new DamageSource(
                            serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                            this, this.getOwner()
                    );
                    float baseTickDmg = MushokuConfig.TORNADO_TICK_DAMAGE.get().floatValue();
                    float finalDmg = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.WIND, baseTickDmg * charge);

                    for (LivingEntity entity : trapped) {
                        entity.hurt(source, finalDmg);
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                                2, 0.2, 0.2, 0.2, 0.0);
                    }
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.8f, 1.0f);
                }
            }

            // Lifetime and final explosive dispersal
            activeTicks++;
            if (activeTicks >= maxLifetime) {
                disperseTornado(trapped, serverLevel, charge);
                this.discard();
            }
        }
    }

    private void disperseTornado(List<LivingEntity> trapped, ServerLevel serverLevel, float charge) {
        // Dramatic finale: sound and shockwave burst
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 3.0f, 0.6f);
        serverLevel.sendParticles(ParticleTypes.GUST, this.getX(), this.getY() + 4.0, this.getZ(),
                25, 2.5, 4.0, 2.5, 0.2);

        // Hurl all airborne victims outwards with tremendous centrifugal force!
        for (LivingEntity victim : trapped) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            Vec3 flingDir = new Vec3(dx, 0.0, dz).normalize();
            if (flingDir.lengthSqr() < 0.01) {
                flingDir = new Vec3(this.random.nextDouble() - 0.5, 0.0, this.random.nextDouble() - 0.5).normalize();
            }
            double throwPower = 1.6 * charge;
            victim.push(flingDir.x * throwPower, 0.75 * throwPower, flingDir.z * throwPower);
            victim.hasImpulse = true;
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 dir = new Vec3(x, 0.0, z);
        if (dir.lengthSqr() > 0.001) {
            this.glideDirection = dir.normalize();
        }
    }
}
