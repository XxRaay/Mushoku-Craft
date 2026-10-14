package com.mushokucraft.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.server.level.ServerPlayer;

public class ModNetworking {

    public static void register() {
        // Register client receivers on client environment only
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.mushokucraft.client.network.ClientNetworking.register());

        // Server receivers (C2S)
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
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, QteResultPacket.TYPE, QteResultPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
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
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ToggleAirCushionPacket.TYPE, ToggleAirCushionPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handleServer(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, InscribeCanvasPacket.TYPE, InscribeCanvasPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ConsumeCanvasInkPacket.TYPE, ConsumeCanvasInkPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SaveCanvasDraftPacket.TYPE, SaveCanvasDraftPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ForgeWeaponPacket.TYPE, ForgeWeaponPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CancelCastPacket.TYPE, CancelCastPacket.STREAM_CODEC, (packet, context) -> {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                context.queue(() -> packet.handle(sp));
            }
        });
    }
}
