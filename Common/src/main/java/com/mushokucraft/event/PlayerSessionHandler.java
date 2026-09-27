package com.mushokucraft.event;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.network.SyncFullMasteryPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import dev.architectury.networking.NetworkManager;
import dev.architectury.event.events.common.PlayerEvent;
// import com.mushokucraft.attachment.ModAttachments;

public class PlayerSessionHandler {
    public static void register() {
        PlayerEvent.PLAYER_JOIN.register((player) -> {
            if (player instanceof ServerPlayer) {
                ServerPlayer player2 = (ServerPlayer)player;
                PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player2);
                ModGameEvents.syncMana(player2, data);
                NetworkManager.sendToPlayer((ServerPlayer)player2, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player2.level().registryAccess())));
                long seed = player2.serverLevel().getSeed();
                int patternIdx = com.mushokucraft.magic.circle.MagicCirclePatterns.getTeleportPatternIndexForSeed(seed);
                NetworkManager.sendToPlayer(player2, new com.mushokucraft.network.SyncMagicCirclesPacket(seed, patternIdx));
            }
        });

        PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) -> {
            if (newPlayer instanceof ServerPlayer) {
                ServerPlayer serverNewPlayer = (ServerPlayer)newPlayer;
                PlayerMasteryData oldData = PlayerMasteryProvider.get(oldPlayer);
                PlayerMasteryData newData = PlayerMasteryProvider.get(serverNewPlayer);
                if (oldData != null && newData != null) {
                    newData.deserializeNBT(serverNewPlayer.level().registryAccess(), oldData.serializeNBT(oldPlayer.level().registryAccess()));
                    if (!wonGame) {
                        newData.setToukiActive(false);
                    }
                    ModGameEvents.syncMana(serverNewPlayer, newData);
                }
            }
        });

        PlayerEvent.PLAYER_RESPAWN.register(new PlayerEvent.PlayerRespawn() {
            @Override
            public void respawn(ServerPlayer player, boolean wonGame, net.minecraft.world.entity.Entity.RemovalReason reason) {
                PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                NetworkManager.sendToPlayer(player, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player.level().registryAccess())));
                long seed = player.serverLevel().getSeed();
                int patternIdx = com.mushokucraft.magic.circle.MagicCirclePatterns.getTeleportPatternIndexForSeed(seed);
                NetworkManager.sendToPlayer(player, new com.mushokucraft.network.SyncMagicCirclesPacket(seed, patternIdx));
            }
        });
    }
}
