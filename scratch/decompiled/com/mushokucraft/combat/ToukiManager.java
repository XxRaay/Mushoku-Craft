/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.combat;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.network.SyncToukiPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class ToukiManager {
    public static void toggleTouki(ServerPlayer player) {
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery != null) {
            SwordStyle stance;
            boolean newState;
            boolean bl = newState = !mastery.isToukiActive();
            if (newState && mastery.getMana() <= 0.0f) {
                return;
            }
            if (newState && ((stance = mastery.getActiveStance()) == null || mastery.getStanceMastery(stance) < 0.25f)) {
                return;
            }
            mastery.setToukiActive(newState);
            if (newState) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 2.0f);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 2.0f);
            }
            PacketDistributor.sendToPlayersTrackingEntityAndSelf((Entity)player, (CustomPacketPayload)new SyncToukiPacket(player.getId(), newState), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public static boolean tick(ServerPlayer player, PlayerMasteryData data) {
        if (!data.isToukiActive()) {
            return false;
        }
        float mastery = data.getStanceMastery(data.getActiveStance());
        float drain = ((Double)MushokuConfig.TOUKI_MANA_DRAIN_BASE.get()).floatValue() + ((Double)MushokuConfig.TOUKI_MANA_DRAIN_SCALING.get()).floatValue() * mastery;
        if (data.consumeMana(drain)) {
            return true;
        }
        data.setToukiActive(false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 2.0f);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf((Entity)player, (CustomPacketPayload)new SyncToukiPacket(player.getId(), false), (CustomPacketPayload[])new CustomPacketPayload[0]);
        return true;
    }
}

