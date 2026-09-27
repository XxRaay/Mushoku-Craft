package com.mushokucraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CastEndedPacket(int reason) implements CustomPacketPayload {
    public static final int REASON_SUCCESS = 0;
    public static final int REASON_FIZZLE = 1;
    public static final int REASON_INTERRUPTED = 2;
    public static final int REASON_CANCELLED = 3;

    public static final CustomPacketPayload.Type<CastEndedPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mushokucraft", "cast_ended"));

    public static final StreamCodec<FriendlyByteBuf, CastEndedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    CastEndedPacket::reason,
                    CastEndedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
