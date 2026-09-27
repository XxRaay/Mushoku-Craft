package com.mushokucraft.item;

import com.mushokucraft.client.gui.MagicCanvasScreen;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class BlankCanvasItem extends Item {

    public BlankCanvasItem(Properties properties) {
        super(properties);
    }

    public static MagicCirclePattern getDraftPattern(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("DraftPattern")) {
            return MagicCirclePattern.load(tag.getCompound("DraftPattern"));
        }
        return new MagicCirclePattern();
    }

    public static int getSpentInkSacs(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.getInt("SpentInkSacs");
    }

    public static void setSpentInkSacs(ItemStack stack, int count) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("SpentInkSacs", count);
        });
    }

    public static void saveDraft(ItemStack stack, MagicCirclePattern pattern, int spentInkSacs) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (pattern != null && pattern.countFilled() > 0) {
                tag.put("DraftPattern", pattern.save());
                tag.putInt("SpentInkSacs", spentInkSacs);
            } else {
                tag.remove("DraftPattern");
                tag.remove("SpentInkSacs");
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherHeld = player.getItemInHand(otherHand);

        if (!otherHeld.is(Items.INK_SAC)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.requires_ink_sac"), true);
            }
            return InteractionResultHolder.fail(held);
        }

        // If holding a stack > 1, isolate 1 canvas for drafting
        if (held.getCount() > 1) {
            ItemStack remaining = held.copy();
            remaining.shrink(1);
            held.setCount(1);
            if (!player.getInventory().add(remaining)) {
                player.drop(remaining, false);
            }
        }

        if (level.isClientSide()) {
            MagicCirclePattern draft = getDraftPattern(held);
            int spent = getSpentInkSacs(held);
            openCanvasScreen(hand == InteractionHand.MAIN_HAND, draft, spent);
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    private void openCanvasScreen(boolean isMainHand, MagicCirclePattern draft, int spent) {
        if (Platform.getEnv() == EnvType.CLIENT) {
            Minecraft.getInstance().setScreen(new MagicCanvasScreen(isMainHand, draft, spent));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        MagicCirclePattern draft = getDraftPattern(stack);
        int filled = draft.countFilled();
        if (filled > 0) {
            tooltipComponents.add(Component.translatable("item.mushokucraft.blank_canvas.draft_pixels", filled));
            int spent = getSpentInkSacs(stack);
            if (spent > 0) {
                tooltipComponents.add(Component.translatable("item.mushokucraft.blank_canvas.draft_ink", spent));
            }
        }
        tooltipComponents.add(Component.translatable("item.mushokucraft.blank_canvas.tooltip1"));
        tooltipComponents.add(Component.translatable("item.mushokucraft.blank_canvas.tooltip2"));
    }
}
