package com.mushokucraft.compat.jei;

import com.mushokucraft.weapon.modular.WeaponForm;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ModularAnvilRecipe(
        WeaponForm form,
        List<ItemStack> bladeMaterials,
        List<ItemStack> guardMaterials,
        List<ItemStack> handleMaterials,
        List<ItemStack> cores,
        ItemStack outputSample,
        float manaCost
) {}
