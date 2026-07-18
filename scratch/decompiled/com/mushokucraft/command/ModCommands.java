/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.arguments.EntityArgument
 *  net.minecraft.commands.arguments.ResourceLocationArgument
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 */
package com.mushokucraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mushokucraft.combat.SwordCombatHandler;
import com.mushokucraft.command.AdminCommandService;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="mushokucraft")
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher dispatcher = event.getDispatcher();
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"mushoku").requires(source -> source.hasPermission(2))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"mana").then(Commands.literal((String)"set").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)0.0f)).executes(ctx -> ModCommands.setMana((CommandContext<CommandSourceStack>)ctx, "set")))))).then(Commands.literal((String)"setmax").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)0.0f)).executes(ctx -> ModCommands.setMana((CommandContext<CommandSourceStack>)ctx, "setmax")))))).then(Commands.literal((String)"setregen").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)0.0f)).executes(ctx -> ModCommands.setMana((CommandContext<CommandSourceStack>)ctx, "setregen"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"mastery").then(Commands.literal((String)"spell").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"spell", (ArgumentType)ResourceLocationArgument.id()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(ctx -> ModCommands.setMastery((CommandContext<CommandSourceStack>)ctx, "spell"))))))).then(Commands.literal((String)"school").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"school", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(ctx -> ModCommands.setMastery((CommandContext<CommandSourceStack>)ctx, "school"))))))).then(Commands.literal((String)"stance").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"stance", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"amount", (ArgumentType)FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(ctx -> ModCommands.setMastery((CommandContext<CommandSourceStack>)ctx, "stance")))))))).then(Commands.literal((String)"debug").executes(ctx -> {
            Entity patt0$temp = ((CommandSourceStack)ctx.getSource()).getEntity();
            if (patt0$temp instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)patt0$temp;
                UUID uuid = player.getUUID();
                if (SwordCombatHandler.DEBUG_PLAYERS.contains(uuid)) {
                    SwordCombatHandler.DEBUG_PLAYERS.remove(uuid);
                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"Touki debug panel \u00a7cDISABLED"), false);
                } else {
                    SwordCombatHandler.DEBUG_PLAYERS.add(uuid);
                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"Touki debug panel \u00a7aENABLED"), false);
                }
            }
            return 1;
        })));
    }

    private static int setMana(CommandContext<CommandSourceStack> ctx, String type) throws CommandSyntaxException {
        Collection players = EntityArgument.getPlayers(ctx, (String)"targets");
        float amount = FloatArgumentType.getFloat(ctx, (String)"amount");
        return AdminCommandService.setMana((CommandSourceStack)ctx.getSource(), players, amount, type);
    }

    private static int setMastery(CommandContext<CommandSourceStack> ctx, String type) throws CommandSyntaxException {
        Collection players = EntityArgument.getPlayers(ctx, (String)"targets");
        float amount = FloatArgumentType.getFloat(ctx, (String)"amount");
        if (type.equals("spell")) {
            ResourceLocation spell = ResourceLocationArgument.getId(ctx, (String)"spell");
            return AdminCommandService.setSpellMastery((CommandSourceStack)ctx.getSource(), players, spell, amount);
        }
        if (type.equals("school")) {
            String schoolStr = StringArgumentType.getString(ctx, (String)"school").toUpperCase();
            return AdminCommandService.setSchoolMastery((CommandSourceStack)ctx.getSource(), players, schoolStr, amount);
        }
        if (type.equals("stance")) {
            String stanceStr = StringArgumentType.getString(ctx, (String)"stance").toUpperCase();
            return AdminCommandService.setStanceMastery((CommandSourceStack)ctx.getSource(), players, stanceStr, amount);
        }
        return 0;
    }
}

