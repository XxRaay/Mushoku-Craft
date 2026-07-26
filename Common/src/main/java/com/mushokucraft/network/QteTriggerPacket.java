package com.mushokucraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record QteTriggerPacket(String keyLetter, float speedModifier, float targetSizeModifier, float perfectMultiplier) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<QteTriggerPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"qte_trigger"));
    public static final StreamCodec<FriendlyByteBuf, QteTriggerPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.STRING_UTF8, QteTriggerPacket::keyLetter, (StreamCodec)ByteBufCodecs.FLOAT, QteTriggerPacket::speedModifier, (StreamCodec)ByteBufCodecs.FLOAT, QteTriggerPacket::targetSizeModifier, (StreamCodec)ByteBufCodecs.FLOAT, QteTriggerPacket::perfectMultiplier, QteTriggerPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    
}


