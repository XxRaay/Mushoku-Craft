/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 */
package com.mushokucraft.magic.entity;

import com.mushokucraft.init.ModDamageTypes;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.magic.entity.AbstractMagicProjectileEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RockBulletEntity
extends AbstractMagicProjectileEntity {
    public int spinTicks = 0;

    public RockBulletEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.maxFlightTicks = 100;
    }

    public RockBulletEntity(Level pLevel, LivingEntity pShooter) {
        super(ModEntities.ROCK_BULLET.get(), pLevel, pShooter);
        this.maxFlightTicks = 100;
    }

    @Override
    public void tick() {
        float scale = this.getChargeScale();
        this.spinTicks += 10 + (int)(scale * 20.0f);
        super.tick();
    }

    @Override
    public float getSpin(float partialTicks) {
        return (float)this.spinTicks + partialTicks * (10.0f + this.getChargeScale() * 20.0f);
    }

    @Override
    public boolean scalesWithCharge() {
        return false;
    }

    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            float scale = this.getChargeScale();
            Entity entity = pResult.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity)entity;
                DamageSource source = new DamageSource((Holder)this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MAGIC), (Entity)this, this.getOwner());
                target.hurt(source, 8.0f * scale);
            }
            this.discard();
        }
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}

