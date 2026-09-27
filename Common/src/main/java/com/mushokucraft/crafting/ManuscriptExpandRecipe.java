package com.mushokucraft.crafting;

import com.mushokucraft.init.ModItems;
import com.mushokucraft.init.ModRecipeSerializers;
import com.mushokucraft.item.InscribedManuscriptItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ManuscriptExpandRecipe extends CustomRecipe {

    public ManuscriptExpandRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }

        // Center item (index 4 in a 3x3 grid)
        ItemStack center = input.getItem(4);
        if (!center.is(ModItems.INSCRIBED_MANUSCRIPT.get())) {
            return false;
        }

        // Must not already be 3x3
        if (InscribedManuscriptItem.getSize(center) >= 3) {
            return false;
        }

        // All 8 surrounding slots (0, 1, 2, 3, 5, 6, 7, 8) must be paper
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            ItemStack stack = input.getItem(i);
            if (!stack.is(Items.PAPER)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack center = input.getItem(4);
        if (center.isEmpty() || !center.is(ModItems.INSCRIBED_MANUSCRIPT.get())) {
            return ItemStack.EMPTY;
        }

        ItemStack result = center.copy();
        result.setCount(1);
        InscribedManuscriptItem.setSize(result, 3);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MANUSCRIPT_EXPAND.get();
    }
}
