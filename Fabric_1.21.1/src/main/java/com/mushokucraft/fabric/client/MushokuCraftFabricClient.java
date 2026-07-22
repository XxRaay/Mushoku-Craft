package com.mushokucraft.fabric.client;

import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.client.input.MenuInputHandler;
import com.mushokucraft.client.input.ModKeybindings;
import com.mushokucraft.client.input.QteInputHandler;
import com.mushokucraft.client.ModClientSetup;
import com.mushokucraft.event.ModClientEvents;
import net.fabricmc.api.ClientModInitializer;

public class MushokuCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModClientEvents.register();
        ModClientSetup.register();
        
        CombatInputHandler.register();
        MenuInputHandler.register();
        QteInputHandler.register();
        ModKeybindings.register();
    }
}
