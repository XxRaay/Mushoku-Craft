package com.mushokucraft.network;

import com.mushokucraft.magic.LearningManager;
import com.mushokucraft.magic.ServerCastManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QteResultPacket(int result, boolean isLearning) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<QteResultPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"qte_result"));
    public static final StreamCodec<FriendlyByteBuf, QteResultPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.INT, QteResultPacket::result, (StreamCodec)ByteBufCodecs.BOOL, QteResultPacket::isLearning, QteResultPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(QteResultPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer)context.player();
            if (packet.isLearning()) {
                LearningManager.handleQteResult(player, packet.result());
            } else {
                ServerCastManager.handleQteResult(player, packet.result());
            }
        });
    }
}


