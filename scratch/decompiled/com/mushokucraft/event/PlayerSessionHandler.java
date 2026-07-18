/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.event;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.network.SyncFullMasteryPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="mushokucraft")
public class PlayerSessionHandler {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            PlayerMasteryData data = (PlayerMasteryData)player2.getData(ModAttachments.PLAYER_MASTERY);
            ModGameEvents.syncMana(player2, data);
            PacketDistributor.sendToPlayer((ServerPlayer)player2, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player2.level().registryAccess())), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer newPlayer = (ServerPlayer)player;
            PlayerMasteryData oldData = (PlayerMasteryData)event.getOriginal().getData(ModAttachments.PLAYER_MASTERY);
            if (event.isWasDeath()) {
                oldData.setToukiActive(false);
            }
            newPlayer.setData(ModAttachments.PLAYER_MASTERY, (Object)oldData);
            ModGameEvents.syncMana(newPlayer, oldData);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            PlayerMasteryData data = (PlayerMasteryData)player2.getData(ModAttachments.PLAYER_MASTERY);
            PacketDistributor.sendToPlayer((ServerPlayer)player2, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player2.level().registryAccess())), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }
}

