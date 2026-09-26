package com.mushokucraft.network;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.magic.circle.ClientMagicCircleState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncMagicCirclesPacket(long worldSeed, int teleportPatternIndex) implements CustomPacketPayload {
    public static final Type<SyncMagicCirclesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "sync_magic_circles"));

    public static final StreamCodec<ByteBuf, SyncMagicCirclesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncMagicCirclesPacket::worldSeed,
            ByteBufCodecs.VAR_INT, SyncMagicCirclesPacket::teleportPatternIndex,
            SyncMagicCirclesPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClient() {
        ClientMagicCircleState.setClientWorldInfo(this.worldSeed, this.teleportPatternIndex);
    }
}
