package com.mushokucraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MasteryGainedPacket(float amount) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<MasteryGainedPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"mastery_gained"));
    public static final StreamCodec<FriendlyByteBuf, MasteryGainedPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.FLOAT, MasteryGainedPacket::amount, MasteryGainedPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    
}


