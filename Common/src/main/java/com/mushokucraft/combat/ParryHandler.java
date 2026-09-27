package com.mushokucraft.combat;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.EventResult;
// import com.mushokucraft.init.ModAttachments;

public class ParryHandler {
    public static void triggerParry(ServerPlayer player) {
        PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
        if (mastery == null || mastery.getActiveStance() != SwordStyle.WATER_GOD) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SwordItem)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }
        mastery.parryTicks = (Integer)MushokuConfig.PARRY_WINDOW_TICKS.get();
        player.getCooldowns().addCooldown(stack.getItem(), ((Integer)MushokuConfig.PARRY_COOLDOWN_TICKS.get()).intValue());
        mastery.setItemCooldownEnd(BuiltInRegistries.ITEM.getKey(stack.getItem()), System.currentTimeMillis() + (long)((Integer)MushokuConfig.PARRY_COOLDOWN_TICKS.get()).intValue() * 50L);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    public static void register() {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player.level().isClientSide) {
                return dev.architectury.event.CompoundEventResult.pass();
            }
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                ParryHandler.triggerParry(sp);
            }
            return dev.architectury.event.CompoundEventResult.pass();
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            Player victim;
            PlayerMasteryData mastery;
            if (entity.level().isClientSide) {
                return EventResult.pass();
            }
            LivingEntity livingEntity = entity;
            if (livingEntity instanceof Player && (mastery = PlayerMasteryProvider.get((Player)livingEntity)) != null && mastery.getActiveStance() == SwordStyle.WATER_GOD && mastery.parryTicks > 0) {
                Entity srcEntity;
                ((Player)livingEntity).level().playSound(null, ((Player)livingEntity).getX(), ((Player)livingEntity).getY(), ((Player)livingEntity).getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0f, 1.5f);
                if ((Player)livingEntity instanceof ServerPlayer) {
                    ServerPlayer sp = (ServerPlayer)livingEntity;
                    ModGameEvents.addStanceMastery(sp, SwordStyle.WATER_GOD, ((Double)MushokuConfig.WATER_GOD_PARRY_MASTERY_GAIN.get()).floatValue());
                }
                if ((srcEntity = source.getDirectEntity()) instanceof Projectile) {
                    Projectile projectile = (Projectile)srcEntity;
                    Vec3 motion = projectile.getDeltaMovement();
                    projectile.setDeltaMovement(motion.scale(((Double)MushokuConfig.PROJECTILE_DEFLECT_SPEED_MULT.get()).doubleValue()));
                } else {
                    srcEntity = source.getEntity();
                    if (srcEntity instanceof LivingEntity) {
                        LivingEntity attacker = (LivingEntity)srcEntity;
                        float masteryFactor = 1.0f + mastery.getStanceMastery(SwordStyle.WATER_GOD);
                        float baseCounter = ((Double)MushokuConfig.COUNTER_ATTACK_DAMAGE.get()).floatValue();
                        attacker.hurt(((Player)livingEntity).damageSources().playerAttack((Player)livingEntity), baseCounter * masteryFactor);
                        ((Player)livingEntity).level().playSound(null, ((Player)livingEntity).getX(), ((Player)livingEntity).getY(), ((Player)livingEntity).getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
                    }
                }
                return EventResult.interruptFalse(); // Cancel damage
            }
            return EventResult.pass();
        });
    }
}
