package com.mushokucraft.event;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.EntityEvent;

public class AdvancementHandler {
    public static void register() {
        PlayerEvent.PLAYER_ADVANCEMENT.register((player, advancement) -> {
            if (player instanceof ServerPlayer) {
                ServerPlayer player2 = (ServerPlayer)player;
                String advId = advancement.id().toString();
                PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player2);
                boolean sync = false;
                if (advId.equals("mushokucraft:style_sword_god")) {
                    data.unlockStyle(SwordStyle.SWORD_GOD);
                    sync = true;
                } else if (advId.equals("mushokucraft:style_water_god")) {
                    data.unlockStyle(SwordStyle.WATER_GOD);
                    sync = true;
                } else if (advId.equals("mushokucraft:style_north_god")) {
                    data.unlockStyle(SwordStyle.NORTH_GOD);
                    sync = true;
                }
                if (sync) {
                    ModGameEvents.syncMastery(player2, data);
                }
            }
        });

        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            ServerPlayer player;
            Entity entity = damageSource.getEntity();
            if (entity instanceof ServerPlayer && (player = (ServerPlayer)entity).getHealth() <= ((Double)MushokuConfig.LOW_HP_THRESHOLD.get()).floatValue() && livingEntity instanceof Enemy) {
                PlayerMasteryData data;
                AdvancementHolder adv = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("mushokucraft", "style_north_god"));
                if (adv != null) {
                    for (String criteria : adv.value().criteria().keySet()) {
                        player.getAdvancements().award(adv, criteria);
                    }
                }
                if ((data = (PlayerMasteryData)PlayerMasteryProvider.get(player)) != null && data.getActiveStance() == SwordStyle.NORTH_GOD) {
                    ModGameEvents.addStanceMastery(player, SwordStyle.NORTH_GOD, ((Double)MushokuConfig.NORTH_GOD_LOW_HP_KILL_MASTERY_GAIN.get()).floatValue());
                }
            }
            return dev.architectury.event.EventResult.pass();
        });

        EntityEvent.LIVING_HURT.register((livingEntity, damageSource, amount) -> {
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)livingEntity;
                if (!player.isBlocking()) {
                    return dev.architectury.event.EventResult.pass();
                }
                if (player.isDamageSourceBlocked(damageSource)) {
                    PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    if (data != null && !data.isStyleUnlocked(SwordStyle.WATER_GOD)) {
                        AdvancementHolder adv;
                        data.incrementShieldBlocks();
                        if (data.getShieldBlocks() >= (Integer)MushokuConfig.SHIELD_BLOCKS_FOR_WATER_GOD.get() && (adv = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("mushokucraft", "style_water_god"))) != null) {
                            for (String criteria : adv.value().criteria().keySet()) {
                                player.getAdvancements().award(adv, criteria);
                            }
                        }
                    }
                }
            }
            return dev.architectury.event.EventResult.pass();
        });
    }
}
