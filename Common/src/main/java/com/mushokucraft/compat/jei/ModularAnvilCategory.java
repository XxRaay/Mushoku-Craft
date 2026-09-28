package com.mushokucraft.compat.jei;

import com.mushokucraft.init.ModBlocks;
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

public class ModularAnvilCategory implements IRecipeCategory<ModularAnvilRecipe> {
    public static final RecipeType<ModularAnvilRecipe> RECIPE_TYPE =
            RecipeType.create("mushokucraft", "modular_anvil", ModularAnvilRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final Component title;

    public ModularAnvilCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(160, 75);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.MODULAR_ANVIL.get()));
        this.slotDrawable = helper.getSlotDrawable();
        this.title = Component.translatable("jei.mushokucraft.modular_anvil");
    }

    @Override
    public RecipeType<ModularAnvilRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, ModularAnvilRecipe recipe, IFocusGroup focuses) {
        // Blade Slot
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 8)
                .addItemStacks(recipe.bladeMaterials())
                .setBackground(this.slotDrawable, -1, -1);

        // Guard Slot
        builder.addSlot(RecipeIngredientRole.INPUT, 30, 8)
                .addItemStacks(recipe.guardMaterials())
                .setBackground(this.slotDrawable, -1, -1);

        // Handle Slot
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 28)
                .addItemStacks(recipe.handleMaterials())
                .setBackground(this.slotDrawable, -1, -1);

        // Core Slot (optional)
        if (!recipe.cores().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 30, 28)
                    .addItemStacks(recipe.cores())
                    .setBackground(this.slotDrawable, -1, -1);
        }

        // Result Slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 18)
                .addItemStack(recipe.outputSample())
                .setBackground(this.slotDrawable, -1, -1);
    }

    @Override
    public void draw(ModularAnvilRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, "➔", 68, 22, 0x888888, false);

        Component formText = Component.translatable("weapon.form.mushokucraft." + recipe.form().getId() + ".short");
        guiGraphics.drawString(font, formText, 60, 8, 0xFFCC44, false);

        Component manaText = Component.translatable("jei.mushokucraft.modular_anvil.mana", (int) recipe.manaCost());
        guiGraphics.drawString(font, manaText, 10, 52, 0x33AAFF, false);

        Component hintText = Component.translatable("jei.mushokucraft.modular_anvil.hint");
        guiGraphics.drawString(font, hintText, 10, 63, 0x777777, false);
    }
}
