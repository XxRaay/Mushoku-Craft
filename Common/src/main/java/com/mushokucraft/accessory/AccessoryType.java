package com.mushokucraft.accessory;

import net.minecraft.network.chat.Component;

public enum AccessoryType {
    RING("ring", "tooltip.mushokucraft.accessory.type.ring", "§6"),
    NECKLACE("necklace", "tooltip.mushokucraft.accessory.type.necklace", "§5"),
    CHARM("charm", "tooltip.mushokucraft.accessory.type.charm", "§c"),
    BELT("belt", "tooltip.mushokucraft.accessory.type.belt", "§e"),
    HANDS("hands", "tooltip.mushokucraft.accessory.type.hands", "§2"),
    BODY("body", "tooltip.mushokucraft.accessory.type.body", "§b");

    private final String id;
    private final String translationKey;
    private final String colorCode;

    AccessoryType(String id, String translationKey, String colorCode) {
        this.id = id;
        this.translationKey = translationKey;
        this.colorCode = colorCode;
    }

    public String getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return Component.literal("§7[" + this.colorCode).append(Component.translatable(this.translationKey)).append(Component.literal("§7]"));
    }
}
