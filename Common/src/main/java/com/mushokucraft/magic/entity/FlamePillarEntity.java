package com.mushokucraft.magic.entity;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.MagicSchool;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Flame Pillar (Intermediate Fire Magic / 中級「火柱」).
 * Erupts a roaring vertical pillar of incinerating flame from beneath the ground,
 * launching enemies into the air, burning them continuously, and disintegrating incoming projectiles.
 */
public class FlamePillarEntity extends AbstractMagicProjectileEntity {

    private static final EntityDataAccessor<Boolean> DATA_IS_ERUPTING =
            SynchedEntityData.defineId(FlamePillarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ERUPTION_TICKS =
            SynchedEntityData.defineId(FlamePillarEntity.class, EntityDataSerializers.INT);

    private boolean hasSnappedToGround = false;
    private static final int MAX_ERUPTION_TICKS = 32;

    public FlamePillarEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public FlamePillarEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.FLAME_PILLAR.get(), pLevel, pShooter);
        this.setNoGravity(true);
        Vec3 groundPos = calculateTargetPos(pShooter, pLevel, 24.0);
        this.setPos(groundPos.x, groundPos.y + 0.02, groundPos.z);
        this.hasSnappedToGround = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_IS_ERUPTING, false);
        pBuilder.define(DATA_ERUPTION_TICKS, 0);
    }

    public boolean isErupting() {
        return this.entityData.get(DATA_IS_ERUPTING);
    }

    public void setErupting(boolean erupting) {
        this.entityData.set(DATA_IS_ERUPTING, erupting);
    }

    public int getEruptionTicks() {
        return this.entityData.get(DATA_ERUPTION_TICKS);
    }

    public void setEruptionTicks(int ticks) {
        this.entityData.set(DATA_ERUPTION_TICKS, ticks);
    }

    public boolean hasSnappedToGround() {
        return this.hasSnappedToGround;
    }

    public void startEruption() {
        this.setCharging(false);
        this.setErupting(true);
        this.setEruptionTicks(0);
    }

    @Override
    public MagicSchool getMagicSchool() {
        return MagicSchool.FIRE;
    }

    @Override
    protected boolean customTickLogic() {
        return true; // We manage ground locking and eruption ourselves
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isCharging()) {
            LivingEntity owner = (LivingEntity) this.getOwner();
            if (owner != null && owner.isAlive()) {
                Vec3 groundPos = calculateTargetPos(owner, this.level(), 24.0);
                this.setPos(groundPos.x, groundPos.y + 0.02, groundPos.z);
                this.setDeltaMovement(Vec3.ZERO);
                this.hasSnappedToGround = true;

                // Charging particles on client around the ground rune
                if (this.level().isClientSide) {
                    float charge = this.getChargeScale();
                    double radius = MushokuConfig.FLAME_PILLAR_RADIUS.get() * Math.min(1.8, 0.8 + 0.3 * charge);
                    double angle = this.random.nextDouble() * Math.PI * 2.0;
                    double dist = this.random.nextDouble() * radius;
                    double px = this.getX() + Math.cos(angle) * dist;
                    double pz = this.getZ() + Math.sin(angle) * dist;
                    this.level().addParticle(ParticleTypes.FLAME, px, this.getY() + 0.04, pz, 0, 0.02, 0);
                    if (this.random.nextFloat() < 0.2f) {
                        this.level().addParticle(ParticleTypes.LAVA, px, this.getY() + 0.04, pz, 0, 0, 0);
                    }
                }
            } else if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        if (this.isErupting()) {
            int ticks = this.getEruptionTicks() + 1;
            this.setEruptionTicks(ticks);

            float charge = this.getChargeScale();
            double radius = MushokuConfig.FLAME_PILLAR_RADIUS.get() * Math.min(1.8, 0.8 + 0.3 * charge);
            double height = MushokuConfig.FLAME_PILLAR_HEIGHT.get() * Math.min(1.8, 0.8 + 0.3 * charge);

            // First tick sound effects
            if (ticks == 1) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.5f, 0.8f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0f, 0.6f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, 1.7f);
            }

            // Visual effects
            if (this.level().isClientSide) {
                // Ground geyser lava sparks
                for (int i = 0; i < 4; i++) {
                    double angle = this.random.nextDouble() * Math.PI * 2.0;
                    double dist = this.random.nextDouble() * radius;
                    double px = this.getX() + Math.cos(angle) * dist;
                    double pz = this.getZ() + Math.sin(angle) * dist;
                    this.level().addParticle(ParticleTypes.LAVA, px, this.getY() + 0.1, pz, (this.random.nextDouble() - 0.5) * 0.1, 0.45, (this.random.nextDouble() - 0.5) * 0.1);
                }

                // Vertical flame torrent rushing upwards
                for (int i = 0; i < 14; i++) {
                    double h = this.random.nextDouble() * (height * 0.85);
                    double angle = (ticks * 0.45) + (h * 0.4) + (i * (Math.PI / 7.0));
                    double r = radius * (0.3 + 0.7 * (1.0 - h / height));
                    double px = this.getX() + Math.cos(angle) * r;
                    double pz = this.getZ() + Math.sin(angle) * r;
                    this.level().addParticle(ParticleTypes.FLAME, px, this.getY() + h, pz, 0, 0.35 + this.random.nextDouble() * 0.35, 0);
                    if (this.random.nextFloat() < 0.25f) {
                        this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, this.getY() + h + 1.0, pz, 0, 0.25, 0);
                    }
                }
            } else {
                // Combat tick every 4 ticks
                if (ticks % 4 == 0) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    AABB searchBox = new AABB(
                            this.getX() - radius, this.getY(), this.getZ() - radius,
                            this.getX() + radius, this.getY() + height, this.getZ() + radius
                    );

                    float baseDamage = MushokuConfig.FLAME_PILLAR_BASE_DAMAGE.get().floatValue();
                    float finalDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.FIRE, baseDamage * charge);
                    DamageSource damageSource = new DamageSource(
                            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                            this, this.getOwner()
                    );

                    List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
                            e -> e != this.getOwner() && e.isAlive());

                    for (LivingEntity target : targets) {
                        double distSq = (target.getX() - this.getX()) * (target.getX() - this.getX()) +
                                        (target.getZ() - this.getZ()) * (target.getZ() - this.getZ());
                        if (distSq <= radius * radius) {
                            target.hurt(damageSource, finalDamage);
                            target.igniteForSeconds((int) (6 + 3 * charge));

                            // Upward launch impulse: "испепеляющий противника снизу вверх"
                            double knockup = MushokuConfig.FLAME_PILLAR_KNOCKUP.get() * Math.min(1.6, 0.8 + 0.3 * charge);
                            Vec3 vel = target.getDeltaMovement();
                            target.setDeltaMovement(vel.x * 0.3, knockup, vel.z * 0.3);
                            target.hasImpulse = true;
                        }
                    }

                    // Disintegrate incoming projectiles inside the pillar: "преграждающий путь группе врагов"
                    List<Projectile> projectiles = serverLevel.getEntitiesOfClass(Projectile.class, searchBox,
                            p -> p != this && p.getOwner() != this.getOwner());
                    for (Projectile proj : projectiles) {
                        serverLevel.sendParticles(ParticleTypes.SMOKE, proj.getX(), proj.getY(), proj.getZ(), 8, 0.1, 0.1, 0.1, 0.05);
                        serverLevel.playSound(null, proj.getX(), proj.getY(), proj.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 1.5f);
                        proj.discard();
                    }
                }

                if (ticks >= MAX_ERUPTION_TICKS) {
                    this.discard();
                }
            }
        }
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        if (this.getOwner() instanceof LivingEntity owner) {
            Vec3 groundPos = calculateTargetPos(owner, this.level(), 24.0);
            this.setPos(groundPos.x, groundPos.y + 0.02, groundPos.z);
            this.setDeltaMovement(Vec3.ZERO);
            this.hasSnappedToGround = true;
            this.startEruption();
        }
    }

    public static Vec3 calculateTargetPos(LivingEntity owner, Level level, double maxRange) {
        Vec3 eyePos = owner.getEyePosition();
        Vec3 look = owner.getLookAngle();
        Vec3 endPos = eyePos.add(look.scale(maxRange));

        // 1. Target entity under crosshair
        AABB searchBox = owner.getBoundingBox().expandTowards(look.scale(maxRange)).inflate(1.5);
        net.minecraft.world.phys.EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                level, owner, eyePos, endPos, searchBox,
                e -> !e.isSpectator() && e.isAlive() && e != owner
        );
        if (entityHit != null && entityHit.getEntity() != null) {
            Entity target = entityHit.getEntity();
            BlockHitResult downHit = level.clip(new ClipContext(
                    target.position().add(0, 1.0, 0),
                    target.position().subtract(0, 10.0, 0),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
            ));
            if (downHit.getType() == HitResult.Type.BLOCK) {
                return downHit.getLocation();
            }
            return target.position();
        }

        // 2. Direct block hit along crosshair line of sight (ground, hill, slope)
        BlockHitResult blockHit = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation();
        }

        // 3. Fallback when looking into air/horizon: project forward and raycast down to ground
        double horizLen = Math.sqrt(look.x * look.x + look.z * look.z);
        double forwardDist = horizLen > 0.05 ? maxRange : 4.0;
        double dirX = horizLen > 0.05 ? (look.x / horizLen) : 0.0;
        double dirZ = horizLen > 0.05 ? (look.z / horizLen) : 1.0;

        double targetX = eyePos.x + dirX * forwardDist;
        double targetZ = eyePos.z + dirZ * forwardDist;

        Vec3 topCheck = new Vec3(targetX, eyePos.y + 6.0, targetZ);
        Vec3 bottomCheck = new Vec3(targetX, eyePos.y - 48.0, targetZ);
        BlockHitResult groundHit = level.clip(new ClipContext(topCheck, bottomCheck, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (groundHit.getType() == HitResult.Type.BLOCK) {
            return groundHit.getLocation();
        }

        return new Vec3(targetX, owner.getY(), targetZ);
    }
}
