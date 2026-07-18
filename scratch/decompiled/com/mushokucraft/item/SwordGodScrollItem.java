/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.level.Level
 */
package com.mushokucraft.item;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class SwordGodScrollItem
extends Item {
    private static final int USE_DURATION = 50;
    private static final float MANA_PER_TICK = 20.0f;

    public SwordGodScrollItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (data.hasUnlockedLongswordOfSilence()) {
            if (!level.isClientSide()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.already_unlocked_silence"), true);
            }
            return InteractionResultHolder.fail((Object)itemStack);
        }
        if (data.getStanceMastery(SwordStyle.SWORD_GOD) < 1.0f) {
            if (!level.isClientSide()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.requires_sword_god_mastery"), true);
            }
            return InteractionResultHolder.fail((Object)itemStack);
        }
        if (data.getMana() < 1000.0f) {
            if (!level.isClientSide()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.not_enough_mana_scroll"), true);
            }
            return InteractionResultHolder.fail((Object)itemStack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume((Object)itemStack);
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 50;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            if (!data.consumeMana(20.0f)) {
                player.stopUsingItem();
            } else {
                ModGameEvents.syncMana(player, data);
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)level;
                    serverLevel.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player;
        if (!level.isClientSide() && livingEntity instanceof Player) {
            player = (Player)livingEntity;
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            data.setUnlockedLongswordOfSilence(true);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.5, 1.0, 0.5, 0.2);
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.5f);
            player.displayClientMessage((Component)Component.translatable((String)"message.mushokucraft.unlocked_silence"), true);
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                ModGameEvents.syncMastery(serverPlayer, data);
            }
        }
        if (livingEntity instanceof Player) {
            player = (Player)livingEntity;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }
}

