package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BarrierCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "barrier");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.barrier");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.barrier.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (level != null && level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            if (be.getSize() == 3) {
                return MushokuConfig.MAGIC_CIRCLE_BARRIER_3X3_MANA.get().floatValue();
            }
        }
        return MushokuConfig.MAGIC_CIRCLE_BARRIER_1X1_MANA.get().floatValue();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.25, cz, 4, 0.35, 0.1, 0.35, 0.05);
        level.sendParticles(ParticleTypes.WAX_ON, cx, cy + 0.35, cz, 2, 0.3, 0.15, 0.3, 0.02);

        if (level.getGameTime() % 15 == 0) {
            float pitch = 0.8f + (infusedSoFar / Math.max(1.0f, required)) * 0.8f;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, pitch);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity originBE) {
            if (player != null) {
                originBE.setOwnerUUID(player.getUUID());
            }
            boolean activated = originBE.activateBarrier(level);
            if (activated && player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_activated"), true);
            }
            return activated;
        }
        return false;
    }
}
