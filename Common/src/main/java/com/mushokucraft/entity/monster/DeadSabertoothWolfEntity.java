package com.mushokucraft.entity.monster;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.entity.AbstractCarcassEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.storage.loot.LootTable;

public class DeadSabertoothWolfEntity extends AbstractCarcassEntity {
    public final AnimationState deadAnimationState = new AnimationState();
    
    public static final ResourceKey<LootTable> CARCASS_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "entities/dead_sabertooth_wolf_carcass")
    );

    public DeadSabertoothWolfEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.deadAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    protected ResourceKey<LootTable> getCarcassLootTable() {
        return CARCASS_LOOT;
    }

    @Override
    protected void dropCarcassLoot(net.minecraft.world.entity.player.Player player) {
        super.dropCarcassLoot(player);
        if (this.random.nextFloat() < com.mushokucraft.config.MushokuConfig.SABERTOOTH_CORE_DROP_CHANCE.get()) {
            this.spawnAtLocation(com.mushokucraft.init.ModItems.SABERTOOTH_CORE.get().getDefaultInstance());
            this.level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.4f);
        }
    }
}






