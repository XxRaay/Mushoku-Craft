package com.mushokucraft.network;

import com.mushokucraft.crafting.ModularAnvilMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ForgeWeaponPacket(String formId, String customName, float qualityScore) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ForgeWeaponPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mushokucraft", "forge_weapon"));

    public static final StreamCodec<FriendlyByteBuf, ForgeWeaponPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ForgeWeaponPacket::formId,
            ByteBufCodecs.STRING_UTF8, ForgeWeaponPacket::customName,
            ByteBufCodecs.FLOAT, ForgeWeaponPacket::qualityScore,
            ForgeWeaponPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        if (player.containerMenu instanceof ModularAnvilMenu menu) {
            menu.serverForgeWeapon(player, this.formId, this.customName, this.qualityScore);
        }
    }
}
