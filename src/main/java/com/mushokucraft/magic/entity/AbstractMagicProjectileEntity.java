package com.mushokucraft.magic.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMagicProjectileEntity extends Projectile implements IMagicProjectile {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_IS_CHARGING = net.minecraft.network.syncher.SynchedEntityData.defineId(AbstractMagicProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> DATA_CHARGE_SCALE = net.minecraft.network.syncher.SynchedEntityData.defineId(AbstractMagicProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_OWNER_ID = net.minecraft.network.syncher.SynchedEntityData.defineId(AbstractMagicProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    public int flightTicks = 0;
    protected int maxFlightTicks = 200;

    protected AbstractMagicProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected AbstractMagicProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel, LivingEntity pShooter) {
        super(pEntityType, pLevel);
        this.setOwner(pShooter);
        this.setPos(pShooter.getX(), pShooter.getEyeY() - 0.1D, pShooter.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_IS_CHARGING, false);
        pBuilder.define(DATA_CHARGE_SCALE, 1.0f);
        pBuilder.define(DATA_OWNER_ID, -1);
    }
    
    @Override
    public void setOwner(net.minecraft.world.entity.Entity pOwner) {
        super.setOwner(pOwner);
        if (pOwner != null) {
            this.entityData.set(DATA_OWNER_ID, pOwner.getId());
        }
    }
    
    @Override
    public net.minecraft.world.entity.Entity getOwner() {
        net.minecraft.world.entity.Entity owner = super.getOwner();
        if (owner == null && this.level().isClientSide) {
            int ownerId = this.entityData.get(DATA_OWNER_ID);
            if (ownerId != -1) {
                return this.level().getEntity(ownerId);
            }
        }
        return owner;
    }
    
    public void setCharging(boolean charging) {
        this.entityData.set(DATA_IS_CHARGING, charging);
    }
    
    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }
    
    public void setChargeScale(float scale) {
        this.entityData.set(DATA_CHARGE_SCALE, scale);
    }
    
    public float getChargeScale() {
        return this.entityData.get(DATA_CHARGE_SCALE);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isCharging()) {
            if (this.getOwner() instanceof LivingEntity owner) {
                Vec3 look = owner.getLookAngle();
                Vec3 pos = owner.getEyePosition().add(look.scale(1.2)).subtract(0, 0.4, 0);
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

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult)) {
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
            this.flightTicks++;
            if (this.flightTicks > this.maxFlightTicks) {
                this.discard();
            }
        }
    }

    protected void onChargingTick() {}

    /**
     * @return true to skip default flight logic
     */
    protected boolean customTickLogic() {
        return false;
    }

    public float getSpin(float partialTicks) {
        return 0f;
    }

    public boolean scalesWithCharge() {
        return true;
    }

    protected void onFlightTick(double d0, double d1, double d2, Vec3 vec3) {}
}





