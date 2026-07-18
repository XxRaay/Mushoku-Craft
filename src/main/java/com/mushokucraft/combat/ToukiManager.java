package com.mushokucraft.combat;

/**
 * Manages Touki (Battle Aura) state and bonuses per sword style.
 * <p>
 * Touki activates via the radial menu ('R'), drains mana per second,
 * and provides bonuses that scale with the active sword style's mastery level.
 * Each style has unique Touki bonuses.
 */
public class ToukiManager {

    public static void toggleTouki(net.minecraft.server.level.ServerPlayer player) {
        com.mushokucraft.data.PlayerMasteryData mastery = player.getData(com.mushokucraft.init.ModAttachments.PLAYER_MASTERY);
        if (mastery != null) {
            boolean newState = !mastery.isToukiActive();
            if (newState && mastery.getMana() <= 0) {
                return; // Cannot activate without mana
            }
            if (newState) {
                SwordStyle stance = mastery.getActiveStance();
                if (stance == null || mastery.getStanceMastery(stance) < 0.25f) {
                    return; // Cannot activate without 25% mastery
                }
            }
            mastery.setToukiActive(newState);
            
            if (newState) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 2.0f);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 2.0f);
            }
            
            // Broadcast to tracking clients and self
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new com.mushokucraft.network.SyncToukiPacket(player.getId(), newState));
        }
    }

    public static boolean tick(net.minecraft.server.level.ServerPlayer player, com.mushokucraft.data.PlayerMasteryData data) {
        if (!data.isToukiActive()) return false;

        float mastery = data.getStanceMastery(data.getActiveStance());
        float drain = com.mushokucraft.config.MushokuConfig.TOUKI_MANA_DRAIN_BASE.get().floatValue() + com.mushokucraft.config.MushokuConfig.TOUKI_MANA_DRAIN_SCALING.get().floatValue() * mastery;
        
        if (data.consumeMana(drain)) {
            return true; // syncNeeded
        } else {
            // Not enough mana, deactivate Touki
            data.setToukiActive(false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 2.0f);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new com.mushokucraft.network.SyncToukiPacket(player.getId(), false));
            return true; // syncNeeded
        }
    }
}





