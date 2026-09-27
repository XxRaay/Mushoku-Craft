package com.mushokucraft.network;

import com.mushokucraft.magic.AirCushionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ToggleAirCushionPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleAirCushionPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mushokucraft", "toggle_air_cushion"));
    public static final StreamCodec<FriendlyByteBuf, ToggleAirCushionPacket> STREAM_CODEC = StreamCodec.unit(new ToggleAirCushionPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleServer(ServerPlayer player) {
        AirCushionManager.toggle(player);
    }
}
