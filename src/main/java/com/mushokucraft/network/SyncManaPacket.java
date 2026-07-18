package com.mushokucraft.network;

import com.mushokucraft.MushokuCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncManaPacket(float currentMana, float maxMana) implements CustomPacketPayload {
    public static final Type<SyncManaPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MushokuCraft.MOD_ID, "sync_mana"));

    public static final StreamCodec<ByteBuf, SyncManaPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncManaPacket::currentMana,
            ByteBufCodecs.FLOAT, SyncManaPacket::maxMana,
            SyncManaPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}






