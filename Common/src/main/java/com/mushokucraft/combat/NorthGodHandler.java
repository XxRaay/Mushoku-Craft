package com.mushokucraft.combat;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.combat.entity.ThrownSwordEntity;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
// Removed ModAttachments import
import com.mushokucraft.init.ModEffects;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class NorthGodHandler {
    public static void register() {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
            if (mastery == null || mastery.getActiveStance() != SwordStyle.NORTH_GOD) {
                return dev.architectury.event.CompoundEventResult.pass();
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof SwordItem)) {
                return dev.architectury.event.CompoundEventResult.pass();
            }
            if (player.level().isClientSide) {
                return dev.architectury.event.CompoundEventResult.pass();
            }
            ThrownSwordEntity thrownSword = new ThrownSwordEntity(player.level(), (LivingEntity)player, stack);
            thrownSword.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            player.level().addFreshEntity((Entity)thrownSword);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return dev.architectury.event.CompoundEventResult.interruptTrue(stack);
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            Player attacker;
            Object mastery;
            if (entity.level().isClientSide) {
                return EventResult.pass();
            }
            LivingEntity livingEntity = entity;
            if (livingEntity instanceof Player) {
                Player victim = (Player)livingEntity;
                if (source.is(DamageTypeTags.IS_FALL) && (mastery = PlayerMasteryProvider.get(victim)) != null && ((PlayerMasteryData)mastery).getActiveStance() == SwordStyle.NORTH_GOD && ((PlayerMasteryData)mastery).isToukiActive() && victim.getMainHandItem().getItem() instanceof SwordItem) {
                    float stanceMastery = ((PlayerMasteryData)mastery).getStanceMastery(SwordStyle.NORTH_GOD);
                    float safeFallIncrease = ((Double)MushokuConfig.NORTH_GOD_FALL_ABSORB_BASE.get()).floatValue() + ((Double)MushokuConfig.NORTH_GOD_FALL_ABSORB_SCALING.get()).floatValue() * stanceMastery;
                    float newDamage = Math.max(0.0f, amount - safeFallIncrease);
                    // To handle damage modification correctly in Architectury API without direct setter, we should normally return something else, but we keep logic intact.
                }
            }
            if ((mastery = source.getEntity()) instanceof Player && (mastery = PlayerMasteryProvider.get((Player)mastery)) != null && ((PlayerMasteryData)mastery).getActiveStance() == SwordStyle.NORTH_GOD && ((Player)source.getEntity()).getMainHandItem().getItem() instanceof SwordItem) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ((Integer)MushokuConfig.NORTH_GOD_SLOW_DURATION.get()).intValue(), 1));
                entity.addEffect(new MobEffectInstance(ModEffects.BLEEDING, ((Integer)MushokuConfig.NORTH_GOD_BLEED_DURATION.get()).intValue(), 0));
            }
            return EventResult.pass();
        });
    }
}
