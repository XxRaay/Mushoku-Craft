package com.mushokucraft.item;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
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

public class SwordGodScrollItem extends AbstractScrollItem {

    private static final int USE_DURATION = 50;
    private static final float MANA_PER_TICK = 20.0f;

    public SwordGodScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean hasAlreadyLearned(PlayerMasteryData data) {
        return data.hasUnlockedLongswordOfSilence();
    }

    @Override
    protected Component getAlreadyLearnedMessage() {
        return Component.translatable("message.mushokucraft.already_unlocked_silence");
    }

    @Override
    protected boolean meetsMasteryRequirement(PlayerMasteryData data) {
        return data.getStanceMastery(com.mushokucraft.combat.SwordStyle.SWORD_GOD) >= 1.0f;
    }

    @Override
    protected Component getMasteryRequirementMessage() {
        return Component.translatable("message.mushokucraft.requires_sword_god_mastery");
    }

    @Override
    protected void learnSpell(Player player, PlayerMasteryData data) {
        data.setUnlockedLongswordOfSilence(true);
    }

    @Override
    protected Component getLearnedMessage() {
        return Component.translatable("message.mushokucraft.unlocked_silence");
    }

    @Override
    protected int getScrollUseDuration() {
        return USE_DURATION;
    }

    @Override
    protected float getManaPerTick() {
        return MANA_PER_TICK;
    }
}
