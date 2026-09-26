package com.mushokucraft;

import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.init.ModCreativeTabs;
import com.mushokucraft.init.ModEffects;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.init.ModLootModifiers;
import com.mushokucraft.network.ModNetworking;
import com.mushokucraft.event.*;
import com.mushokucraft.combat.*;
import com.mushokucraft.magic.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MushokuCraftCommon {
    public static final String MOD_ID = "mushokucraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Initializing Mushoku Craft Common...");
        
        // Register Registries
        ModBlocks.register();
        com.mushokucraft.init.ModBlockEntities.register();
        com.mushokucraft.init.ModMenuTypes.register();
        ModEntities.register();
        ModItems.register();
        ModCreativeTabs.register();
        ModEffects.register();
        ModLootModifiers.register();
        com.mushokucraft.init.ModRecipeSerializers.register();
        
        // Register Networking
        ModNetworking.register();
        
        // Register Events
        ModGameEvents.register();
        ModEventBusEvents.register();
        AdvancementHandler.register();
        PlayerInteractionBlocker.register();
        PlayerSessionHandler.register();
        com.mushokucraft.command.ModCommands.register();
        
        // Combat
        SwordCombatHandler.register();
        ParryHandler.register();
        NorthGodHandler.register();
        
        // Magic
        LearningManager.register();
        ServerCastManager.register();
        ServerChargeManager.register();
        com.mushokucraft.magic.companion.SummonCompanionManager.init();
        com.mushokucraft.magic.circle.SoulRecallHandler.register();
        
        LOGGER.info("Mushoku Craft Common initialized.");
    }
}
