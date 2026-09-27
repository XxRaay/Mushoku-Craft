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

public class SoulAnchorCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "soul_anchor");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.soul_anchor");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.soul_anchor.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        return MushokuConfig.MAGIC_CIRCLE_SOUL_ANCHOR_MANA.get().floatValue();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        // Ethereal soul aura particles
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 0.25, cz, 4, 0.8, 0.2, 0.8, 0.03);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy + 0.4, cz, 5, 0.9, 0.3, 0.9, 0.08);
        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.5, cz, 6, 1.0, 0.3, 1.0, 0.05);

        if (level.getGameTime() % 14 == 0) {
            float pitch = 0.7f + (infusedSoFar / Math.max(1.0f, required)) * 0.7f;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8f, pitch);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.6f, pitch + 0.2f);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity originBE) {
            if (!originBE.isSoulAnchor()) {
                if (player != null) {
                    if (originBE.getSize() < 3) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_3x3"), true);
                    } else if (originBE.getAdditionalLayers().size() < 2) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_three_layers"), true);
                    } else if (!originBE.allLayersExpanded()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_layers_3x3"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_sealing_layer"), true);
                    }
                }
                return false;
            }

            level.playSound(null, origin, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.2f);
            level.playSound(null, origin, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8f, 1.6f);
            level.sendParticles(ParticleTypes.FLASH, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, origin.getX() + 0.5, origin.getY() + 0.4, origin.getZ() + 0.5, 30, 0.8, 0.4, 0.8, 0.1);
            return true;
        }
        return false;
    }

    @Override
    public void onServerTick(ServerLevel level, BlockPos pos, MagicCircleBlockEntity be) {
        if (be.isSoulAnchor() && be.getCurrentMana() >= MushokuConfig.MAGIC_CIRCLE_SOUL_ANCHOR_RECALL_COST.get()) {
            // Ambient dormant soul flame breathing
            if (level.getGameTime() % 25 == 0) {
                double cx = pos.getX() + 0.5;
                double cy = pos.getY() + 0.15;
                double cz = pos.getZ() + 0.5;
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy, cz, 2, 0.5, 0.1, 0.5, 0.02);
                if (be.getBoundPlayerUUID() != null) {
                    level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy + 0.2, cz, 3, 0.6, 0.2, 0.6, 0.05);
                }
            }
        }
    }
}
