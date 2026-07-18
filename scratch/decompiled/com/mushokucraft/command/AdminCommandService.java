/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.command;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.magic.MagicSchool;
import com.mushokucraft.network.SyncFullMasteryPacket;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdminCommandService {
    public static int setMana(CommandSourceStack source, Collection<ServerPlayer> players, float amount, String type) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            switch (type) {
                case "set": {
                    data.setMana(amount);
                    break;
                }
                case "setmax": {
                    data.setMaxMana(amount);
                    break;
                }
                case "setregen": {
                    data.setManaRegenRate(amount);
                }
            }
            ModGameEvents.syncMana(player, data);
            String translationKey = switch (type) {
                case "set" -> "command.mushokucraft.mana.set.success";
                case "setmax" -> "command.mushokucraft.mana.max.success";
                case "setregen" -> "command.mushokucraft.mana.regen.success";
                default -> "command.mushokucraft.mana.success";
            };
            source.sendSuccess(() -> Component.translatable((String)translationKey, (Object[])new Object[]{player.getScoreboardName(), Float.valueOf(amount)}), true);
        }
        return players.size();
    }

    public static int setSpellMastery(CommandSourceStack source, Collection<ServerPlayer> players, ResourceLocation spell, float amount) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            data.setSpellMastery(spell, amount);
            source.sendSuccess(() -> Component.translatable((String)"command.mushokucraft.mastery.spell.success", (Object[])new Object[]{player.getScoreboardName(), spell.toString(), Float.valueOf(amount * 100.0f)}), true);
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player.level().registryAccess())), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        return players.size();
    }

    public static int setSchoolMastery(CommandSourceStack source, Collection<ServerPlayer> players, String schoolStr, float amount) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            try {
                MagicSchool school = MagicSchool.valueOf(schoolStr);
                data.setSchoolMastery(school, amount);
                source.sendSuccess(() -> Component.translatable((String)"command.mushokucraft.mastery.school.success", (Object[])new Object[]{player.getScoreboardName(), schoolStr, Float.valueOf(amount * 100.0f)}), true);
                PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player.level().registryAccess())), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            catch (IllegalArgumentException e) {
                source.sendFailure((Component)Component.translatable((String)"command.mushokucraft.mastery.school.invalid", (Object[])new Object[]{schoolStr}));
                return 0;
            }
        }
        return players.size();
    }

    public static int setStanceMastery(CommandSourceStack source, Collection<ServerPlayer> players, String stanceStr, float amount) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            try {
                SwordStyle stance = SwordStyle.valueOf(stanceStr);
                if (stance == SwordStyle.SWORD_GOD) {
                    data.setSwordGodMastery(amount);
                } else if (stance == SwordStyle.WATER_GOD) {
                    data.setWaterGodMastery(amount);
                } else if (stance == SwordStyle.NORTH_GOD) {
                    data.setNorthGodMastery(amount);
                }
                source.sendSuccess(() -> Component.translatable((String)"command.mushokucraft.mastery.stance.success", (Object[])new Object[]{player.getScoreboardName(), stanceStr, Float.valueOf(amount * 100.0f)}), true);
                PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player.level().registryAccess())), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            catch (IllegalArgumentException e) {
                source.sendFailure((Component)Component.translatable((String)"command.mushokucraft.mastery.stance.invalid", (Object[])new Object[]{stanceStr}));
                return 0;
            }
        }
        return players.size();
    }
}

