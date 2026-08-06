package com.mushokucraft.magic;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.MasteryCalculator;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModSpells;
import com.mushokucraft.magic.Spell;
import com.mushokucraft.network.LearnSpellResultPacket;
import com.mushokucraft.network.LearnSpellSyncPacket;
import com.mushokucraft.network.QteTriggerPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import dev.architectury.networking.NetworkManager;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

public class LearningManager {
    private static final Map<UUID, ActiveLearning> learningTasks = new HashMap<UUID, ActiveLearning>();

    public static void register() {
        TickEvent.PLAYER_POST.register((player2) -> {
            ServerPlayer player;
            UUID uuid;
            ActiveLearning cast;
            if (player2 instanceof ServerPlayer && (cast = learningTasks.get(uuid = (player = (ServerPlayer)player2).getUUID())) != null) {
                if (cast.isQteWaiting) {
                    return;
                }
                if (!cast.qteTriggerTicks.isEmpty() && cast.remainingTicks <= cast.qteTriggerTicks.get(cast.qteTriggerTicks.size() - 1)) {
                    cast.qteTriggerTicks.remove(cast.qteTriggerTicks.size() - 1);
                    cast.isQteWaiting = true;
                    String letters = "abcdefghijklmnopqrstuvwxyz";
                    String randomKey = String.valueOf(letters.charAt((int)(Math.random() * (double)letters.length())));
                    Spell spell = ModSpells.SPELLS.get(cast.spellId);
                    PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    float schoolMastery = mastery.getSchoolMastery(spell.getSchool());
                    float speedModifier = MasteryCalculator.calculateQteSpeedModifier(schoolMastery, spell.getRank().getTier());
                    float targetSizeModifier = MasteryCalculator.calculateQteTargetSize(spell.getRank().getTier());
                    float perfectMultiplier = 0.4f * MasteryCalculator.calculateQteSizeModifier(schoolMastery);
                    NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new QteTriggerPacket(randomKey, speedModifier, targetSizeModifier, perfectMultiplier));
                    return;
                }
                --cast.remainingTicks;
                if (cast.forceFizzle || cast.remainingTicks <= 0) {
                    boolean finalSuccess;
                    learningTasks.remove(uuid);
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    Spell spell = ModSpells.SPELLS.get(cast.spellId);
                    if (spell == null) {
                        return;
                    }
                    boolean bl = finalSuccess = cast.success && !cast.forceFizzle;
                    if (finalSuccess) {
                        PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                        mastery.addSpellMastery(cast.spellId, ((Double)MushokuConfig.LEARNING_MASTERY_GAIN.get()).floatValue());
                        if (spell.getProjectileFactory() != null) {
                            Projectile projectile = spell.getProjectileFactory().create(player.level(), player);
                            Vec3 look = player.getLookAngle();
                            projectile.shoot(look.x, look.y, look.z, spell.getBaseSpeed(), spell.getBaseInaccuracy());
                            player.level().addFreshEntity((Entity)projectile);
                        }
                    }
                    NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new LearnSpellResultPacket(finalSuccess, cast.spellId));
                }
            }
        });

        PlayerEvent.PLAYER_QUIT.register((player) -> {
            learningTasks.remove(player.getUUID());
        });
    }

    public static void handleQteResult(ServerPlayer player, int result) {
        ActiveLearning cast = learningTasks.get(player.getUUID());
        if (cast != null && cast.isQteWaiting) {
            cast.isQteWaiting = false;
            if (result == 0) {
                cast.remainingTicks = cast.totalTicks - cast.fizzleTick;
                if (cast.fizzleTick <= 0) {
                    cast.remainingTicks = 0;
                }
                cast.forceFizzle = true;
            } else {
                if (result == 2) {
                    cast.remainingTicks = Math.max(0, cast.remainingTicks - (Integer)MushokuConfig.QTE_PERFECT_TIME_BONUS_TICKS.get());
                }
                    PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    Spell spell = ModSpells.SPELLS.get(cast.spellId);
                    if (!cast.spellId.getPath().equals("air_cushion")) {
                        float tierPenalty = MasteryCalculator.calculateTierPenalty(spell, mastery);
                        double successChance = Math.max(0.0, MushokuConfig.LEARNING_SUCCESS_BASE_CHANCE.get() - tierPenalty);
                        LearningManager.scheduleNextFizzle(cast, successChance);
                    }
            }
        }
    }

    private static void scheduleNextFizzle(ActiveLearning cast, double successChance) {
        boolean success = Math.random() < successChance;
        if (!success && cast.remainingTicks > 5) {
            int ticksFromNow;
            if (successChance <= 0.0) {
                // 100% fizzle -> QTE "on every letter" (very fast, 2-5 ticks)
                ticksFromNow = 2 + (int)(Math.random() * 4.0);
            } else if (successChance < 0.21) {
                // > 79% fizzle -> QTE "on every word" (fast, 10-20 ticks)
                ticksFromNow = 10 + (int)(Math.random() * 11.0);
            } else {
                // Normal
                int maxDelay = Math.max(1, cast.remainingTicks - 20);
                ticksFromNow = 15 + (int)(Math.random() * (double)Math.min(40, maxDelay));
            }
            
            ticksFromNow = Math.min(ticksFromNow, cast.remainingTicks - 1);
            
            cast.fizzleTick = cast.totalTicks - cast.remainingTicks + ticksFromNow;
            cast.qteTriggerTicks.add(cast.remainingTicks - ticksFromNow);
            Collections.sort(cast.qteTriggerTicks);
        } else {
            cast.fizzleTick = -1;
        }
    }

    public static void startLearning(ServerPlayer player, ResourceLocation spellId) {
        int castDurationTicks;
        Spell spell = ModSpells.SPELLS.get(spellId);
        if (spell == null) {
            return;
        }
        PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
        if (!mastery.consumeMana(spell.getEffectiveManaCost(mastery))) {
            player.displayClientMessage((Component)Component.literal((String)"\u00a7cNot enough mana!"), true);
            return;
        }
        boolean success = true;
        int activeTicks = castDurationTicks = (int)spell.getBaseCastTimeTicks();
        ArrayList<Integer> qteTicks = new ArrayList<Integer>();
        ActiveLearning castTask = new ActiveLearning(spellId, success, activeTicks, 0, qteTicks);
        learningTasks.put(player.getUUID(), castTask);
        if (spellId.getPath().equals("air_cushion")) {
            qteTicks.add(16);
            qteTicks.add(32);
            qteTicks.add(48);
            qteTicks.add(64);
            castTask.fizzleTick = 64;
        } else {
            float tierPenalty = MasteryCalculator.calculateTierPenalty(spell, mastery);
            double successChance = Math.max(0.0, MushokuConfig.LEARNING_SUCCESS_BASE_CHANCE.get() - tierPenalty);
            LearningManager.scheduleNextFizzle(castTask, successChance);
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, activeTicks + 10, 1, false, false, true));
        NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new LearnSpellSyncPacket(spellId, 0));
    }

    private static class ActiveLearning {
        final ResourceLocation spellId;
        final boolean success;
        final int totalTicks;
        int remainingTicks;
        int fizzleTick;
        final List<Integer> qteTriggerTicks;
        boolean isQteWaiting = false;
        boolean forceFizzle = false;

        ActiveLearning(ResourceLocation spellId, boolean success, int totalTicks, int fizzleTick, List<Integer> qteTriggerTicks) {
            this.spellId = spellId;
            this.success = success;
            this.totalTicks = totalTicks;
            this.remainingTicks = totalTicks;
            this.fizzleTick = fizzleTick;
            this.qteTriggerTicks = qteTriggerTicks;
        }
    }
}
