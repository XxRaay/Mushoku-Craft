package com.mushokucraft.magic;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.network.SyncAirCushionPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class AirCushionManager {
    private static final ResourceLocation AIR_CUSHION_SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "air_cushion_speed");

    public static void toggle(ServerPlayer player) {
        PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
        if (mastery != null) {
            boolean newState = !mastery.isAirCushionActive();
            if (newState && mastery.getMana() <= 0) {
                return;
            }
            mastery.setAirCushionActive(newState);
            
            if (newState) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0f, 1.5f);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);
                removeSpeedBoost(player);
            }
            
            NetworkManager.sendToPlayers(player.getServer().getPlayerList().getPlayers(), new SyncAirCushionPacket(player.getId(), newState));
        }
    }

    public static boolean tick(ServerPlayer player, PlayerMasteryData data) {
        if (!data.isAirCushionActive()) {
            removeSpeedBoost(player);
            return false;
        }

        Level level = player.level();
        float mastery = data.getSchoolMastery(MagicSchool.WIND);
        
        // Every second (20 ticks), consume mana and give mastery
        if (player.tickCount % 20 == 0) {
            // Find distance to real ground (not magical blocks)
            BlockPos currentPos = player.blockPosition();
            BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, currentPos);
            int distance = Math.max(0, currentPos.getY() - groundPos.getY());
            
            // Mana cost scales with height
            float drain = MushokuConfig.AIR_CUSHION_MANA_DRAIN_BASE.get().floatValue() 
                        + (distance * MushokuConfig.AIR_CUSHION_MANA_DRAIN_HEIGHT_MULT.get().floatValue());
            
            if (data.consumeMana(drain)) {
                data.addSchoolMastery(MagicSchool.WIND, MushokuConfig.AIR_CUSHION_MASTERY_GAIN.get().floatValue());
            } else {
                // Not enough mana
                data.setAirCushionActive(false);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);
                NetworkManager.sendToPlayers(player.getServer().getPlayerList().getPlayers(), new SyncAirCushionPacket(player.getId(), false));
                removeSpeedBoost(player);
                return true;
            }
        }

        // Spawn blocks if in air or already on a cushion
        BlockPos center = player.blockPosition();
        boolean shouldSpawn = !player.onGround() || level.getBlockState(center).is(ModBlocks.AIR_CUSHION.get());
        
        if (shouldSpawn) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos posSpawn = center.offset(dx, 0, dz);
                    BlockState stateSpawn = level.getBlockState(posSpawn);
                    
                    // Replace empty space or reset timer for existing cushion
                    if (stateSpawn.canBeReplaced() && !stateSpawn.is(ModBlocks.AIR_CUSHION.get())) {
                        level.setBlock(posSpawn, ModBlocks.AIR_CUSHION.get().defaultBlockState(), 3);
                        if (level.getBlockEntity(posSpawn) instanceof com.mushokucraft.block.entity.AirCushionBlockEntity cushion) {
                            cushion.setOwner(player.getUUID());
                        }
                    } else if (stateSpawn.is(ModBlocks.AIR_CUSHION.get())) {
                        if (level.getBlockEntity(posSpawn) instanceof com.mushokucraft.block.entity.AirCushionBlockEntity cushion) {
                            cushion.resetTicksLived();
                        }
                    }
                }
            }
        }

        // Apply speed boost if standing on the block
        BlockPos standingPos = player.blockPosition();
        if (player.onGround() && level.getBlockState(standingPos).is(ModBlocks.AIR_CUSHION.get())) {
            applySpeedBoost(player, mastery);
        } else {
            removeSpeedBoost(player);
        }

        return false;
    }

    private static void applySpeedBoost(ServerPlayer player, float mastery) {
        AttributeInstance moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null && moveSpeedAttr.getModifier(AIR_CUSHION_SPEED_MOD_ID) == null) {
            double boost = MushokuConfig.AIR_CUSHION_SPEED_MULT_BASE.get() + (mastery * (MushokuConfig.AIR_CUSHION_SPEED_MULT_MAX.get() - MushokuConfig.AIR_CUSHION_SPEED_MULT_BASE.get()));
            moveSpeedAttr.addTransientModifier(new AttributeModifier(AIR_CUSHION_SPEED_MOD_ID, boost, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private static void removeSpeedBoost(ServerPlayer player) {
        AttributeInstance moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.removeModifier(AIR_CUSHION_SPEED_MOD_ID);
        }
    }
}
