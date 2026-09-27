package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DimensionalGateCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "dimensional_gate");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.dimensional_gate");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.dimensional_gate.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        return MushokuConfig.MAGIC_CIRCLE_DIMENSIONAL_GATE_MANA.get().floatValue();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        // Grand interdimensional vortex particles
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, cx, cy + 0.3, cz, 6, 1.2, 0.2, 1.2, 0.1);
        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.5, cz, 8, 1.0, 0.4, 1.0, 0.15);
        level.sendParticles(ParticleTypes.END_ROD, cx, cy + 0.4, cz, 2, 0.8, 0.3, 0.8, 0.04);
        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.6, cz, 5, 1.1, 0.3, 1.1, 0.1);

        if (level.getGameTime() % 12 == 0) {
            float pitch = 0.6f + (infusedSoFar / Math.max(1.0f, required)) * 0.8f;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.8f, pitch);
            level.playSound(null, pos, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5f, 1.3f);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        if (destination == null) return false;

        MagicCircleBlockEntity originBE = null;
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            originBE = be;
        }

        if (originBE == null || !originBE.isDimensionalGate()) {
            if (player != null) {
                if (originBE != null && originBE.getSize() < 3) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_3x3"), true);
                } else if (originBE != null && originBE.getAdditionalLayers().isEmpty()) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers"), true);
                } else if (originBE != null && !originBE.allLayersExpanded()) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers_3x3"), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_not_multi_layered"), true);
                }
            }
            return false;
        }

        // Validate destination
        if (!originBE.isDestinationValid(level)) {
            originBE.setLinked(null, null);
            originBE.setCurrentMana(0.0f);
            originBE.setChanged();
            level.sendBlockUpdated(origin, level.getBlockState(origin), level.getBlockState(origin), 3);
            level.playSound(null, origin, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0f, 0.5f);
            level.sendParticles(ParticleTypes.SMOKE, origin.getX() + 0.5, origin.getY() + 0.2, origin.getZ() + 0.5, 20, 0.8, 0.2, 0.8, 0.05);
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.circle_target_missing"), true);
            }
            return false;
        }

        // Target level
        ServerLevel destLevel = level;
        if (originBE.getLinkedDim() != null && !originBE.getLinkedDim().equals(level.dimension().location())) {
            ServerLevel other = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, originBE.getLinkedDim()));
            if (other != null) {
                destLevel = other;
            }
        }

        // Force chunk load
        if (!destLevel.hasChunk(destination.getX() >> 4, destination.getZ() >> 4)) {
            destLevel.getChunk(destination.getX() >> 4, destination.getZ() >> 4);
        }

        if (!(destLevel.getBlockEntity(destination) instanceof MagicCircleBlockEntity destBE) || !destBE.isDimensionalGate()) {
            originBE.setLinked(null, null);
            originBE.setCurrentMana(0.0f);
            originBE.setChanged();
            level.sendBlockUpdated(origin, level.getBlockState(origin), level.getBlockState(origin), 3);
            level.playSound(null, origin, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0f, 0.5f);
            level.sendParticles(ParticleTypes.SMOKE, origin.getX() + 0.5, origin.getY() + 0.2, origin.getZ() + 0.5, 20, 0.8, 0.2, 0.8, 0.05);
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_target_not_multilayer"), true);
            }
            return false;
        }

        double halfSize = 1.5;
        double origX = origin.getX() + 0.5;
        double origY = origin.getY() + 0.05;
        double origZ = origin.getZ() + 0.5;

        double destX = destination.getX() + 0.5;
        double destY = destination.getY() + 0.05;
        double destZ = destination.getZ() + 0.5;

        // Spectacular dimensional rift FX at origin
        level.sendParticles(ParticleTypes.FLASH, origX, origY + 1.0, origZ, 3, 0.2, 0.2, 0.2, 0.0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, origX, origY + 0.8, origZ, 120, halfSize, 0.8, halfSize, 0.4);
        level.sendParticles(ParticleTypes.PORTAL, origX, origY + 0.8, origZ, 80, halfSize, 0.6, halfSize, 0.25);
        level.sendParticles(ParticleTypes.END_ROD, origX, origY + 1.0, origZ, 40, halfSize * 0.8, 0.8, halfSize * 0.8, 0.15);
        level.playSound(null, origin, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.2f, 1.2f);
        level.playSound(null, origin, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.6f);

        // Entities standing on 3x3 platform
        AABB box = new AABB(origX - halfSize, origin.getY(), origZ - halfSize,
                origX + halfSize, origin.getY() + 2.5, origZ + halfSize);
        List<Entity> standingEntities = level.getEntities((Entity) null, box, e -> e.isAlive() && !(e instanceof ItemEntity));

        Set<Entity> toTeleport = new LinkedHashSet<>();
        for (Entity e : standingEntities) {
            toTeleport.add(e.getRootVehicle());
        }
        if (player != null) {
            toTeleport.add(player.getRootVehicle());
        }

        for (Entity entity : toTeleport) {
            double relX = entity.getX() - origX;
            double relZ = entity.getZ() - origZ;
            relX = Math.max(-halfSize, Math.min(halfSize, relX));
            relZ = Math.max(-halfSize, Math.min(halfSize, relZ));

            double targetX = destX + relX;
            double targetY = destY;
            double targetZ = destZ + relZ;

            if (destLevel == level) {
                entity.teleportTo(targetX, targetY, targetZ);
            } else if (entity instanceof ServerPlayer sp) {
                sp.teleportTo(destLevel, targetX, targetY, targetZ, sp.getYRot(), sp.getXRot());
            } else {
                entity.changeDimension(new DimensionTransition(
                        destLevel,
                        new Vec3(targetX, targetY, targetZ),
                        entity.getDeltaMovement(),
                        entity.getYRot(),
                        entity.getXRot(),
                        DimensionTransition.DO_NOTHING
                ));
            }
            entity.fallDistance = 0.0f;
            entity.resetFallDistance();
        }

        // Spectacular destination FX
        destLevel.sendParticles(ParticleTypes.FLASH, destX, destY + 1.0, destZ, 3, 0.2, 0.2, 0.2, 0.0);
        destLevel.sendParticles(ParticleTypes.PORTAL, destX, destY + 0.8, destZ, 120, halfSize, 0.8, halfSize, 0.4);
        destLevel.sendParticles(ParticleTypes.ENCHANT, destX, destY + 0.8, destZ, 80, halfSize, 0.8, halfSize, 0.25);
        destLevel.sendParticles(ParticleTypes.END_ROD, destX, destY + 1.0, destZ, 40, halfSize * 0.8, 0.8, halfSize * 0.8, 0.15);
        destLevel.playSound(null, destination, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.2f, 1.2f);
        destLevel.playSound(null, destination, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.8f);

        return true;
    }
}
