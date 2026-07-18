package com.mushokucraft.network;

import com.mushokucraft.magic.LearningManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartLearnSpellPacket(ResourceLocation spellId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<StartLearnSpellPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"start_learn_spell"));
    public static final StreamCodec<FriendlyByteBuf, StartLearnSpellPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ResourceLocation.STREAM_CODEC, StartLearnSpellPacket::spellId, StartLearnSpellPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartLearnSpellPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player patt0$temp = context.player();
            if (patt0$temp instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)patt0$temp;
                LearningManager.startLearning(serverPlayer, packet.spellId());
            }
        });
    }
}


