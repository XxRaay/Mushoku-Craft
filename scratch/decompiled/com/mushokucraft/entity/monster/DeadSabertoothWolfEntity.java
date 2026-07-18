/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.world.entity.AnimationState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.Level
 */
package com.mushokucraft.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DeadSabertoothWolfEntity
extends Entity {
    public final AnimationState deadAnimationState = new AnimationState();
    private int deathTime = 0;

    public DeadSabertoothWolfEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
        this.deathTime = tag.getInt("DeathTime");
    }

    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("DeathTime", this.deathTime);
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.deadAnimationState.startIfStopped(this.tickCount);
        } else {
            ++this.deathTime;
            if (this.deathTime > 6000) {
                this.discard();
            }
        }
    }
}

