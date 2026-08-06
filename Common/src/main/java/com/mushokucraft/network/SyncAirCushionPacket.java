package com.mushokucraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncAirCushionPacket(int entityId, boolean isActive) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncAirCushionPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mushokucraft", "sync_air_cushion"));
    public static final StreamCodec<ByteBuf, SyncAirCushionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncAirCushionPacket::entityId,
            ByteBufCodecs.BOOL, SyncAirCushionPacket::isActive,
            SyncAirCushionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClient() {
        com.mushokucraft.client.network.ClientPayloadHandler.handleSyncAirCushion(this);
    }
}
