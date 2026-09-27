package com.mushokucraft.magic.entity;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CumulonimbusStormEntity extends AbstractMagicProjectileEntity {

    private int tickCounter = 0;

    public CumulonimbusStormEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public CumulonimbusStormEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.CUMULONIMBUS_STORM.get(), pLevel, pShooter);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (this.isCharging()) {
            // Keep the entity slightly above the owner's position
            if (this.getOwner() != null) {
                this.setPos(this.getOwner().getX(), this.getOwner().getEyeY() + 0.1, this.getOwner().getZ());
            }

            if (!this.level().isClientSide) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    if (!serverLevel.isRaining()) {
                        serverLevel.setWeatherParameters(0, 6000, true, true);
                    }
                }
                
                tickCounter++;
                int interval = MushokuConfig.CUMULONIMBUS_STRIKE_INTERVAL_TICKS.get();
                
                // Strike every 'interval' ticks.
                if (tickCounter >= interval) {
                    tickCounter = 0;
                    strikeLightning();
                }
            }
        } else {
            // If it's not charging (released), it should immediately dissipate.
            this.discard();
        }
    }

    private void strikeLightning() {
        double radius = MushokuConfig.CUMULONIMBUS_RADIUS.get();
        AABB searchBox = this.getBoundingBox().inflate(radius, 50.0, radius);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, 
            e -> e != this.getOwner() && e.isAlive());

        Vec3 strikePos;
        if (!targets.isEmpty() && this.random.nextBoolean()) {
            // 50% chance to strike a random entity if any exist
            LivingEntity target = targets.get(this.random.nextInt(targets.size()));
            strikePos = target.position();
        } else {
            // Otherwise strike a random block in radius
            double offsetX = (this.random.nextDouble() - 0.5) * radius * 2;
            double offsetZ = (this.random.nextDouble() - 0.5) * radius * 2;
            BlockPos highest = this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, 
                this.blockPosition().offset((int)offsetX, 0, (int)offsetZ));
            strikePos = highest.getCenter();
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                lightning.moveTo(strikePos);
                lightning.setVisualOnly(true); // Don't cause fire or vanilla damage, we apply custom damage
                serverLevel.addFreshEntity(lightning);
            }
        }

        // Apply custom magic damage to entities near the strike
        float damage = MushokuConfig.CUMULONIMBUS_LIGHTNING_DAMAGE.get().floatValue();
        DamageSource source = new DamageSource(
            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC),
            this, this.getOwner()
        );
        
        List<LivingEntity> hitEntities = this.level().getEntitiesOfClass(LivingEntity.class, 
            new AABB(strikePos.x, strikePos.y, strikePos.z, strikePos.x, strikePos.y, strikePos.z).inflate(3.0), e -> e != this.getOwner() && e.isAlive());
        for (LivingEntity e : hitEntities) {
            e.hurt(source, damage);
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        // Overridden to prevent the entity from being shot. It just discards.
        this.discard();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(6000, 0, false, false);
        }
        super.remove(reason);
    }
}
