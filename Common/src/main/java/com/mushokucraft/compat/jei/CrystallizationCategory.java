package com.mushokucraft.compat.jei;

import com.mushokucraft.init.ModItems;
import com.mushokucraft.magic.circle.CrystallizationCircleType;
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
import net.minecraft.world.item.ItemStack;

public class CrystallizationCategory implements IRecipeCategory<CrystallizationCircleType.CrystallizationRecipe> {
    public static final RecipeType<CrystallizationCircleType.CrystallizationRecipe> RECIPE_TYPE =
            RecipeType.create("mushokucraft", "crystallization", CrystallizationCircleType.CrystallizationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final Component title;

    public CrystallizationCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(140, 50);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.SMALL_MANA_CRYSTAL.get()));
        this.slotDrawable = helper.getSlotDrawable();
        this.title = Component.translatable("jei.mushokucraft.crystallization");
    }

    @Override
    public RecipeType<CrystallizationCircleType.CrystallizationRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrystallizationCircleType.CrystallizationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 12)
                .addItemStack(new ItemStack(recipe.input()))
                .setBackground(this.slotDrawable, -1, -1);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 12)
                .addItemStack(new ItemStack(recipe.output()))
                .setBackground(this.slotDrawable, -1, -1);
    }

    @Override
    public void draw(CrystallizationCircleType.CrystallizationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "➔", 50, 16, 0x888888, false);

        Component manaText = Component.translatable("jei.mushokucraft.crystallization.mana", (int) recipe.manaCost());
        guiGraphics.drawString(font, manaText, 15, 36, 0x33AAFF, false);
    }
}
