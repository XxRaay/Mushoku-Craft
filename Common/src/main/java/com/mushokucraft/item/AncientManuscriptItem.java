package com.mushokucraft.item;

import com.mushokucraft.client.gui.AncientManuscriptScreen;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import com.mushokucraft.magic.circle.TeleportCircleType;
import com.mushokucraft.network.SyncFullMasteryPacket;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class AncientManuscriptItem extends Item {

    public AncientManuscriptItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    public static ItemStack createForType(ResourceLocation circleTypeId) {
        ItemStack stack = new ItemStack(ModItems.ANCIENT_MANUSCRIPT.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (circleTypeId != null) {
                tag.putString("CircleType", circleTypeId.toString());
            }
        });
        return stack;
    }

    public static ResourceLocation getCircleType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("CircleType")) {
            ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("CircleType"));
            if (parsed != null) return parsed;
        }
        return TeleportCircleType.ID;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResourceLocation circleType = getCircleType(stack);

        if (!level.isClientSide()) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            if (data != null && data.studyCircle(circleType)) {
                if (player instanceof ServerPlayer sp) {
                    NetworkManager.sendToPlayer(sp, new SyncFullMasteryPacket(data.serializeNBT(level.registryAccess())));
                }
                MagicCircleType type = MagicCircleRegistry.get(circleType);
                Component typeName = type != null ? type.getDisplayName() : Component.literal(circleType.toString());
                player.displayClientMessage(Component.translatable("message.mushokucraft.circle_studied", typeName), true);
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.4f);
            }
        } else {
            openScreen(circleType);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void openScreen(ResourceLocation circleTypeId) {
        if (Platform.getEnv() == EnvType.CLIENT) {
            Minecraft.getInstance().setScreen(new AncientManuscriptScreen(circleTypeId));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation circleType = getCircleType(stack);
        MagicCircleType type = MagicCircleRegistry.get(circleType);
        Component typeName = type != null ? type.getDisplayName() : Component.literal(circleType.toString());

        tooltipComponents.add(Component.translatable("tooltip.mushokucraft.blueprint", typeName).withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("item.mushokucraft.ancient_manuscript.tooltip1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.mushokucraft.ancient_manuscript.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
