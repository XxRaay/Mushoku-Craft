package com.mushokucraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CastStartedPacket(ResourceLocation spellId, int castTimeTicks, int fizzleTick) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<CastStartedPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"cast_started"));
    public static final StreamCodec<FriendlyByteBuf, CastStartedPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ResourceLocation.STREAM_CODEC, CastStartedPacket::spellId, (StreamCodec)ByteBufCodecs.INT, CastStartedPacket::castTimeTicks, (StreamCodec)ByteBufCodecs.INT, CastStartedPacket::fizzleTick, CastStartedPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    
}


