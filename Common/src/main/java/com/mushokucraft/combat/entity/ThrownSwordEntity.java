package com.mushokucraft.combat.entity;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownSwordEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrownSwordEntity.class, EntityDataSerializers.ITEM_STACK);

    public ThrownSwordEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownSwordEntity(Level level, LivingEntity shooter, ItemStack swordStack) {
        super(ModEntities.THROWN_SWORD.get(), shooter, level);
        this.setSwordItem(swordStack.copy());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    public void setSwordItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM_STACK, stack);
    }

    public ItemStack getSwordItem() {
        return this.entityData.get(DATA_ITEM_STACK);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.IRON_SWORD;
    }

    @Override
    public ItemStack getItem() {
        ItemStack stack = getSwordItem();
        return stack.isEmpty() ? new ItemStack(getDefaultItem()) : stack;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            if (result.getEntity() instanceof LivingEntity target && this.getOwner() instanceof LivingEntity owner) {
                // Calculate damage based on the sword
                float damage = MushokuConfig.THROWN_SWORD_DAMAGE.get().floatValue();
                ItemStack swordStack = getSwordItem();
                if (swordStack.getItem() instanceof com.mushokucraft.weapon.modular.ModularWeaponItem) {
                    damage += com.mushokucraft.weapon.modular.ModularWeaponItem.getWeaponDamage(swordStack) * 0.6f;
                } else if (swordStack.getItem() instanceof net.minecraft.world.item.TieredItem ti) {
                    damage += (ti.getTier().getAttackDamageBonus() + 3.0f) * 0.6f;
                }
                target.hurt(this.damageSources().thrown(this, owner), damage);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, MushokuConfig.NORTH_GOD_SLOW_DURATION.get(), 1));
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.mushokucraft.init.ModEffects.BLEEDING, MushokuConfig.NORTH_GOD_BLEED_DURATION.get(), 0));
                
                // Return sword to owner if they are a player
                if (owner instanceof net.minecraft.world.entity.player.Player player) {
                    ItemStack stack = getSwordItem();
                    if (!stack.isEmpty()) {
                        if (!player.getInventory().add(stack)) {
                            player.drop(stack, false);
                        } else {
                            player.playSound(SoundEvents.ITEM_PICKUP, 0.5f, 1.0f);
                        }
                    }
                } else {
                    dropSword();
                }
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (result.getType() == HitResult.Type.BLOCK) {
                this.playSound(SoundEvents.ANVIL_PLACE, 0.5f, 1.2f);
                dropSword();
                this.discard();
            }
        }
    }
    
    private void dropSword() {
        ItemStack stack = getSwordItem();
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
            itemEntity.setPickUpDelay(MushokuConfig.THROWN_SWORD_PICKUP_DELAY.get());
            this.level().addFreshEntity(itemEntity);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }
}





