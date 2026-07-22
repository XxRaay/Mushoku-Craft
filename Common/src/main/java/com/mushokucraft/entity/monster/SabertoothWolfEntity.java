package com.mushokucraft.entity.monster;

import com.mushokucraft.init.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.damagesource.DamageSource;

public class SabertoothWolfEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState damageAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int attackAnimationTimeout = 0;
    private int damageAnimationTimeout = 0;
    private int deathAnimationTimeout = 0;

    public SabertoothWolfEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new FollowPackLeaderGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.attackAnimationTimeout > 0) this.attackAnimationTimeout--;
            if (this.damageAnimationTimeout > 0) this.damageAnimationTimeout--;
            
            if (this.isDeadOrDying()) {
                if (this.deathAnimationTimeout <= 0) {
                    this.deathAnimationTimeout = 12;
                }
                this.deathAnimationTimeout--;
            }

            setupAnimationStates();
        } else {
            if (this.attackAnimationTimeout > 0) this.attackAnimationTimeout--;
            if (this.damageAnimationTimeout > 0) this.damageAnimationTimeout--;
        }
    }

    private void setupAnimationStates() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.damageAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        } else {
            this.deathAnimationState.stop();
        }

        if (this.damageAnimationTimeout > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            return;
        } else {
            this.damageAnimationState.stop();
        }

        if (this.attackAnimationTimeout > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            return;
        } else {
            this.attackAnimationState.stop();
        }

        if (this.walkAnimation.speed() > 0.01F) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    @Override
    protected void updateWalkAnimation(float v) {
        float f;
        if (this.getPose() == net.minecraft.world.entity.Pose.STANDING) {
            f = Math.min(v * 6.0F, 1.0F);
        } else {
            f = 0.0F;
        }
        this.walkAnimation.update(f, 0.2F);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) { // Vanilla attack event
            this.attackAnimationTimeout = 12;
            this.attackAnimationState.start(this.tickCount);
        } else if (id == 2) { // Vanilla hurt event
            this.damageAnimationTimeout = 5;
            this.damageAnimationState.start(this.tickCount);
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 12) { // Death animation is 0.5833s (approx 12 ticks)
            if (!this.level().isClientSide()) {
                DeadSabertoothWolfEntity deadWolf = new DeadSabertoothWolfEntity(ModEntities.DEAD_SABERTOOTH_WOLF.get(), this.level());
                deadWolf.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                deadWolf.setYBodyRot(this.yBodyRot);
                deadWolf.setYHeadRot(this.yHeadRot);
                this.level().addFreshEntity(deadWolf);
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity target) {
        net.minecraft.world.entity.LivingEntity prevTarget = this.getTarget();
        super.setTarget(target);
        if (target != null && prevTarget != target && !this.level().isClientSide()) {
            java.util.List<SabertoothWolfEntity> nearbyWolves = this.level().getEntitiesOfClass(
                SabertoothWolfEntity.class, this.getBoundingBox().inflate(16.0D));
            for (SabertoothWolfEntity wolf : nearbyWolves) {
                if (wolf != this && wolf.getTarget() != target) {
                    wolf.setTarget(target);
                }
            }
        }
    }

    class FollowPackLeaderGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final SabertoothWolfEntity wolf;
        private SabertoothWolfEntity leader;
        private int timeToRecalcPath;

        public FollowPackLeaderGoal(SabertoothWolfEntity wolf) {
            this.wolf = wolf;
        }

        @Override
        public boolean canUse() {
            if (this.wolf.getTarget() != null) return false;
            java.util.List<SabertoothWolfEntity> list = this.wolf.level().getEntitiesOfClass(SabertoothWolfEntity.class, this.wolf.getBoundingBox().inflate(8.0D, 4.0D, 8.0D));
            SabertoothWolfEntity foundLeader = null;
            for (SabertoothWolfEntity w : list) {
                if (w != this.wolf && w.getTarget() == null) {
                    foundLeader = w;
                    break;
                }
            }
            if (foundLeader == null) return false;
            if (this.wolf.distanceToSqr(foundLeader) < 25.0D) return false; // Don't start if already within 5 blocks
            this.leader = foundLeader;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            // Stop following if we get closer than 3 blocks (9.0D sqr)
            return this.leader != null && this.leader.isAlive() && this.wolf.distanceToSqr(this.leader) > 9.0D && this.wolf.getTarget() == null;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.leader = null;
            this.wolf.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                this.wolf.getNavigation().moveTo(this.leader, 1.0D);
            }
        }
    }
}





