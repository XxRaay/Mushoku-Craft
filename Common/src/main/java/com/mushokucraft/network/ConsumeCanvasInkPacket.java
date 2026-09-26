package com.mushokucraft.network;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.item.BlankCanvasItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ConsumeCanvasInkPacket(boolean isMainHand) implements CustomPacketPayload {
    public static final Type<ConsumeCanvasInkPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "consume_canvas_ink"));

    public static final StreamCodec<ByteBuf, ConsumeCanvasInkPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ConsumeCanvasInkPacket::isMainHand,
            ConsumeCanvasInkPacket::new
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

        // Check if player has an ink sac
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        boolean consumed = false;
        if (otherStack.is(Items.INK_SAC)) {
            otherStack.shrink(1);
            consumed = true;
        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.INK_SAC)) {
                    stack.shrink(1);
                    consumed = true;
                    break;
                }
            }
        }

        if (consumed) {
            int currentSpent = BlankCanvasItem.getSpentInkSacs(canvasStack);
            BlankCanvasItem.setSpentInkSacs(canvasStack, currentSpent + 1);
            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 0.4f, 1.4f);
        }
    }
}
