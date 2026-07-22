package com.mushokucraft.network;

import com.mushokucraft.magic.ServerChargeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record StartChargePacket(ResourceLocation spellId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<StartChargePacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"start_charge"));
    public static final StreamCodec<FriendlyByteBuf, StartChargePacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ResourceLocation.STREAM_CODEC, StartChargePacket::spellId, StartChargePacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {                 ServerChargeManager.startCharge(player, this.spellId()); }
}


