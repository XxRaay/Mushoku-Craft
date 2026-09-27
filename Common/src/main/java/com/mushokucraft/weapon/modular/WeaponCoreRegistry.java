package com.mushokucraft.weapon.modular;

import com.mushokucraft.init.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponCoreRegistry {
    private static final Map<String, WeaponCoreType> CORES = new LinkedHashMap<>();

    public static final WeaponCoreType SABERTOOTH_WOLF = register(new WeaponCoreType(
            "sabertooth_wolf",
            "item.mushokucraft.sabertooth_core",
            0xFF6600,
            "core.mushokucraft.sabertooth_wolf.perk",
            "core.mushokucraft.sabertooth_wolf.desc"
    ));

    public static WeaponCoreType register(WeaponCoreType core) {
        CORES.put(core.getId(), core);
        return core;
    }

    public static WeaponCoreType get(String id) {
        if (id == null) return null;
        return CORES.get(id.toLowerCase());
    }

    public static Collection<WeaponCoreType> getAll() {
        return Collections.unmodifiableCollection(CORES.values());
    }

    public static WeaponCoreType fromItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.is(ModItems.SABERTOOTH_CORE.get())) {
            return SABERTOOTH_WOLF;
        }
        return null;
    }
}
