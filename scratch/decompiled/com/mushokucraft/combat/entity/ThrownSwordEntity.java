/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 */
package com.mushokucraft.combat.entity;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownSwordEntity
extends ThrowableItemProjectile {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrownSwordEntity.class, (EntityDataSerializer)EntityDataSerializers.ITEM_STACK);

    public ThrownSwordEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownSwordEntity(Level level, LivingEntity shooter, ItemStack swordStack) {
        super(ModEntities.THROWN_SWORD.get(), shooter, level);
        this.setSwordItem(swordStack.copy());
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM_STACK, (Object)ItemStack.EMPTY);
    }

    public void setSwordItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM_STACK, (Object)stack);
    }

    public ItemStack getSwordItem() {
        return (ItemStack)this.entityData.get(DATA_ITEM_STACK);
    }

    protected Item getDefaultItem() {
        return Items.IRON_SWORD;
    }

    public ItemStack getItem() {
        ItemStack stack = this.getSwordItem();
        return stack.isEmpty() ? new ItemStack((ItemLike)this.getDefaultItem()) : stack;
    }

    protected void onHitEntity(EntityHitResult result) {
        Entity entity;
        super.onHitEntity(result);
        if (!this.level().isClientSide && (entity = result.getEntity()) instanceof LivingEntity) {
            LivingEntity target = (LivingEntity)entity;
            entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                LivingEntity owner = (LivingEntity)entity;
                float damage = ((Double)MushokuConfig.THROWN_SWORD_DAMAGE.get()).floatValue();
                target.hurt(this.damageSources().thrown((Entity)this, (Entity)owner), damage);
                if (owner instanceof Player) {
                    Player player = (Player)owner;
                    ItemStack stack = this.getSwordItem();
                    if (!stack.isEmpty()) {
                        if (!player.getInventory().add(stack)) {
                            player.drop(stack, false);
                        } else {
                            player.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                        }
                    }
                } else {
                    this.dropSword();
                }
                this.discard();
            }
        }
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && result.getType() == HitResult.Type.BLOCK) {
            this.playSound(SoundEvents.ANVIL_PLACE, 0.5f, 1.2f);
            this.dropSword();
            this.discard();
        }
    }

    private void dropSword() {
        ItemStack stack = this.getSwordItem();
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
            itemEntity.setPickUpDelay(((Integer)MushokuConfig.THROWN_SWORD_PICKUP_DELAY.get()).intValue());
            this.level().addFreshEntity((Entity)itemEntity);
        }
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle((ParticleOptions)ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }
}

