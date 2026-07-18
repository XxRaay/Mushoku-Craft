package com.mushokucraft.network;

import com.mushokucraft.combat.SwordCombatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChangeStancePacket(String stanceId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ChangeStancePacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"change_stance"));
    public static final StreamCodec<FriendlyByteBuf, ChangeStancePacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.STRING_UTF8, ChangeStancePacket::stanceId, ChangeStancePacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            SwordCombatHandler.changeStance(player, this.stanceId);
        });
    }
}


