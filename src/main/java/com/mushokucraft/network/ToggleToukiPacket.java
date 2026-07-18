package com.mushokucraft.network;

import com.mushokucraft.combat.ToukiManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleToukiPacket() implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ToggleToukiPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"toggle_touki"));
    public static final StreamCodec<FriendlyByteBuf, ToggleToukiPacket> STREAM_CODEC = StreamCodec.unit(new ToggleToukiPacket());

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player patt0$temp = context.player();
            if (patt0$temp instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)patt0$temp;
                ToukiManager.toggleTouki(player);
            }
        });
    }
}


