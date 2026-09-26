package com.mushokucraft.magic.circle;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface MagicCircleType {
    ResourceLocation getId();
    Component getDisplayName();
    Component getDescription();

    float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination);
    void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required);
    boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player);

    default void onServerTick(ServerLevel level, BlockPos pos, com.mushokucraft.block.entity.MagicCircleBlockEntity be) {}
}
