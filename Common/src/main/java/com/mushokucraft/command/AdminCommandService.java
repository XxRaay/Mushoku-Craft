package com.mushokucraft.command;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Collection;
import java.util.Optional;

public class AdminCommandService {
    
    public static int setMana(CommandSourceStack source, Collection<ServerPlayer> players, float amount, String type) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            switch (type) {
                case "set" -> data.setMana(amount);
                case "setmax" -> data.setMaxMana(amount);
                case "setregen" -> data.setManaRegenRate(amount);
            }
            ModGameEvents.syncMana(player, data);
            String translationKey = switch (type) {
                case "set" -> "command.mushokucraft.mana.set.success";
                case "setmax" -> "command.mushokucraft.mana.max.success";
                case "setregen" -> "command.mushokucraft.mana.regen.success";
                default -> "command.mushokucraft.mana.success";
            };
            
            source.sendSuccess(() -> Component.translatable(translationKey, player.getScoreboardName(), amount), true);
        }
        return players.size();
    }

    public static int setSpellMastery(CommandSourceStack source, Collection<ServerPlayer> players, net.minecraft.resources.ResourceLocation spell, float amount) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            data.setSpellMastery(spell, amount);
            source.sendSuccess(() -> Component.translatable("command.mushokucraft.mastery.spell.success", player.getScoreboardName(), spell.toString(), amount * 100), true);
            dev.architectury.networking.NetworkManager.sendToPlayer(player, new com.mushokucraft.network.SyncFullMasteryPacket(data.serializeNBT(player.level().registryAccess())));
        }
        return players.size();
    }
    
    public static int setSchoolMastery(CommandSourceStack source, Collection<ServerPlayer> players, String schoolStr, float amount) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            try {
                com.mushokucraft.magic.MagicSchool school = com.mushokucraft.magic.MagicSchool.valueOf(schoolStr);
                data.setSchoolMastery(school, amount);
                source.sendSuccess(() -> Component.translatable("command.mushokucraft.mastery.school.success", player.getScoreboardName(), schoolStr, amount * 100), true);
                dev.architectury.networking.NetworkManager.sendToPlayer(player, new com.mushokucraft.network.SyncFullMasteryPacket(data.serializeNBT(player.level().registryAccess())));
            } catch (IllegalArgumentException e) {
                source.sendFailure(Component.translatable("command.mushokucraft.mastery.school.invalid", schoolStr));
                return 0;
            }
        }
        return players.size();
    }
    
    public static int setStanceMastery(CommandSourceStack source, Collection<ServerPlayer> players, String stanceStr, float amount) {
        for (ServerPlayer player : players) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            try {
                com.mushokucraft.combat.SwordStyle stance = com.mushokucraft.combat.SwordStyle.valueOf(stanceStr);
                if (stance == com.mushokucraft.combat.SwordStyle.SWORD_GOD) data.setSwordGodMastery(amount);
                else if (stance == com.mushokucraft.combat.SwordStyle.WATER_GOD) data.setWaterGodMastery(amount);
                else if (stance == com.mushokucraft.combat.SwordStyle.NORTH_GOD) data.setNorthGodMastery(amount);
                source.sendSuccess(() -> Component.translatable("command.mushokucraft.mastery.stance.success", player.getScoreboardName(), stanceStr, amount * 100), true);
                dev.architectury.networking.NetworkManager.sendToPlayer(player, new com.mushokucraft.network.SyncFullMasteryPacket(data.serializeNBT(player.level().registryAccess())));
            } catch (IllegalArgumentException e) {
                source.sendFailure(Component.translatable("command.mushokucraft.mastery.stance.invalid", stanceStr));
                return 0;
            }
        }
        return players.size();
    }

    public static int spawnStructure(CommandSourceStack source, BlockPos targetPos, String structureName) {
        ServerLevel level = source.getLevel();
        ResourceLocation location;
        if (structureName.contains(":")) {
            location = ResourceLocation.parse(structureName);
        } else if ("mage_house".equalsIgnoreCase(structureName)) {
            location = ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "village/houses/mage_house");
        } else {
            location = ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, structureName);
        }

        Optional<StructureTemplate> templateOpt = level.getStructureManager().get(location);
        if (templateOpt.isEmpty()) {
            source.sendFailure(Component.literal("§c[MushokuCraft] Структура не найдена: " + location));
            return 0;
        }

        StructureTemplate template = templateOpt.get();
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);

        boolean placed = template.placeInWorld(level, targetPos, targetPos, settings, level.getRandom(), Block.UPDATE_ALL);
        if (placed) {
            source.sendSuccess(() -> Component.literal("§a[MushokuCraft] Структура '" + structureName + "' успешно размещена на " + targetPos.toShortString()), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§c[MushokuCraft] Не удалось разместить структуру на " + targetPos.toShortString()));
            return 0;
        }
    }
}





