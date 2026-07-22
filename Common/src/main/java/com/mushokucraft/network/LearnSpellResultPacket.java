package com.mushokucraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LearnSpellResultPacket(boolean success, ResourceLocation spellId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<LearnSpellResultPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"learn_spell_result"));
    public static final StreamCodec<FriendlyByteBuf, LearnSpellResultPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.BOOL, LearnSpellResultPacket::success, (StreamCodec)ResourceLocation.STREAM_CODEC, LearnSpellResultPacket::spellId, LearnSpellResultPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    
}


