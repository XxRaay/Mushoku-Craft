package com.mushokucraft.item;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ManaCrystalItem extends Item {
    private final float manaRestored;

    public ManaCrystalItem(Properties properties, float manaRestored) {
        super(properties);
        this.manaRestored = manaRestored;
    }

    public float getManaRestored() {
        return manaRestored;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            if (data != null) {
                if (data.getMana() >= data.getMaxMana()) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.mana_already_full"), true);
                    return InteractionResultHolder.fail(stack);
                }

                data.regenMana(this.manaRestored);
                PlayerMasteryProvider.sync(player);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(),
                            20, 0.35, 0.5, 0.35, 0.05);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(),
                            10, 0.25, 0.4, 0.25, 0.03);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.4f);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                }

                player.displayClientMessage(Component.translatable("message.mushokucraft.mana_crystal_consumed",
                        String.format("%.0f", this.manaRestored)), true);
                stack.shrink(1);
                return InteractionResultHolder.consume(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.mushokucraft.mana_crystal_restore",
                String.format("%.0f", this.manaRestored)).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.mushokucraft.mana_crystal_desc").withStyle(ChatFormatting.GRAY));
    }
}
