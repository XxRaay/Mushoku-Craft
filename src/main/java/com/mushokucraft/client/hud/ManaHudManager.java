package com.mushokucraft.client.hud;

import com.mushokucraft.config.MushokuConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Top-level mana HUD manager.
 * Delegates rendering to {@link ClassicManaBarRenderer} and/or {@link ArcManaRenderer}
 * based on the configured {@link ManaDisplayMode}.
 * <p>
 * Registered as a GUI layer in {@link com.mushokucraft.client.ModClientSetup}.
 */
public class ManaHudManager {

    /** Shared animation state — updated by game events, read by renderers. */
    public static final ManaAnimationState ANIMATION_STATE = new ManaAnimationState();

    /**
     * Called every frame by the registered GUI layer.
     */
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        ManaDisplayMode mode = MushokuConfig.MANA_DISPLAY_MODE.get();

        if (ANIMATION_STATE.getMaxMana() <= 0) {
            return; // Completely hide if max mana is 0
        }

        // Determine global alpha based on mode
        float globalAlpha = 1.0f;
        if (mode == ManaDisplayMode.MINIMAL) {
            globalAlpha = ANIMATION_STATE.getManaFadeAlpha();
            if (globalAlpha <= 0.01f) return; // Fully hidden
        }
        
        // Dynamic arc specifically fades out when full in all modes (except classic which doesn't use it)
        float arcAlpha = mode == ManaDisplayMode.MINIMAL ? globalAlpha * 0.7f : ANIMATION_STATE.getManaFadeAlpha();

        switch (mode) {
            case CLASSIC -> {
                ClassicManaBarRenderer.render(guiGraphics, ANIMATION_STATE,
                        partialTick, globalAlpha);
            }
            case DYNAMIC -> {
                if (arcAlpha > 0.01f) {
                    ArcManaRenderer.render(guiGraphics, ANIMATION_STATE,
                            partialTick, arcAlpha);
                }
            }
            case HYBRID -> {
                // Arc at crosshair for real-time feedback (no numbers)
                if (arcAlpha > 0.01f) {
                    ArcManaRenderer.render(guiGraphics, ANIMATION_STATE,
                            partialTick, arcAlpha);
                }
                // Classic bar for full stats
                ClassicManaBarRenderer.render(guiGraphics, ANIMATION_STATE,
                        partialTick, globalAlpha);
            }
            case MINIMAL -> {
                // Show whichever the user last used (default to classic)
                ClassicManaBarRenderer.render(guiGraphics, ANIMATION_STATE,
                        partialTick, globalAlpha);
                if (arcAlpha > 0.01f) {
                    ArcManaRenderer.render(guiGraphics, ANIMATION_STATE,
                            partialTick, arcAlpha);
                }
            }
        }
    }

    /**
     * Called every client tick to advance animation timers.
     */
    public static void tick() {
        ANIMATION_STATE.tick();
    }
}





