package com.mushokucraft.network;

import com.mushokucraft.magic.ServerChargeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ReleaseChargePacket() implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ReleaseChargePacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"release_charge"));
    public static final StreamCodec<FriendlyByteBuf, ReleaseChargePacket> STREAM_CODEC = StreamCodec.unit(new ReleaseChargePacket());

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {                 ServerChargeManager.releaseCharge(player); }
}


