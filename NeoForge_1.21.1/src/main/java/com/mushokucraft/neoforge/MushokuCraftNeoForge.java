package com.mushokucraft.neoforge;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.client.input.MenuInputHandler;
import com.mushokucraft.client.input.ModKeybindings;
import com.mushokucraft.client.input.QteInputHandler;
import com.mushokucraft.client.ModClientSetup;
import com.mushokucraft.event.ModClientEvents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import com.mushokucraft.neoforge.loot.NeoForgeLootModifiers;

@Mod(MushokuCraftCommon.MOD_ID)
public class MushokuCraftNeoForge {
    public MushokuCraftNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        MushokuCraftCommon.init();
        
        NeoForgeLootModifiers.register(modEventBus);
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModClientEvents.register();
            ModClientSetup.register();
            
            CombatInputHandler.register();
            MenuInputHandler.register();
            QteInputHandler.register();
            ModKeybindings.register();
        }
    }
}
