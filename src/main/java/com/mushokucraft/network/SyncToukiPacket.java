package com.mushokucraft.network;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncToukiPacket(int entityId, boolean isActive) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncToukiPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"sync_touki"));
    public static final StreamCodec<FriendlyByteBuf, SyncToukiPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.INT, SyncToukiPacket::entityId, (StreamCodec)ByteBufCodecs.BOOL, SyncToukiPacket::isActive, SyncToukiPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player;
            PlayerMasteryData mastery;
            Entity entity;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && (entity = mc.level.getEntity(this.entityId)) instanceof Player && (mastery = (PlayerMasteryData)(player = (Player)entity).getData(ModAttachments.PLAYER_MASTERY)) != null) {
                mastery.setToukiActive(this.isActive);
            }
        });
    }
}


