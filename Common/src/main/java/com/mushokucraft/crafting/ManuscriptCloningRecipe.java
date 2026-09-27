package com.mushokucraft.crafting;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.init.ModRecipeSerializers;
import com.mushokucraft.item.BlankCanvasItem;
import com.mushokucraft.item.InscribedManuscriptItem;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ManuscriptCloningRecipe extends CustomRecipe {

    public ManuscriptCloningRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int manuscriptCount = 0;
        int canvasCount = 0;
        int totalInkSacs = 0;
        ItemStack manuscript = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.INSCRIBED_MANUSCRIPT.get())) {
                manuscriptCount++;
                manuscript = stack;
            } else if (stack.is(ModItems.BLANK_CANVAS.get())) {
                // Must be an empty blank canvas (no draft in progress)
                if (BlankCanvasItem.getDraftPattern(stack).countFilled() > 0) {
                    return false;
                }
                canvasCount += stack.getCount();
            } else if (stack.is(Items.INK_SAC)) {
                totalInkSacs += stack.getCount();
            } else {
                return false;
            }
        }

        if (manuscriptCount != 1 || canvasCount != 1) {
            return false;
        }

        MagicCirclePattern pattern = InscribedManuscriptItem.getPattern(manuscript);
        if (pattern == null || pattern.countFilled() == 0) {
            return false;
        }

        int pixelsPerSac = Math.max(1, MushokuConfig.MAGIC_CIRCLE_INK_PIXELS_PER_SAC.get());
        int requiredInkSacs = (pattern.countFilled() + pixelsPerSac - 1) / pixelsPerSac;

        return totalInkSacs >= requiredInkSacs;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack manuscript = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.INSCRIBED_MANUSCRIPT.get())) {
                manuscript = stack;
                break;
            }
        }

        if (manuscript.isEmpty()) return ItemStack.EMPTY;

        MagicCirclePattern pattern = InscribedManuscriptItem.getPattern(manuscript);
        ResourceLocation circleType = InscribedManuscriptItem.getCircleTypeId(manuscript);

        // Creates a fresh, unlinked duplicate manuscript with identical pattern and type
        return InscribedManuscriptItem.create(pattern, circleType);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        ItemStack manuscript = ItemStack.EMPTY;
        int manuscriptIndex = -1;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(ModItems.INSCRIBED_MANUSCRIPT.get())) {
                manuscript = stack;
                manuscriptIndex = i;
                break;
            }
        }

        if (!manuscript.isEmpty() && manuscriptIndex >= 0) {
            // Keep the original inscribed manuscript in its crafting slot
            remaining.set(manuscriptIndex, manuscript.copyWithCount(1));

            MagicCirclePattern pattern = InscribedManuscriptItem.getPattern(manuscript);
            int pixelsPerSac = Math.max(1, MushokuConfig.MAGIC_CIRCLE_INK_PIXELS_PER_SAC.get());
            int inkToConsume = (pattern.countFilled() + pixelsPerSac - 1) / pixelsPerSac;

            // Consume exact amount of ink sacs across single or multiple slots
            for (int i = 0; i < input.size() && inkToConsume > 0; i++) {
                ItemStack stack = input.getItem(i);
                if (stack.is(Items.INK_SAC)) {
                    int additionalToShrink = Math.min(stack.getCount() - 1, inkToConsume - 1);
                    if (additionalToShrink > 0) {
                        stack.shrink(additionalToShrink);
                        inkToConsume -= additionalToShrink;
                    }
                    inkToConsume -= 1; // Minecraft will automatically shrink each non-empty crafting slot by 1
                }
            }
        }

        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MANUSCRIPT_CLONING.get();
    }
}
