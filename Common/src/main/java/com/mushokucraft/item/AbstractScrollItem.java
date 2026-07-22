package com.mushokucraft.item;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public abstract class AbstractScrollItem extends Item {

    public AbstractScrollItem(Properties properties) {
        super(properties);
    }

    protected abstract boolean hasAlreadyLearned(PlayerMasteryData data);
    protected abstract Component getAlreadyLearnedMessage();
    protected abstract boolean meetsMasteryRequirement(PlayerMasteryData data);
    protected abstract Component getMasteryRequirementMessage();
    protected abstract void learnSpell(Player player, PlayerMasteryData data);
    protected abstract Component getLearnedMessage();
    protected abstract int getScrollUseDuration();
    protected abstract float getManaPerTick();

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        PlayerMasteryData data = PlayerMasteryProvider.get(player);
        if (hasAlreadyLearned(data)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(getAlreadyLearnedMessage(), true);
            }
            return InteractionResultHolder.fail(itemStack);
        }
        if (!meetsMasteryRequirement(data)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(getMasteryRequirementMessage(), true);
            }
            return InteractionResultHolder.fail(itemStack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return getScrollUseDuration();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!level.isClientSide() && livingEntity instanceof net.minecraft.server.level.ServerPlayer player) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            if (!data.consumeMana(getManaPerTick())) {
                player.stopUsingItem();
            } else {
                com.mushokucraft.event.ModGameEvents.syncMana(player, data);

                // Spawn particles while charging
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(),
                            5, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            
            learnSpell(player, data);

            // Visual and Sound effects
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 50,
                        0.5, 1.0, 0.5, 0.2);
                serverLevel.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0,
                        0, 0, 0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 1.0F, 0.5F);

            player.displayClientMessage(getLearnedMessage(), true);

            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.mushokucraft.event.ModGameEvents.syncMastery(serverPlayer, data);
            }
        }

        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }
}
