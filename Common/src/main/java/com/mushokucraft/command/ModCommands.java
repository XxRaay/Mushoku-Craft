package com.mushokucraft.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mushokucraft.combat.SwordCombatHandler;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.UUID;

public class ModCommands {
    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("mushoku")
                    .requires(source -> source.hasPermission(2))
                    // Mana
                    .then(Commands.literal("mana")
                            .then(Commands.literal("set")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f))
                                                    .executes(ctx -> setMana(ctx, "set")))))
                            .then(Commands.literal("setmax")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f))
                                                    .executes(ctx -> setMana(ctx, "setmax")))))
                            .then(Commands.literal("setregen")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f))
                                                    .executes(ctx -> setMana(ctx, "setregen"))))))
                    // Mastery
                    .then(Commands.literal("mastery")
                            .then(Commands.literal("spell")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("spell", ResourceLocationArgument.id())
                                                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                            .executes(ctx -> setMastery(ctx, "spell"))))))
                            .then(Commands.literal("school")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("school", StringArgumentType.word())
                                                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                            .executes(ctx -> setMastery(ctx, "school"))))))
                            .then(Commands.literal("stance")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                            .then(Commands.argument("stance", StringArgumentType.word())
                                                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                            .executes(ctx -> setMastery(ctx, "stance")))))))
                    // Structure: /mushoku structure <name> [pos]
                    .then(Commands.literal("structure")
                            .then(Commands.literal("mage_house")
                                    .executes(ctx -> spawnStructure(ctx, "mage_house", false))
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                            .executes(ctx -> spawnStructure(ctx, "mage_house", true))))
                            .then(Commands.argument("name", StringArgumentType.string())
                                    .executes(ctx -> spawnStructure(ctx, StringArgumentType.getString(ctx, "name"), false))
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                            .executes(ctx -> spawnStructure(ctx, StringArgumentType.getString(ctx, "name"), true)))))
                    // Structure alias: /mushoku spawn_structure <name> [pos]
                    .then(Commands.literal("spawn_structure")
                            .then(Commands.literal("mage_house")
                                    .executes(ctx -> spawnStructure(ctx, "mage_house", false))
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                            .executes(ctx -> spawnStructure(ctx, "mage_house", true))))
                            .then(Commands.argument("name", StringArgumentType.string())
                                    .executes(ctx -> spawnStructure(ctx, StringArgumentType.getString(ctx, "name"), false))
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                            .executes(ctx -> spawnStructure(ctx, StringArgumentType.getString(ctx, "name"), true)))))
                    // Debug
                    .then(Commands.literal("debug").executes(ctx -> {
                        Entity entity = ctx.getSource().getEntity();
                        if (entity instanceof ServerPlayer player) {
                            UUID uuid = player.getUUID();
                            if (SwordCombatHandler.DEBUG_PLAYERS.contains(uuid)) {
                                SwordCombatHandler.DEBUG_PLAYERS.remove(uuid);
                                ctx.getSource().sendSuccess(() -> Component.literal("Touki debug panel §cDISABLED"), false);
                            } else {
                                SwordCombatHandler.DEBUG_PLAYERS.add(uuid);
                                ctx.getSource().sendSuccess(() -> Component.literal("Touki debug panel §aENABLED"), false);
                            }
                        }
                        return 1;
                    }))
            );

            // Standalone shortcut: /spawn_mage_house [pos]
            dispatcher.register(Commands.literal("spawn_mage_house")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> spawnStructure(ctx, "mage_house", false))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> spawnStructure(ctx, "mage_house", true)))
            );
        });
    }

    private static int spawnStructure(CommandContext<CommandSourceStack> ctx, String name, boolean hasPos) {
        CommandSourceStack source = ctx.getSource();
        BlockPos pos;
        if (hasPos) {
            try {
                pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            } catch (Exception e) {
                pos = BlockPos.containing(source.getPosition());
            }
        } else {
            pos = BlockPos.containing(source.getPosition());
        }
        return AdminCommandService.spawnStructure(source, pos, name);
    }

    private static int setMana(CommandContext<CommandSourceStack> ctx, String type) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        return AdminCommandService.setMana(ctx.getSource(), players, amount, type);
    }

    private static int setMastery(CommandContext<CommandSourceStack> ctx, String type) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
        float amount = FloatArgumentType.getFloat(ctx, "amount");
        if (type.equals("spell")) {
            ResourceLocation spell = ResourceLocationArgument.getId(ctx, "spell");
            return AdminCommandService.setSpellMastery(ctx.getSource(), players, spell, amount);
        }
        if (type.equals("school")) {
            String schoolStr = StringArgumentType.getString(ctx, "school").toUpperCase();
            return AdminCommandService.setSchoolMastery(ctx.getSource(), players, schoolStr, amount);
        }
        if (type.equals("stance")) {
            String stanceStr = StringArgumentType.getString(ctx, "stance").toUpperCase();
            return AdminCommandService.setStanceMastery(ctx.getSource(), players, stanceStr, amount);
        }
        return 0;
    }
}
