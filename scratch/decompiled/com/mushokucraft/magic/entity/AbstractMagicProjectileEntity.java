/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.event.EventHooks
 */
package com.mushokucraft.magic.entity;

import com.mushokucraft.magic.entity.IMagicProjectile;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

public abstract class AbstractMagicProjectileEntity
extends Projectile
implements IMagicProjectile {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(AbstractMagicProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_CHARGE_SCALE = SynchedEntityData.defineId(AbstractMagicProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(AbstractMagicProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    public int flightTicks = 0;
    protected int maxFlightTicks = 200;

    protected AbstractMagicProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected AbstractMagicProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel, LivingEntity pShooter) {
        super(pEntityType, pLevel);
        this.setOwner((Entity)pShooter);
        this.setPos(pShooter.getX(), pShooter.getEyeY() - 0.1, pShooter.getZ());
    }

    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_IS_CHARGING, (Object)false);
        pBuilder.define(DATA_CHARGE_SCALE, (Object)Float.valueOf(1.0f));
        pBuilder.define(DATA_OWNER_ID, (Object)-1);
    }

    public void setOwner(Entity pOwner) {
        super.setOwner(pOwner);
        if (pOwner != null) {
            this.entityData.set(DATA_OWNER_ID, (Object)pOwner.getId());
        }
    }

    public Entity getOwner() {
        int ownerId;
        Entity owner = super.getOwner();
        if (owner == null && this.level().isClientSide && (ownerId = ((Integer)this.entityData.get(DATA_OWNER_ID)).intValue()) != -1) {
            return this.level().getEntity(ownerId);
        }
        return owner;
    }

    @Override
    public void setCharging(boolean charging) {
        this.entityData.set(DATA_IS_CHARGING, (Object)charging);
    }

    public boolean isCharging() {
        return (Boolean)this.entityData.get(DATA_IS_CHARGING);
    }

    @Override
    public void setChargeScale(float scale) {
        this.entityData.set(DATA_CHARGE_SCALE, (Object)Float.valueOf(scale));
    }

    @Override
    public float getChargeScale() {
        return ((Float)this.entityData.get(DATA_CHARGE_SCALE)).floatValue();
    }

    public boolean isNoGravity() {
        return true;
    }

    public void tick() {
        super.tick();
        if (this.isCharging()) {
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                LivingEntity owner = (LivingEntity)entity;
                Vec3 look = owner.getLookAngle();
                Vec3 pos = owner.getEyePosition().add(look.scale(1.2)).subtract(0.0, 0.4, 0.0);
                this.setPos(pos.x, pos.y, pos.z);
                this.setDeltaMovement(Vec3.ZERO);
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
                this.setYRot(owner.getYRot());
                this.setXRot(owner.getXRot());
                this.onChargingTick();
            } else if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }
        if (this.customTickLogic()) {
            return;
        }
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector((Entity)this, x$0 -> this.canHitEntity((Entity)x$0));
        if (hitresult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact((Projectile)this, (HitResult)hitresult)) {
            this.onHit(hitresult);
        }
        this.checkInsideBlocks();
        Vec3 vec3 = this.getDeltaMovement();
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        this.onFlightTick(d0, d1, d2, vec3);
        this.setPos(d0, d1, d2);
        if (!this.isCharging()) {
            ++this.flightTicks;
            if (this.flightTicks > this.maxFlightTicks) {
                this.discard();
            }
        }
    }

    protected void onChargingTick() {
    }

    protected boolean customTickLogic() {
        return false;
    }

    public float getSpin(float partialTicks) {
        return 0.0f;
    }

    public boolean scalesWithCharge() {
        return true;
    }

    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {
    }
}

