package com.mushokucraft.client.input;

import com.mushokucraft.client.gui.SpellWheelScreen;
import com.mushokucraft.client.gui.StanceWheelScreen;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;

public class MenuInputHandler {
    private static boolean wasRPressed = false;
    private static boolean wasXPressed = false;

    public static void register() {
        ClientTickEvent.CLIENT_POST.register(mc -> {
            if (mc.screen != null || mc.player == null) {
                return;
            }
            while (ModKeybindings.SPELL_MENU_KEY.consumeClick()) {
                mc.setScreen(new SpellWheelScreen());
            }
            while (ModKeybindings.STANCE_MENU_KEY.consumeClick()) {
                mc.setScreen(new StanceWheelScreen());
            }
        });
    }
}

