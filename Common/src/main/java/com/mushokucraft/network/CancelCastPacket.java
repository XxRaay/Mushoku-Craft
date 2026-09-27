package com.mushokucraft.network;

import com.mushokucraft.magic.LearningManager;
import com.mushokucraft.magic.ServerCastManager;
import com.mushokucraft.magic.ServerChargeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record CancelCastPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CancelCastPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mushokucraft", "cancel_cast"));
    public static final StreamCodec<FriendlyByteBuf, CancelCastPacket> STREAM_CODEC =
            StreamCodec.unit(new CancelCastPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        ServerCastManager.cancelCast(player);
        ServerChargeManager.cancelCharge(player);
        LearningManager.cancelLearning(player);
    }
}
