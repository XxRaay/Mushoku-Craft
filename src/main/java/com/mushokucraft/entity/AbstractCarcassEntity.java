package com.mushokucraft.entity;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public abstract class AbstractCarcassEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_USES = SynchedEntityData.defineId(AbstractCarcassEntity.class, EntityDataSerializers.INT);
    protected int deathTime = 0;

    public AbstractCarcassEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_USES, MushokuConfig.CARCASS_MAX_USES.get());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.deathTime = tag.getInt("DeathTime");
        if (tag.contains("Uses")) {
            this.setUses(tag.getInt("Uses"));
        } else {
            this.setUses(MushokuConfig.CARCASS_MAX_USES.get());
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("DeathTime", this.deathTime);
        tag.putInt("Uses", this.getUses());
    }

    public int getUses() {
        return this.entityData.get(DATA_USES);
    }

    public void setUses(int uses) {
        this.entityData.set(DATA_USES, uses);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.deathTime++;
            if (this.deathTime > 6000) { // 5 minutes
                this.discard();
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.is(ModItems.HUNTING_KNIFE.get())) {
            if (!this.level().isClientSide()) {
                if (!player.getCooldowns().isOnCooldown(itemInHand.getItem())) {
                    player.getCooldowns().addCooldown(itemInHand.getItem(), 40); // 2 seconds

                    itemInHand.hurtAndBreak(1, player, Player.getSlotForHand(hand));

                    int currentUses = this.getUses();
                    if (currentUses > 1) {
                        this.setUses(currentUses - 1);
                        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        spawnScrapingParticles();
                    } else {
                        spawnScrapingParticles();
                        dropCarcassLoot(player);
                        this.discard();
                    }
                }
            } else {
                if (!player.getCooldowns().isOnCooldown(itemInHand.getItem())) {
                    spawnScrapingParticles();
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.interact(player, hand);
    }

    protected void spawnScrapingParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
            serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
        } else {
            for (int i = 0; i < 5; ++i) {
                this.level().addParticle(ParticleTypes.CRIT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    protected void dropCarcassLoot(Player player) {
        ResourceKey<LootTable> lootTableKey = this.getCarcassLootTable();
        if (lootTableKey != null && this.level() instanceof ServerLevel serverLevel) {
            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);
            LootParams.Builder builder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().playerAttack(player));

            LootParams lootParams = builder.create(LootContextParamSets.ENTITY);
            List<ItemStack> loot = lootTable.getRandomItems(lootParams);
            for (ItemStack stack : loot) {
                this.spawnAtLocation(stack);
            }
        }
    }

    protected abstract ResourceKey<LootTable> getCarcassLootTable();
}





