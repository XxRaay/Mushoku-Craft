package com.mushokucraft.combat;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.combat.entity.ThrownSwordEntity;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.init.ModEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="mushokucraft", bus=EventBusSubscriber.Bus.GAME)
public class NorthGodHandler {
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery == null || mastery.getActiveStance() != SwordStyle.NORTH_GOD) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SwordItem)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        ThrownSwordEntity thrownSword = new ThrownSwordEntity(player.level(), (LivingEntity)player, stack);
        thrownSword.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
        player.level().addFreshEntity((Entity)thrownSword);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Player attacker;
        Object mastery;
        if (event.getEntity().level().isClientSide) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            Player victim = (Player)livingEntity;
            if (event.getSource().is(DamageTypeTags.IS_FALL) && (mastery = (PlayerMasteryData)victim.getData(ModAttachments.PLAYER_MASTERY)) != null && ((PlayerMasteryData)mastery).getActiveStance() == SwordStyle.NORTH_GOD && ((PlayerMasteryData)mastery).isToukiActive() && victim.getMainHandItem().getItem() instanceof SwordItem) {
                float stanceMastery = ((PlayerMasteryData)mastery).getStanceMastery(SwordStyle.NORTH_GOD);
                float safeFallIncrease = ((Double)MushokuConfig.NORTH_GOD_FALL_ABSORB_BASE.get()).floatValue() + ((Double)MushokuConfig.NORTH_GOD_FALL_ABSORB_SCALING.get()).floatValue() * stanceMastery;
                float newDamage = Math.max(0.0f, event.getNewDamage() - safeFallIncrease);
                event.setNewDamage(newDamage);
            }
        }
        if ((mastery = event.getSource().getEntity()) instanceof Player && (mastery = (PlayerMasteryData)(attacker = (Player)mastery).getData(ModAttachments.PLAYER_MASTERY)) != null && ((PlayerMasteryData)mastery).getActiveStance() == SwordStyle.NORTH_GOD && attacker.getMainHandItem().getItem() instanceof SwordItem) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ((Integer)MushokuConfig.NORTH_GOD_SLOW_DURATION.get()).intValue(), 1));
            event.getEntity().addEffect(new MobEffectInstance(ModEffects.BLEEDING, ((Integer)MushokuConfig.NORTH_GOD_BLEED_DURATION.get()).intValue(), 0));
        }
    }
}


