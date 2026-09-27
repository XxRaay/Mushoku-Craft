package com.mushokucraft.network;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.item.BlankCanvasItem;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record SaveCanvasDraftPacket(byte[] gridData, int spentInkSacs, boolean isMainHand) implements CustomPacketPayload {
    public static final Type<SaveCanvasDraftPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "save_canvas_draft"));

    public static final StreamCodec<ByteBuf, SaveCanvasDraftPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, SaveCanvasDraftPacket::gridData,
            ByteBufCodecs.VAR_INT, SaveCanvasDraftPacket::spentInkSacs,
            ByteBufCodecs.BOOL, SaveCanvasDraftPacket::isMainHand,
            SaveCanvasDraftPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack canvasStack = player.getItemInHand(hand);

        if (!canvasStack.is(ModItems.BLANK_CANVAS.get())) {
            hand = isMainHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            canvasStack = player.getItemInHand(hand);
            if (!canvasStack.is(ModItems.BLANK_CANVAS.get())) {
                return;
            }
        }

        MagicCirclePattern pattern = new MagicCirclePattern(gridData);
        BlankCanvasItem.saveDraft(canvasStack, pattern, spentInkSacs);
    }
}
