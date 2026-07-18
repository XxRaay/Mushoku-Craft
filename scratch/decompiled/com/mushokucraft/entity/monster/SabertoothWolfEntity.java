/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.AnimationState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.mushokucraft.entity.monster;

import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import com.mushokucraft.init.ModEntities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SabertoothWolfEntity
extends Monster {
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
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.2, false));
        this.goalSelector.addGoal(3, (Goal)new FollowPackLeaderGoal(this, this));
        this.goalSelector.addGoal(4, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0));
        this.goalSelector.addGoal(5, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, (Goal)new RandomLookAroundGoal((Mob)this));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.attackAnimationTimeout > 0) {
                --this.attackAnimationTimeout;
            }
            if (this.damageAnimationTimeout > 0) {
                --this.damageAnimationTimeout;
            }
            if (this.isDeadOrDying()) {
                if (this.deathAnimationTimeout <= 0) {
                    this.deathAnimationTimeout = 12;
                }
                --this.deathAnimationTimeout;
            }
            this.setupAnimationStates();
        } else {
            if (this.attackAnimationTimeout > 0) {
                --this.attackAnimationTimeout;
            }
            if (this.damageAnimationTimeout > 0) {
                --this.damageAnimationTimeout;
            }
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
        }
        this.deathAnimationState.stop();
        if (this.damageAnimationTimeout > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            return;
        }
        this.damageAnimationState.stop();
        if (this.attackAnimationTimeout > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            return;
        }
        this.attackAnimationState.stop();
        if (this.walkAnimation.speed() > 0.01f) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    protected void updateWalkAnimation(float v) {
        float f = this.getPose() == Pose.STANDING ? Math.min(v * 6.0f, 1.0f) : 0.0f;
        this.walkAnimation.update(f, 0.2f);
    }

    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackAnimationTimeout = 12;
            this.attackAnimationState.start(this.tickCount);
        } else if (id == 2) {
            this.damageAnimationTimeout = 5;
            this.damageAnimationState.start(this.tickCount);
        }
        super.handleEntityEvent(id);
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 12 && !this.level().isClientSide()) {
            DeadSabertoothWolfEntity deadWolf = new DeadSabertoothWolfEntity(ModEntities.DEAD_SABERTOOTH_WOLF.get(), this.level());
            deadWolf.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            deadWolf.setYBodyRot(this.yBodyRot);
            deadWolf.setYHeadRot(this.yHeadRot);
            this.level().addFreshEntity((Entity)deadWolf);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity prevTarget = this.getTarget();
        super.setTarget(target);
        if (target != null && prevTarget != target && !this.level().isClientSide()) {
            List nearbyWolves = this.level().getEntitiesOfClass(SabertoothWolfEntity.class, this.getBoundingBox().inflate(16.0));
            for (SabertoothWolfEntity wolf : nearbyWolves) {
                if (wolf == this || wolf.getTarget() == target) continue;
                wolf.setTarget(target);
            }
        }
    }

    class FollowPackLeaderGoal
    extends Goal {
        private final SabertoothWolfEntity wolf;
        private SabertoothWolfEntity leader;
        private int timeToRecalcPath;

        public FollowPackLeaderGoal(SabertoothWolfEntity this$0, SabertoothWolfEntity wolf) {
            this.wolf = wolf;
        }

        public boolean canUse() {
            if (this.wolf.getTarget() != null) {
                return false;
            }
            List list = this.wolf.level().getEntitiesOfClass(SabertoothWolfEntity.class, this.wolf.getBoundingBox().inflate(8.0, 4.0, 8.0));
            SabertoothWolfEntity foundLeader = null;
            for (SabertoothWolfEntity w : list) {
                if (w == this.wolf || w.getTarget() != null) continue;
                foundLeader = w;
                break;
            }
            if (foundLeader == null) {
                return false;
            }
            if (this.wolf.distanceToSqr((Entity)foundLeader) < 25.0) {
                return false;
            }
            this.leader = foundLeader;
            return true;
        }

        public boolean canContinueToUse() {
            return this.leader != null && this.leader.isAlive() && this.wolf.distanceToSqr((Entity)this.leader) > 9.0 && this.wolf.getTarget() == null;
        }

        public void start() {
            this.timeToRecalcPath = 0;
        }

        public void stop() {
            this.leader = null;
            this.wolf.getNavigation().stop();
        }

        public void tick() {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                this.wolf.getNavigation().moveTo((Entity)this.leader, 1.0);
            }
        }
    }
}

