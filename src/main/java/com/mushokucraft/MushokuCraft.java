package com.mushokucraft;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.init.ModCreativeTabs;
import com.mushokucraft.init.ModEffects;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.init.ModLootModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value="mushokucraft")
public class MushokuCraft {
    public static final String MOD_ID = "mushokucraft";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"mushokucraft");

    public MushokuCraft(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Mushoku Craft...");
        modContainer.registerConfig(ModConfig.Type.CLIENT, MushokuConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, MushokuConfig.SERVER_SPEC);
        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        ModAttachments.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEffects.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        LOGGER.info("Mushoku Craft initialized.");
    }
}


