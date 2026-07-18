package com.mushokucraft.item;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

public class SwordGodScrollItem extends Item {
    
    // 50 ticks * 20 mana = 1000 mana
    private static final int USE_DURATION = 50; 
    private static final float MANA_PER_TICK = 20.0f;

    public SwordGodScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        PlayerMasteryData data = player.getData(ModAttachments.PLAYER_MASTERY);
        if (data.hasUnlockedLongswordOfSilence()) {
            if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message.mushokucraft.already_unlocked_silence"), true);
            return InteractionResultHolder.fail(itemStack);
        }
        if (data.getStanceMastery(com.mushokucraft.combat.SwordStyle.SWORD_GOD) < 1.0f) {
            if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message.mushokucraft.requires_sword_god_mastery"), true);
            return InteractionResultHolder.fail(itemStack);
        }
        if (data.getMana() < USE_DURATION * MANA_PER_TICK) {
            if (!level.isClientSide()) player.displayClientMessage(Component.translatable("message.mushokucraft.not_enough_mana_scroll"), true);
            return InteractionResultHolder.fail(itemStack);
        }
        
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!level.isClientSide() && livingEntity instanceof net.minecraft.server.level.ServerPlayer player) {
            PlayerMasteryData data = player.getData(ModAttachments.PLAYER_MASTERY);
            if (!data.consumeMana(MANA_PER_TICK)) {
                player.stopUsingItem();
            } else {
                com.mushokucraft.event.ModGameEvents.syncMana(player, data);
                
                // Spawn particles while charging
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof Player player) {
            PlayerMasteryData data = player.getData(ModAttachments.PLAYER_MASTERY);
            data.setUnlockedLongswordOfSilence(true);
            
            // Visual and Sound effects
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.5, 1.0, 0.5, 0.2);
                serverLevel.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0, 0, 0, 0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.5F);
            
            player.displayClientMessage(Component.translatable("message.mushokucraft.unlocked_silence"), true);
            
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





