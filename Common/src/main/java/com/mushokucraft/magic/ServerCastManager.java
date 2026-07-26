package com.mushokucraft.magic;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.MasteryCalculator;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.data.PlayerMasteryProvider;
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
import dev.architectury.networking.NetworkManager;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.PlayerEvent;

public class ServerCastManager {
    private static final Map<UUID, ActiveCast> activeCasts = new HashMap<UUID, ActiveCast>();
    public static final Map<UUID, net.minecraft.world.phys.Vec3> lockedTargetPositions = new HashMap<>();

    public static void register() {
        TickEvent.SERVER_POST.register((server) -> {
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
                    PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    float schoolMastery = mastery.getSchoolMastery(cast.spell.getSchool());
                    float speedModifier = MasteryCalculator.calculateQteSpeedModifier(schoolMastery, cast.spell.getRank().getTier());
                    float targetSizeModifier = MasteryCalculator.calculateQteTargetSize(cast.spell.getRank().getTier());
                    float perfectMultiplier = 0.4f * MasteryCalculator.calculateQteSizeModifier(schoolMastery);
                    NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new QteTriggerPacket(randomKey, speedModifier, targetSizeModifier, perfectMultiplier));
                    continue;
                }
                if (cast.forceFizzle || cast.fizzleTick > 0 && cast.totalTicks - cast.remainingTicks >= cast.fizzleTick) {
                    data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    data.consumeMana(cast.spell.getEffectiveManaCost(data));
                    NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncManaPacket(data.getMana(), data.getMaxMana()));
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    iterator.remove();
                    continue;
                }
                if (cast.remainingTicks > 0) continue;
                data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
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
        });

        PlayerEvent.PLAYER_QUIT.register((player) -> {
            activeCasts.remove(player.getUUID());
        });
    }

    public static void startCast(ServerPlayer player, Spell spell, int castTime, int fizzleTick) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, castTime + 10, 1, false, false, true));
        
        if (spell.getId().getPath().equals("icicle_break")) {
            net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            net.minecraft.world.phys.Vec3 endPos = eyePos.add(look.scale(7.0));
            net.minecraft.world.phys.HitResult result = player.level().clip(new net.minecraft.world.level.ClipContext(eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
            net.minecraft.world.phys.Vec3 hitPos = result.getLocation();
            net.minecraft.world.phys.HitResult groundResult = player.level().clip(new net.minecraft.world.level.ClipContext(hitPos, hitPos.add(0, -64, 0), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));
            lockedTargetPositions.put(player.getUUID(), groundResult.getLocation());
        }

        PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
        float spellMastery = mastery.getSpellMastery(spell.getId());
        ArrayList<Integer> qteTicks = new ArrayList<Integer>();
        if (fizzleTick > 0) {
            qteTicks.add(castTime - fizzleTick);
        }
        ActiveCast cast = new ActiveCast(player, spell, castTime, fizzleTick, qteTicks);
        activeCasts.put(player.getUUID(), cast);
        float schoolMastery = mastery.getSchoolMastery(spell.getSchool());
        double successChance = MasteryCalculator.calculateSpellSuccessChance(spellMastery, schoolMastery);
        successChance = Math.max(0.0, successChance - MasteryCalculator.calculateTierPenalty(spell, mastery));
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
                PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                float spellMastery = mastery.getSpellMastery(cast.spell.getId());
                double successChance = 1.0 - (double)cast.spell.getFizzleChance(spellMastery, mastery);
                ServerCastManager.scheduleNextFizzle(cast, successChance);
            }
        }
    }

    private static void scheduleNextFizzle(ActiveCast cast, double successChance) {
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
