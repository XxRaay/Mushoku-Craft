package com.mushokucraft.client.network;

import com.mushokucraft.magic.circle.ClientMagicCircleState;
import com.mushokucraft.network.CastEndedPacket;
import com.mushokucraft.network.CastStartedPacket;
import com.mushokucraft.network.LearnSpellResultPacket;
import com.mushokucraft.network.LearnSpellSyncPacket;
import com.mushokucraft.network.MasteryGainedPacket;
import com.mushokucraft.network.QteTriggerPacket;
import com.mushokucraft.network.SyncAirCushionPacket;
import com.mushokucraft.network.SyncFullMasteryPacket;
import com.mushokucraft.network.SyncMagicCirclesPacket;
import com.mushokucraft.network.SyncManaPacket;
import com.mushokucraft.network.SyncToukiPacket;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientNetworking {
    private static boolean registered = false;

    public static synchronized void register() {
        if (registered) return;
        registered = true;

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncManaPacket.TYPE, SyncManaPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncMana(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CastStartedPacket.TYPE, CastStartedPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleCastStarted(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, LearnSpellSyncPacket.TYPE, LearnSpellSyncPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleLearnSpellSync(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, QteTriggerPacket.TYPE, QteTriggerPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleQteTrigger(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, LearnSpellResultPacket.TYPE, LearnSpellResultPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleLearnSpellResult(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncToukiPacket.TYPE, SyncToukiPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncTouki(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncAirCushionPacket.TYPE, SyncAirCushionPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncAirCushion(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncFullMasteryPacket.TYPE, SyncFullMasteryPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncFullMastery(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, MasteryGainedPacket.TYPE, MasteryGainedPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleMasteryGained(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncMagicCirclesPacket.TYPE, SyncMagicCirclesPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientMagicCircleState.setClientWorldInfo(packet.worldSeed(), packet.teleportPatternIndex()));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CastEndedPacket.TYPE, CastEndedPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleCastEnded(packet));
        });
    }
}
