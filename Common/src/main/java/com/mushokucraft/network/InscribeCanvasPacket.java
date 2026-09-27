package com.mushokucraft.network;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.item.InscribedManuscriptItem;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
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

public record InscribeCanvasPacket(byte[] gridData, boolean isMainHand) implements CustomPacketPayload {
    public static final Type<InscribeCanvasPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "inscribe_canvas"));

    public static final StreamCodec<ByteBuf, InscribeCanvasPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY, InscribeCanvasPacket::gridData,
            ByteBufCodecs.BOOL, InscribeCanvasPacket::isMainHand,
            InscribeCanvasPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack canvasStack = player.getItemInHand(hand);

        if (!canvasStack.is(ModItems.BLANK_CANVAS.get())) {
            // Check other hand just in case
            hand = isMainHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            canvasStack = player.getItemInHand(hand);
            if (!canvasStack.is(ModItems.BLANK_CANVAS.get())) {
                return;
            }
        }

        MagicCirclePattern pattern = new MagicCirclePattern(gridData);
        int filledCount = pattern.countFilled();

        if (filledCount == 0) {
            player.displayClientMessage(Component.translatable("message.mushokucraft.canvas_empty"), true);
            return;
        }

        int inkSacPerPixels = Math.max(1, MushokuConfig.MAGIC_CIRCLE_INK_PIXELS_PER_SAC.get());
        int requiredInkSacs = (filledCount + inkSacPerPixels - 1) / inkSacPerPixels;
        int alreadySpent = com.mushokucraft.item.BlankCanvasItem.getSpentInkSacs(canvasStack);
        int toConsume = Math.max(0, requiredInkSacs - alreadySpent);

        if (toConsume > 0) {
            // Count available ink sacs in player's inventory
            int availableInkSacs = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.INK_SAC)) {
                    availableInkSacs += stack.getCount();
                }
            }

            if (availableInkSacs < toConsume) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.not_enough_ink", toConsume, availableInkSacs), true);
                return;
            }

            // Consume remaining ink sacs
            int remaining = toConsume;
            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.INK_SAC)) {
                    int take = Math.min(stack.getCount(), remaining);
                    stack.shrink(take);
                    remaining -= take;
                }
            }
        }

        // Identify circle type against world rules
        MagicCircleType circleType = MagicCircleRegistry.identify(player.level(), pattern);

        // Replace 1 blank canvas with inscribed manuscript
        canvasStack.shrink(1);

        ItemStack manuscript = InscribedManuscriptItem.create(pattern, circleType != null ? circleType.getId() : null);

        if (canvasStack.isEmpty()) {
            player.setItemInHand(hand, manuscript);
        } else {
            if (!player.getInventory().add(manuscript)) {
                player.drop(manuscript, false);
            }
        }

        // Play feedback sounds and particles
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);

        if (circleType != null) {
            player.displayClientMessage(Component.translatable("message.mushokucraft.circle_inscribed_success", circleType.getDisplayName()), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mushokucraft.circle_inscribed_unknown"), true);
        }
    }
}
