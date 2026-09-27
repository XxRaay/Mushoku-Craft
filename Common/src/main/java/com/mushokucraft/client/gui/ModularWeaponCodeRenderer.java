package com.mushokucraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mushokucraft.weapon.modular.ModularWeaponItem;
import com.mushokucraft.weapon.modular.WeaponCoreType;
import com.mushokucraft.weapon.modular.WeaponForm;
import com.mushokucraft.weapon.modular.WeaponMaterial;
import com.mushokucraft.weapon.modular.WeaponMaterialRegistry;
import com.mushokucraft.weapon.modular.WeaponQuality;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ModularWeaponCodeRenderer {
    private static ItemStack cachedStack = ItemStack.EMPTY;
    private static WeaponForm lastForm = null;
    private static WeaponMaterial lastBlade = null;
    private static WeaponMaterial lastGuard = null;
    private static WeaponMaterial lastHandle = null;
    private static WeaponCoreType lastCore = null;

    public static void renderWeaponPreview(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            WeaponForm form,
            WeaponMaterial blade,
            WeaponMaterial guard,
            WeaponMaterial handle,
            WeaponCoreType core,
            float tick
    ) {
        if (form == null) form = WeaponForm.SWORD;
        WeaponMaterial effectiveBlade = blade != null ? blade : WeaponMaterialRegistry.IRON;
        WeaponMaterial effectiveGuard = guard != null ? guard : WeaponMaterialRegistry.IRON;
        WeaponMaterial effectiveHandle = handle != null ? handle : WeaponMaterialRegistry.OAK;

        if (cachedStack.isEmpty() || form != lastForm || effectiveBlade != lastBlade || effectiveGuard != lastGuard || effectiveHandle != lastHandle || core != lastCore) {
            lastForm = form;
            lastBlade = effectiveBlade;
            lastGuard = effectiveGuard;
            lastHandle = effectiveHandle;
            lastCore = core;
            cachedStack = ModularWeaponItem.createWeapon(form, effectiveBlade, effectiveGuard, effectiveHandle, core, "", WeaponQuality.STANDARD, 1.0f, "");
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();

        // Subtle floating bobbing animation
        float bob = (float) Math.sin(tick * 0.08f) * 1.5f;

        // Anvil workstation center is (centerX, centerY). A 16x16 item scaled by 2.0x is 32x32.
        // Center offset: -16
        pose.translate(centerX - 16, centerY - 16 + bob, 100);
        pose.scale(2.0f, 2.0f, 2.0f);

        // Render the actual modular weapon ItemStack with all client layers and tints!
        graphics.renderItem(cachedStack, 0, 0);

        pose.popPose();

        // Beast core ambient magical glow if core is installed
        if (core != null) {
            float pulse = (float) (0.65f + 0.35f * Math.sin(tick * 0.15f));
            int alpha = (int) (pulse * 180);
            int glowColor = (alpha << 24) | (core.getColor() & 0x00FFFFFF);

            graphics.fill(centerX - 4, centerY - 4 + (int) bob, centerX + 4, centerY + 4 + (int) bob, glowColor);
            graphics.fill(centerX - 2, centerY - 2 + (int) bob, centerX + 2, centerY + 2 + (int) bob, 0xAAFFFFFF);
        }

        // Specular blade gleam sweep
        renderGleam(graphics, centerX - 14, centerX + 14, centerY - 6 + (int) bob, tick);
    }

    private static void renderGleam(GuiGraphics graphics, int minX, int maxX, int y, float tick) {
        float progress = (tick * 0.035f) % 3.0f; // gleam sweeps once every ~3 seconds
        if (progress <= 1.0f) {
            int gleamX = (int) Mth.lerp(progress, minX - 6, maxX + 6);
            graphics.fill(gleamX - 1, y - 1, gleamX + 2, y + 2, 0x88FFFFFF);
        }
    }
}
