package com.mushokucraft.network;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFullMasteryPacket(CompoundTag data) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncFullMasteryPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"sync_full_mastery"));
    public static final StreamCodec<FriendlyByteBuf, SyncFullMasteryPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.COMPOUND_TAG, SyncFullMasteryPacket::data, SyncFullMasteryPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerMasteryData mastery;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && (mastery = (PlayerMasteryData)mc.player.getData(ModAttachments.PLAYER_MASTERY)) != null) {
                mastery.deserializeNBT((HolderLookup.Provider)mc.level.registryAccess(), this.data);
            }
        });
    }
}


