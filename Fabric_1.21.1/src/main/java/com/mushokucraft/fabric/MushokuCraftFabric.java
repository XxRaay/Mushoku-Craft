package com.mushokucraft.fabric;

import com.mushokucraft.MushokuCraftCommon;
import net.fabricmc.api.ModInitializer;

public class MushokuCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MushokuCraftCommon.init();
        
        FabricLootModifiers.register();
        com.mushokucraft.fabric.trinkets.TrinketsIntegration.init();
    }
}
