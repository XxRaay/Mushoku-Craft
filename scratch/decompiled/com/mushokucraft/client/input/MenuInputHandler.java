/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.client.event.InputEvent$Key
 */
package com.mushokucraft.client.input;

import com.mushokucraft.client.gui.SpellWheelScreen;
import com.mushokucraft.client.gui.StanceWheelScreen;
import com.mushokucraft.client.input.ModKeybindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid="mushokucraft", value={Dist.CLIENT}, bus=EventBusSubscriber.Bus.GAME)
public class MenuInputHandler {
    private static boolean wasRPressed = false;
    private static boolean wasXPressed = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        boolean isRPressedNow = ModKeybindings.SPELL_MENU_KEY.isDown();
        if (isRPressedNow && !wasRPressed) {
            mc.setScreen((Screen)new SpellWheelScreen());
            wasRPressed = true;
        } else if (!isRPressedNow && wasRPressed) {
            wasRPressed = false;
        }
        boolean isXPressedNow = ModKeybindings.STANCE_MENU_KEY.isDown();
        if (isXPressedNow && !wasXPressed) {
            if (!(mc.screen instanceof StanceWheelScreen)) {
                mc.setScreen((Screen)new StanceWheelScreen());
                wasXPressed = true;
            }
        } else if (!isXPressedNow && wasXPressed) {
            wasXPressed = false;
        }
    }
}

