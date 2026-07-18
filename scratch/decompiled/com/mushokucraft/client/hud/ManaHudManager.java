/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.mushokucraft.client.hud;

import com.mushokucraft.client.hud.ArcManaRenderer;
import com.mushokucraft.client.hud.ClassicManaBarRenderer;
import com.mushokucraft.client.hud.ManaAnimationState;
import com.mushokucraft.client.hud.ManaDisplayMode;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class ManaHudManager {
    public static final ManaAnimationState ANIMATION_STATE = new ManaAnimationState();

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        ManaDisplayMode mode = (ManaDisplayMode)((Object)MushokuConfig.MANA_DISPLAY_MODE.get());
        if (ANIMATION_STATE.getMaxMana() <= 0.0f) {
            return;
        }
        float globalAlpha = 1.0f;
        if (mode == ManaDisplayMode.MINIMAL && (globalAlpha = ANIMATION_STATE.getManaFadeAlpha()) <= 0.01f) {
            return;
        }
        float arcAlpha = mode == ManaDisplayMode.MINIMAL ? globalAlpha * 0.7f : ANIMATION_STATE.getManaFadeAlpha();
        switch (mode) {
            case CLASSIC: {
                ClassicManaBarRenderer.render(guiGraphics, ANIMATION_STATE, partialTick, globalAlpha);
                break;
            }
            case DYNAMIC: {
                if (!(arcAlpha > 0.01f)) break;
                ArcManaRenderer.render(guiGraphics, ANIMATION_STATE, partialTick, arcAlpha);
                break;
            }
            case HYBRID: {
                if (arcAlpha > 0.01f) {
                    ArcManaRenderer.render(guiGraphics, ANIMATION_STATE, partialTick, arcAlpha);
                }
                ClassicManaBarRenderer.render(guiGraphics, ANIMATION_STATE, partialTick, globalAlpha);
                break;
            }
            case MINIMAL: {
                ClassicManaBarRenderer.render(guiGraphics, ANIMATION_STATE, partialTick, globalAlpha);
                if (!(arcAlpha > 0.01f)) break;
                ArcManaRenderer.render(guiGraphics, ANIMATION_STATE, partialTick, arcAlpha);
            }
        }
    }

    public static void tick() {
        ANIMATION_STATE.tick();
    }
}

