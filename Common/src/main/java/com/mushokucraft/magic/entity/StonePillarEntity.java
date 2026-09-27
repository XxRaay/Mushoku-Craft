package com.mushokucraft.magic.entity;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.MagicSchool;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Stone Pillar (Advanced Earth Magic / 上級「岩柱」).
 * Erupts a massive monolithic pillar of compressed stone and bedrock from the earth.
 * Disrupts enemy formations, catapults enemies skyward, provides tactical high ground for the caster,
 * launches the caster into the air with slow falling, and acts as an impassable barrier against projectiles.
 */
public class StonePillarEntity extends AbstractMagicProjectileEntity {

    private static final EntityDataAccessor<Boolean> DATA_IS_ERUPTING =
            SynchedEntityData.defineId(StonePillarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ERUPTION_TICKS =
            SynchedEntityData.defineId(StonePillarEntity.class, EntityDataSerializers.INT);

    private static final int MAX_ERUPTION_TICKS = 80;
    private boolean casterLaunched = false;

    public StonePillarEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public StonePillarEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.STONE_PILLAR.get(), pLevel, pShooter);
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

                // Heavy tremors around target area
                if (this.level().isClientSide) {
                    float charge = this.getChargeScale();
                    double radius = MushokuConfig.STONE_PILLAR_RADIUS.get() * Math.min(1.8, 0.8 + 0.3 * charge);
                    for (int i = 0; i < 3; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2.0;
                        double dist = this.random.nextDouble() * radius;
                        double px = this.getX() + Math.cos(angle) * dist;
                        double pz = this.getZ() + Math.sin(angle) * dist;
                        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                                px, this.getY() + 0.05, pz, 0, 0.15, 0);
                        this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                px, this.getY() + 0.05, pz, 0, 0.05, 0);
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
            double radius = MushokuConfig.STONE_PILLAR_RADIUS.get() * Math.min(1.7, 0.85 + 0.25 * charge);
            double height = MushokuConfig.STONE_PILLAR_HEIGHT.get() * Math.min(1.7, 0.85 + 0.25 * charge);
            double knockup = MushokuConfig.STONE_PILLAR_KNOCKUP.get() * Math.min(1.6, 0.85 + 0.3 * charge);

            // Explosive Monolith Eruption on Tick 1
            if (ticks == 1) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.8f, 0.65f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 2.0f, 0.5f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 2.5f, 0.7f);

                if (!this.level().isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    AABB searchBox = new AABB(
                            this.getX() - radius, this.getY(), this.getZ() - radius,
                            this.getX() + radius, this.getY() + height, this.getZ() + radius
                    );

                    float baseDamage = MushokuConfig.STONE_PILLAR_BASE_DAMAGE.get().floatValue();
                    float finalDamage = AccessoryHelper.applyMagicDamageBonus(this.getOwner(), MagicSchool.EARTH, baseDamage * charge);
                    DamageSource damageSource = new DamageSource(
                            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
                            this, this.getOwner()
                    );

                    // 1. Launch & Damage Enemies
                    List<LivingEntity> enemies = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
                            e -> e != this.getOwner() && e.isAlive());
                    for (LivingEntity enemy : enemies) {
                        enemy.hurt(damageSource, finalDamage);
                        Vec3 vel = enemy.getDeltaMovement();
                        enemy.setDeltaMovement(vel.x * 0.1, knockup, vel.z * 0.1);
                        enemy.hasImpulse = true;
                    }

                    // Massive shockwave dust & boulder chunks
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                            this.getX(), this.getY() + 0.5, this.getZ(), 60, radius * 0.5, 0.5, radius * 0.5, 0.25);
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                            this.getX(), this.getY() + 1.5, this.getZ(), 50, radius * 0.6, 1.0, radius * 0.6, 0.2);
                }
            }

            // 2. Tactical Self-Launch for Caster (active during early surge: ticks 1 to 8)
            if (!this.level().isClientSide && !casterLaunched && ticks <= 8) {
                if (this.getOwner() instanceof Player caster && caster.isAlive()) {
                    double casterDistSq = caster.distanceToSqr(this.getX(), caster.getY(), this.getZ());
                    if (casterDistSq <= (radius + 2.0) * (radius + 2.0) &&
                            caster.getY() >= this.getY() - 1.5 && caster.getY() <= this.getY() + height + 3.0) {
                        casterLaunched = true;
                        double launchVel = Math.max(1.65, knockup * 1.35);

                        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0, false, false, true));
                        Vec3 cVel = caster.getDeltaMovement();
                        caster.setDeltaMovement(cVel.x * 0.25, launchVel, cVel.z * 0.25);
                        caster.hasImpulse = true;

                        if (caster instanceof ServerPlayer serverPlayer) {
                            serverPlayer.hurtMarked = true;
                            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer.getId(), caster.getDeltaMovement()));
                        }

                        if (this.level() instanceof ServerLevel serverLevel) {
                            serverLevel.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                                    SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 2.0f, 0.7f);
                            serverLevel.sendParticles(ParticleTypes.EXPLOSION, caster.getX(), caster.getY() + 0.2, caster.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
                            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                                    caster.getX(), caster.getY() + 0.1, caster.getZ(), 25, 0.5, 0.2, 0.5, 0.2);
                        }
                    }
                }
            }

            // Continuous tactical cover: intercept & disintegrate incoming projectiles inside the pillar
            if (!this.level().isClientSide && ticks % 2 == 0) {
                ServerLevel serverLevel = (ServerLevel) this.level();
                AABB pillarBox = new AABB(
                        this.getX() - radius * 0.85, this.getY(), this.getZ() - radius * 0.85,
                        this.getX() + radius * 0.85, this.getY() + height, this.getZ() + radius * 0.85
                );
                List<Projectile> projectiles = serverLevel.getEntitiesOfClass(Projectile.class, pillarBox,
                        p -> p != this && p.getOwner() != this.getOwner());
                for (Projectile proj : projectiles) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                            proj.getX(), proj.getY(), proj.getZ(), 10, 0.1, 0.1, 0.1, 0.1);
                    serverLevel.playSound(null, proj.getX(), proj.getY(), proj.getZ(), SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.8f, 1.2f);
                    proj.discard();
                }
            }

            // Client dust streams falling down the monolithic face
            if (this.level().isClientSide) {
                if (ticks < 15) {
                    for (int i = 0; i < 6; i++) {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double r = radius * (0.8 + 0.2 * this.random.nextDouble());
                        double h = this.random.nextDouble() * height;
                        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                                this.getX() + Math.cos(a) * r, this.getY() + h, this.getZ() + Math.sin(a) * r,
                                0, -0.2, 0);
                    }
                }
            } else {
                // End of pillar duration: catastrophic crumble into rubble
                if (ticks >= MAX_ERUPTION_TICKS) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                            this.getX(), this.getY() + height * 0.5, this.getZ(), 100, radius * 0.7, height * 0.4, radius * 0.7, 0.2);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 2.0f, 0.6f);
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

        // 0. Direct downward aim under feet (allows player to catapult themselves)
        if (owner.getXRot() > 30.0f || look.y < -0.55) {
            BlockHitResult feetHit = level.clip(new ClipContext(
                    owner.position().add(0, 1.0, 0),
                    owner.position().subtract(0, 6.0, 0),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
            ));
            if (feetHit.getType() != BlockHitResult.Type.MISS) {
                return feetHit.getLocation();
            }
            return owner.position();
        }

        Vec3 endPos = eyePos.add(look.scale(maxRange));

        // 1. Crosshair on target entity
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

        // 2. Block raycast
        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner
        ));
        if (blockHit.getType() != BlockHitResult.Type.MISS) {
            return blockHit.getLocation();
        }

        // 3. Downward scan
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
