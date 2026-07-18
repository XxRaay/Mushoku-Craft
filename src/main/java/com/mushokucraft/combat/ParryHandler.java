package com.mushokucraft.combat;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="mushokucraft", bus=EventBusSubscriber.Bus.GAME)
public class ParryHandler {
    public static void triggerParry(ServerPlayer player) {
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
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

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            ParryHandler.triggerParry(sp);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Player victim;
        PlayerMasteryData mastery;
        if (event.getEntity().level().isClientSide) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && (mastery = (PlayerMasteryData)(victim = (Player)livingEntity).getData(ModAttachments.PLAYER_MASTERY)) != null && mastery.getActiveStance() == SwordStyle.WATER_GOD && mastery.parryTicks > 0) {
            Entity entity;
            event.setNewDamage(0.0f);
            victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0f, 1.5f);
            if (victim instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)victim;
                ModGameEvents.addStanceMastery(sp, SwordStyle.WATER_GOD, ((Double)MushokuConfig.WATER_GOD_PARRY_MASTERY_GAIN.get()).floatValue());
            }
            if ((entity = event.getSource().getDirectEntity()) instanceof Projectile) {
                Projectile projectile = (Projectile)entity;
                Vec3 motion = projectile.getDeltaMovement();
                projectile.setDeltaMovement(motion.scale(((Double)MushokuConfig.PROJECTILE_DEFLECT_SPEED_MULT.get()).doubleValue()));
            } else {
                entity = event.getSource().getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity)entity;
                    attacker.hurt(victim.damageSources().playerAttack(victim), ((Double)MushokuConfig.COUNTER_ATTACK_DAMAGE.get()).floatValue());
                    victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
    }
}


