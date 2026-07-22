package com.mushokucraft.init;

/**
 * Global Loot Modifiers are NeoForge-specific.
 * Loot injection is handled per-platform:
 *  - NeoForge: via IGlobalLootModifier (in NeoForge_1.21.1 module)
 *  - Fabric: via LootTableEvents (in Fabric_1.21.1 module)
 * 
 * This class is a placeholder to avoid breaking references.
 */
public class ModLootModifiers {
    public static void register() {
        // Platform-specific loot modifiers are registered in their respective modules.
        // Common module uses Architectury's LootEvent for cross-platform loot injection.
    }
}
