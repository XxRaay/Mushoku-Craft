package com.mushokucraft.network;

import com.mushokucraft.client.network.ClientPayloadHandler;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;

public class ModNetworking {

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncManaPacket.TYPE, SyncManaPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncMana(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CastStartedPacket.TYPE, CastStartedPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleCastStarted(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CastSpellPacket.TYPE, CastSpellPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, StartChargePacket.TYPE, StartChargePacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ReleaseChargePacket.TYPE, ReleaseChargePacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, StartLearnSpellPacket.TYPE, StartLearnSpellPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, LearnSpellSyncPacket.TYPE, LearnSpellSyncPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleLearnSpellSync(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, QteTriggerPacket.TYPE, QteTriggerPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleQteTrigger(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, QteResultPacket.TYPE, QteResultPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, LearnSpellResultPacket.TYPE, LearnSpellResultPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleLearnSpellResult(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ChangeStancePacket.TYPE, ChangeStancePacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TriggerParryPacket.TYPE, TriggerParryPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ToggleToukiPacket.TYPE, ToggleToukiPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncToukiPacket.TYPE, SyncToukiPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncTouki(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ToggleAirCushionPacket.TYPE, ToggleAirCushionPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handleServer(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncAirCushionPacket.TYPE, SyncAirCushionPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> packet.handleClient());
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SyncFullMasteryPacket.TYPE, SyncFullMasteryPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleSyncFullMastery(packet));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, MasteryGainedPacket.TYPE, MasteryGainedPacket.STREAM_CODEC, (packet, context) -> {
            context.queue(() -> ClientPayloadHandler.handleMasteryGained(packet));
        });
    }
}
