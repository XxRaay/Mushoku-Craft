/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.magic;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.MasteryCalculator;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.magic.Spell;
import com.mushokucraft.network.QteTriggerPacket;
import com.mushokucraft.network.SyncManaPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="mushokucraft")
public class ServerCastManager {
    private static final Map<UUID, ActiveCast> activeCasts = new HashMap<UUID, ActiveCast>();

    public static void startCast(ServerPlayer player, Spell spell, int castTime, int fizzleTick) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, castTime + 10, 1, false, false, true));
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        float spellMastery = mastery.getSpellMastery(spell.getId());
        ArrayList<Integer> qteTicks = new ArrayList<Integer>();
        if (fizzleTick > 0) {
            qteTicks.add(castTime - fizzleTick);
        }
        ActiveCast cast = new ActiveCast(player, spell, castTime, fizzleTick, qteTicks);
        activeCasts.put(player.getUUID(), cast);
        float schoolMastery = mastery.getSchoolMastery(spell.getSchool());
        double successChance = MasteryCalculator.calculateSpellSuccessChance(spellMastery, schoolMastery);
        ServerCastManager.scheduleNextFizzle(cast, successChance);
    }

    public static void handleQteResult(ServerPlayer player, int result) {
        ActiveCast cast = activeCasts.get(player.getUUID());
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
                PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
                float spellMastery = mastery.getSpellMastery(cast.spell.getId());
                double successChance = 1.0 - (double)cast.spell.getFizzleChance(spellMastery);
                ServerCastManager.scheduleNextFizzle(cast, successChance);
            }
        }
    }

    private static void scheduleNextFizzle(ActiveCast cast, double successChance) {
        boolean success;
        boolean bl = success = Math.random() < successChance;
        if (!success && cast.remainingTicks > 20) {
            int ticksFromNow = 10 + (int)(Math.random() * (double)(cast.remainingTicks - 20));
            cast.fizzleTick = cast.totalTicks - cast.remainingTicks + ticksFromNow;
            cast.qteTriggerTicks.add(cast.remainingTicks - ticksFromNow);
            Collections.sort(cast.qteTriggerTicks);
        } else {
            cast.fizzleTick = -1;
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, ActiveCast>> iterator = activeCasts.entrySet().iterator();
        while (iterator.hasNext()) {
            PlayerMasteryData data;
            ActiveCast cast = iterator.next().getValue();
            ServerPlayer player = cast.player;
            if (player.isRemoved() || !player.isAlive()) {
                iterator.remove();
                continue;
            }
            if (cast.isQteWaiting) continue;
            --cast.remainingTicks;
            if (!cast.qteTriggerTicks.isEmpty() && cast.remainingTicks <= cast.qteTriggerTicks.get(cast.qteTriggerTicks.size() - 1)) {
                cast.qteTriggerTicks.remove(cast.qteTriggerTicks.size() - 1);
                cast.isQteWaiting = true;
                String letters = "abcdefghijklmnopqrstuvwxyz";
                String randomKey = String.valueOf(letters.charAt((int)(Math.random() * (double)letters.length())));
                PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
                float schoolMastery = mastery.getSchoolMastery(cast.spell.getSchool());
                float speedModifier = MasteryCalculator.calculateQteSpeedModifier(schoolMastery);
                float sizeModifier = MasteryCalculator.calculateQteSizeModifier(schoolMastery);
                PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new QteTriggerPacket(randomKey, speedModifier, sizeModifier), (CustomPacketPayload[])new CustomPacketPayload[0]);
                continue;
            }
            if (cast.forceFizzle || cast.fizzleTick > 0 && cast.totalTicks - cast.remainingTicks >= cast.fizzleTick) {
                data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
                data.consumeMana(cast.spell.getEffectiveManaCost(data));
                PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncManaPacket(data.getMana(), data.getMaxMana()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                iterator.remove();
                continue;
            }
            if (cast.remainingTicks > 0) continue;
            data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            if (data.consumeMana(cast.spell.getEffectiveManaCost(data))) {
                if (cast.spell.getSpellAction() != null) {
                    cast.spell.getSpellAction().execute(player.level(), player, cast.spell);
                }
                data.addSpellMastery(cast.spell.getId(), ((Double)MushokuConfig.SPELL_MASTERY_PER_CAST.get()).floatValue());
                data.addSchoolMastery(cast.spell.getSchool(), ((Double)MushokuConfig.SCHOOL_MASTERY_PER_CAST.get()).floatValue());
                ModGameEvents.syncMana(player, data);
                ModGameEvents.syncMastery(player, data);
            }
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            iterator.remove();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        activeCasts.remove(event.getEntity().getUUID());
    }

    private static class ActiveCast {
        final ServerPlayer player;
        final Spell spell;
        final int totalTicks;
        int remainingTicks;
        int fizzleTick;
        final List<Integer> qteTriggerTicks;
        boolean isQteWaiting = false;
        boolean forceFizzle = false;

        ActiveCast(ServerPlayer player, Spell spell, int castTime, int fizzleTick, List<Integer> qteTriggerTicks) {
            this.player = player;
            this.spell = spell;
            this.totalTicks = castTime;
            this.remainingTicks = castTime;
            this.fizzleTick = fizzleTick;
            this.qteTriggerTicks = qteTriggerTicks;
        }
    }
}

