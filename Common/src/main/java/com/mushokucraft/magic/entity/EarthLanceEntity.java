package com.mushokucraft.magic.entity;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.MagicSchool;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Earth Lance (Intermediate Earth Magic / 中級「岩槍」).
 * Erupts a sharp, dense, armor-piercing stone spike from beneath the enemy's feet,
 * impaling and pinning them to the earth with massive physical penetration.
 */
public class EarthLanceEntity extends AbstractMagicProjectileEntity {

    private static final EntityDataAccessor<Boolean> DATA_IS_ERUPTING =
            SynchedEntityData.defineId(EarthLanceEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ERUPTION_TICKS =
            SynchedEntityData.defineId(EarthLanceEntity.class, EntityDataSerializers.INT);

    private static final int MAX_ERUPTION_TICKS = 26;

    public EarthLanceEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public EarthLanceEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.EARTH_LANCE.get(), pLevel, pShooter);
        this.setNoGravity(true);
        Vec3 groundPos = calculateTargetPos(pShooter, pLevel, 24.0);
        this.setPos(groundPos.x, groundPos.y + 0.02, groundPos.z);
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

    public void startEruption() {
        this.setCharging(false);
        this.setErupting(true);
        this.setEruptionTicks(0);
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

        if (this.isCharging()) {
            LivingEntity owner = (LivingEntity) this.getOwner();
            if (owner != null && owner.isAlive()) {
                Vec3 groundPos = calculateTargetPos(owner, this.level(), 24.0);
                this.setPos(groundPos.x, groundPos.y + 0.02, groundPos.z);
                this.setDeltaMovement(Vec3.ZERO);

                // Charging visual rumble at target spot
                if (this.level().isClientSide) {
                    float charge = this.getChargeScale();
                    double radius = MushokuConfig.EARTH_LANCE_RADIUS.get() * Math.min(1.8, 0.8 + 0.3 * charge);
                    double angle = this.random.nextDouble() * Math.PI * 2.0;
                    double dist = this.random.nextDouble() * radius;
                    double px = this.getX() + Math.cos(angle) * dist;
                    double pz = this.getZ() + Math.sin(angle) * dist;

                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                            px, this.getY() + 0.05, pz, 0, 0.08, 0);
                    if (this.random.nextFloat() < 0.35f) {
                        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                                px, this.getY() + 0.05, pz, 0, 0.12, 0);
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
            double radius = MushokuConfig.EARTH_LANCE_RADIUS.get() * Math.min(1.6, 0.85 + 0.25 * charge);
            double height = 3.6 * Math.min(1.6, 0.85 + 0.25 * charge);

            // Explosive thrust on tick 1
            if (ticks == 1) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 2.5f, 0.75f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2f, 1.4f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.5f, 0.6f);

                if (!this.level().isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    AABB searchBox = new AABB(
                            this.getX() - radius, this.getY(), this.getZ() - radius,
                            this.getX() + radius, this.getY() + height, this.getZ() + radius
                    );

                    float baseDamage = MushokuConfig.EARTH_LANCE_BASE_DAMAGE.get().floatValue();
                    float finalDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.EARTH, baseDamage * charge);
                    DamageSource damageSource = new DamageSource(
                            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                            this, this.getOwner()
                    );

                    List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
                            e -> e != this.getOwner() && e.isAlive());

                    for (LivingEntity target : targets) {
                        target.hurt(damageSource, finalDamage);
                        // Impale pin / slowdown (physical spear pinning through limbs)
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, false, true));

                        // Upward impale jolt
                        Vec3 vel = target.getDeltaMovement();
                        target.setDeltaMovement(vel.x * 0.2, 0.48 * Math.min(1.5, 0.8 + 0.3 * charge), vel.z * 0.2);
                        target.hasImpulse = true;
                    }

                    // Shockwave dust and stone chunks
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                            this.getX(), this.getY() + 0.2, this.getZ(), 35, 0.6, 0.4, 0.6, 0.18);
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            this.getX(), this.getY() + 1.2, this.getZ(), 20, 0.4, 0.6, 0.4, 0.2);
                }
            }

            // Client effects during eruption
            if (this.level().isClientSide) {
                if (ticks <= 6) {
                    for (int i = 0; i < 4; i++) {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double d = this.random.nextDouble() * radius;
                        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                                this.getX() + Math.cos(a) * d, this.getY() + 0.1, this.getZ() + Math.sin(a) * d,
                                (this.random.nextDouble() - 0.5) * 0.15, 0.25, (this.random.nextDouble() - 0.5) * 0.15);
                    }
                }
            } else {
                // Crumble at the end of the duration
                if (ticks >= MAX_ERUPTION_TICKS) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                            this.getX(), this.getY() + 1.0, this.getZ(), 25, 0.4, 0.6, 0.4, 0.1);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_FALL, SoundSource.BLOCKS, 1.2f, 1.1f);
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
            this.startEruption();
        }
    }

    public static Vec3 calculateTargetPos(LivingEntity owner, Level level, double maxRange) {
        Vec3 eyePos = owner.getEyePosition();
        Vec3 look = owner.getLookAngle();
        Vec3 endPos = eyePos.add(look.scale(maxRange));

        // 1. Raycast for enemy under crosshair
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
            if (downHit.getType() != BlockHitResult.Type.MISS) {
                return downHit.getLocation();
            }
            return target.position();
        }

        // 2. Raycast for ground block
        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
        ));
        if (blockHit.getType() != BlockHitResult.Type.MISS) {
            return blockHit.getLocation();
        }

        // 3. Fallback: project forward and find terrain floor
        BlockHitResult floorHit = level.clip(new ClipContext(
                endPos.add(0, 5.0, 0),
                endPos.subtract(0, 30.0, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
        ));
        if (floorHit.getType() != BlockHitResult.Type.MISS) {
            return floorHit.getLocation();
        }

        return endPos;
    }
}
