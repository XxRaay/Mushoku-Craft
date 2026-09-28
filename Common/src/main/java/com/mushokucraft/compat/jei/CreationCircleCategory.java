package com.mushokucraft.compat.jei;

import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.magic.circle.CreationCircleRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreationCircleCategory implements IRecipeCategory<CreationCircleRecipe> {
    public static final RecipeType<CreationCircleRecipe> RECIPE_TYPE =
            RecipeType.create("mushokucraft", "creation_circle", CreationCircleRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final Component title;

    public CreationCircleCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(160, 65);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.MAGIC_CIRCLE.get()));
        this.slotDrawable = helper.getSlotDrawable();
        this.title = Component.translatable("jei.mushokucraft.creation_circle");
    }

    @Override
    public RecipeType<CreationCircleRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CreationCircleRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : recipe.getRequiredIngredients().entrySet()) {
            ingredients.add(new ItemStack(entry.getKey(), entry.getValue()));
        }

        int[][] coords = {{10, 8}, {32, 8}, {10, 28}, {32, 28}};
        for (int i = 0; i < ingredients.size() && i < coords.length; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, coords[i][0], coords[i][1])
                    .addItemStack(ingredients.get(i))
                    .setBackground(this.slotDrawable, -1, -1);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 18)
                .addItemStack(recipe.createOutput())
                .setBackground(this.slotDrawable, -1, -1);
    }

    @Override
    public void draw(CreationCircleRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "➔", 65, 22, 0x888888, false);

        Component manaText = Component.translatable("jei.mushokucraft.creation_circle.mana", (int) recipe.getRequiredMana());
        guiGraphics.drawString(font, manaText, 10, 50, 0x33AAFF, false);
    }
}
