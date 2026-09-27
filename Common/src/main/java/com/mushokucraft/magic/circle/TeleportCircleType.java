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

public class TeleportCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "teleportation");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.teleportation");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.teleportation.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (destination == null) return 999999.0f;
        double dx = origin.getX() - destination.getX();
        double dy = origin.getY() - destination.getY();
        double dz = origin.getZ() - destination.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return (float) (MushokuConfig.MAGIC_CIRCLE_TELEPORT_BASE_MANA.get() + dist * MushokuConfig.MAGIC_CIRCLE_TELEPORT_MANA_PER_BLOCK.get());
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        // Swirling particles
        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.2, cz, 4, 0.35, 0.1, 0.35, 0.05);
        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.3, cz, 3, 0.4, 0.2, 0.4, 0.1);

        if (level.getGameTime() % 15 == 0) {
            float pitch = 0.8f + (infusedSoFar / Math.max(1.0f, required)) * 0.8f;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.6f, pitch);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        if (destination == null) return false;

        MagicCircleBlockEntity originBE = null;
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            originBE = be;
        }

        // 1. Destination validation: target must be an intact, matching magic circle
        if (originBE != null && !originBE.isDestinationValid(level)) {
            originBE.setLinked(null, null);
            originBE.setCurrentMana(0.0f);
            originBE.setChanged();
            level.sendBlockUpdated(origin, level.getBlockState(origin), level.getBlockState(origin), 3);
            level.playSound(null, origin, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0f, 0.5f);
            level.sendParticles(ParticleTypes.SMOKE, origin.getX() + 0.5, origin.getY() + 0.2, origin.getZ() + 0.5, 15, 0.2, 0.1, 0.2, 0.05);
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.circle_target_missing"), true);
            }
            return false;
        }

        // Determine destination level (cross-dimension support if linkedDim differs)
        ServerLevel destLevel = level;
        if (originBE != null && originBE.getLinkedDim() != null && !originBE.getLinkedDim().equals(level.dimension().location())) {
            ServerLevel other = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, originBE.getLinkedDim()));
            if (other != null) {
                destLevel = other;
            }
        }

        // Verify destination chunk loaded
        if (!destLevel.hasChunk(destination.getX() >> 4, destination.getZ() >> 4)) {
            destLevel.getChunk(destination.getX() >> 4, destination.getZ() >> 4);
        }

        int originSize = (originBE != null) ? originBE.getSize() : 1;
        double halfSize = (originSize == 3) ? 1.5 : 0.5;

        double origX = origin.getX() + 0.5;
        double origY = origin.getY() + 0.05;
        double origZ = origin.getZ() + 0.5;

        double destX = destination.getX() + 0.5;
        double destY = destination.getY() + 0.05;
        double destZ = destination.getZ() + 0.5;

        // Origin visual & sound burst
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, origX, origY + 0.5, origZ, 60, halfSize, 0.5, halfSize, 0.25);
        level.sendParticles(ParticleTypes.PORTAL, origX, origY + 0.5, origZ, 40, halfSize, 0.4, halfSize, 0.15);
        level.playSound(null, origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.2f, 1.0f);
        level.playSound(null, origin, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.4f);

        // 2. Teleport ALL entities standing on the origin circle (scaled by circle size)
        AABB box = new AABB(origX - halfSize, origin.getY(), origZ - halfSize,
                            origX + halfSize, origin.getY() + 1.5, origZ + halfSize);
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

        // Destination visual & sound burst
        destLevel.sendParticles(ParticleTypes.PORTAL, destX, destY + 0.5, destZ, 60, halfSize, 0.5, halfSize, 0.25);
        destLevel.sendParticles(ParticleTypes.ENCHANT, destX, destY + 0.5, destZ, 40, halfSize, 0.5, halfSize, 0.15);
        destLevel.playSound(null, destination, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.2f, 1.0f);
        destLevel.playSound(null, destination, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8f, 1.6f);

        return true;
    }
}
