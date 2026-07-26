package com.mushokucraft.magic.entity;

import com.mushokucraft.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class IcicleBreakTargetEntity extends AbstractMagicProjectileEntity {
    private int icicleTicks = 0;
    private boolean wasEverCharging = false;
    private boolean hasSnappedToGround = false;

    public IcicleBreakTargetEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public IcicleBreakTargetEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.ICICLE_BREAK_TARGET.get(), pLevel, pShooter);
        this.setNoGravity(true);
    }

    public boolean hasSnappedToGround() {
        return this.hasSnappedToGround;
    }

    public void setSnappedToGround(boolean snapped) {
        this.hasSnappedToGround = snapped;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
    }

    @Override
    public void tick() {
        // Save the real position before AbstractMagicProjectileEntity.tick() forces it to the player's eyes
        double realX = this.getX();
        double realY = this.getY();
        double realZ = this.getZ();

        super.tick();
        
        if (this.isCharging()) {
            this.wasEverCharging = true;
            
            // Restore the real position
            this.setPos(realX, realY, realZ);
            
            // Keep the target circle on the ground where the player is aiming while charging
            if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                net.minecraft.world.phys.Vec3 endPos = eyePos.add(look.scale(7.0));
                net.minecraft.world.phys.HitResult result = this.level().clip(new net.minecraft.world.level.ClipContext(eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                net.minecraft.world.phys.Vec3 hitPos = result.getLocation();
                net.minecraft.world.phys.HitResult groundResult = this.level().clip(new net.minecraft.world.level.ClipContext(hitPos, hitPos.add(0, -64, 0), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                net.minecraft.world.phys.Vec3 groundPos = groundResult.getLocation();
                
                if (!this.hasSnappedToGround) {
                    this.setPos(groundPos.x, groundPos.y + 0.05, groundPos.z);
                    this.hasSnappedToGround = true;
                } else {
                    double lerpFactor = 0.3;
                    double newX = this.getX() + (groundPos.x - this.getX()) * lerpFactor;
                    double newZ = this.getZ() + (groundPos.z - this.getZ()) * lerpFactor;
                    this.setPos(newX, groundPos.y + 0.05, newZ);
                }
            }
        } else {
            if (!this.level().isClientSide) {
                if (this.wasEverCharging) {
                    // This was a Silent Cast that was just released. It should stop immediately.
                    this.discard();
                    return;
                }
                
                // This is a Regular Cast that just spawned.
                icicleTicks++;
                if (icicleTicks > 25) {
                    this.discard();
                    return;
                }
            }
        }

        if (!this.level().isClientSide) {
            // Spawn icicles continuously
            if (this.tickCount % 5 == 0) { // 2 icicles per 5 ticks
                for (int i = 0; i < 2; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 3.0; // 3x3 area (-1.5 to +1.5 blocks)
                    double offsetZ = (this.random.nextDouble() - 0.5) * 3.0;
                    double spawnY = this.getY() + 6.0 + this.random.nextDouble() * 2.0;
                    
                    IcicleEntity icicle = new IcicleEntity(this.level(), this.getOwner() instanceof LivingEntity ? (LivingEntity)this.getOwner() : null);
                    icicle.setPos(this.getX() + offsetX, spawnY, this.getZ() + offsetZ);
                    icicle.setDeltaMovement(0, -1.0, 0); // Fall down fast
                    this.level().addFreshEntity(icicle);
                }
            }
        }
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
            net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            net.minecraft.world.phys.Vec3 endPos = eyePos.add(look.scale(7.0));
            net.minecraft.world.phys.HitResult result = this.level().clip(new net.minecraft.world.level.ClipContext(eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
            net.minecraft.world.phys.Vec3 hitPos = result.getLocation();
            net.minecraft.world.phys.HitResult groundResult = this.level().clip(new net.minecraft.world.level.ClipContext(hitPos, hitPos.add(0, -64, 0), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
            net.minecraft.world.phys.Vec3 groundPos = groundResult.getLocation();
            this.setPos(groundPos.x, groundPos.y + 0.05, groundPos.z);
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        // Do nothing
    }

    @Override
    protected boolean customTickLogic() {
        // Skip default projectile logic (flight ticks, movement) on client side
        return this.level().isClientSide;
    }
}
