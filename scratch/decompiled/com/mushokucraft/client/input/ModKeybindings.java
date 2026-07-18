/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.client.settings.IKeyConflictContext
 *  net.neoforged.neoforge.client.settings.KeyConflictContext
 */
package com.mushokucraft.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid="mushokucraft", value={Dist.CLIENT}, bus=EventBusSubscriber.Bus.MOD)
public class ModKeybindings {
    public static final String CATEGORY = "key.category.mushokucraft";
    public static final KeyMapping SPELL_MENU_KEY = new KeyMapping("key.mushokucraft.spell_menu", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 82, "key.category.mushokucraft");
    public static final KeyMapping STANCE_MENU_KEY = new KeyMapping("key.mushokucraft.stance_menu", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 88, "key.category.mushokucraft");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SPELL_MENU_KEY);
        event.register(STANCE_MENU_KEY);
    }
}

