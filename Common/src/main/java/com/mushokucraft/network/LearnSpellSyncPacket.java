package com.mushokucraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LearnSpellSyncPacket(ResourceLocation spellId, int castTimeTicks, int fizzleTick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LearnSpellSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mushokucraft", "learn_spell_sync"));
    public static final StreamCodec<FriendlyByteBuf, LearnSpellSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, LearnSpellSyncPacket::spellId,
            ByteBufCodecs.INT, LearnSpellSyncPacket::castTimeTicks,
            ByteBufCodecs.INT, LearnSpellSyncPacket::fizzleTick,
            LearnSpellSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}


