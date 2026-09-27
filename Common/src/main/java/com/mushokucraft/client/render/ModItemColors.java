package com.mushokucraft.client.render;

import com.mushokucraft.weapon.modular.ModularWeaponItem;
import com.mushokucraft.weapon.modular.WeaponCoreType;
import com.mushokucraft.weapon.modular.WeaponMaterial;
import net.minecraft.world.item.ItemStack;

public class ModItemColors {
    public static int getModularWeaponColor(ItemStack stack, int tintIndex) {
        if (stack.getItem() instanceof ModularWeaponItem) {
            WeaponMaterial handle = ModularWeaponItem.getHandleMaterial(stack);
            WeaponMaterial guard = ModularWeaponItem.getGuardMaterial(stack);
            WeaponMaterial blade = ModularWeaponItem.getBladeMaterial(stack);
            WeaponCoreType core = ModularWeaponItem.getCore(stack);

            return switch (tintIndex) {
                case 0 -> toOpaque(handle != null ? handle.color() : 0xB8945F);
                case 1 -> toOpaque(guard != null ? guard.color() : 0xDCDCDC);
                case 2 -> toOpaque(blade != null ? blade.color() : 0xDCDCDC);
                case 3 -> core != null ? toOpaque(core.getColor()) : 0xFFFFFFFF;
                default -> 0xFFFFFFFF;
            };
        }
        return 0xFFFFFFFF;
    }

    private static int toOpaque(int rgb) {
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }
}
