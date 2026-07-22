package com.mushokucraft.network;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncFullMasteryPacket(CompoundTag data) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncFullMasteryPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"sync_full_mastery"));
    public static final StreamCodec<FriendlyByteBuf, SyncFullMasteryPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.COMPOUND_TAG, SyncFullMasteryPacket::data, SyncFullMasteryPacket::new);

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(net.minecraft.world.entity.player.Player player) {
        com.mushokucraft.client.network.ClientPayloadHandler.handleSyncFullMastery(this);
    }
}


