package com.mushokucraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;

public class ModKeybindings {
    public static final String CATEGORY = "key.category.mushokucraft";
    public static final KeyMapping SPELL_MENU_KEY = new KeyMapping("key.mushokucraft.spell_menu", InputConstants.Type.KEYSYM, 82, CATEGORY);
    public static final KeyMapping STANCE_MENU_KEY = new KeyMapping("key.mushokucraft.stance_menu", InputConstants.Type.KEYSYM, 88, CATEGORY);

    public static void register() {
        KeyMappingRegistry.register(SPELL_MENU_KEY);
        KeyMappingRegistry.register(STANCE_MENU_KEY);
    }
}

