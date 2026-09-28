package com.mushokucraft.compat.jei;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.client.gui.ModularAnvilScreen;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.magic.circle.CreationCircleRecipes;
import com.mushokucraft.magic.circle.CrystallizationCircleType;
import com.mushokucraft.weapon.modular.ModularWeaponItem;
import com.mushokucraft.weapon.modular.WeaponCoreRegistry;
import com.mushokucraft.weapon.modular.WeaponForm;
import com.mushokucraft.weapon.modular.WeaponMaterialRegistry;
import com.mushokucraft.weapon.modular.WeaponQuality;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class MushokuJeiPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CreationCircleCategory(helper));
        registration.addRecipeCategories(new CrystallizationCategory(helper));
        registration.addRecipeCategories(new ModularAnvilCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 1. Creation Circle recipes
        registration.addRecipes(CreationCircleCategory.RECIPE_TYPE, CreationCircleRecipes.getAllRecipes());

        // 2. Crystallization Circle recipes
        registration.addRecipes(CrystallizationCategory.RECIPE_TYPE, CrystallizationCircleType.getAllRecipes());

        // 3. Modular Anvil recipes
        List<ModularAnvilRecipe> anvilRecipes = createAnvilRecipes();
        registration.addRecipes(ModularAnvilCategory.RECIPE_TYPE, anvilRecipes);

        // 4. Ingredient Info descriptions
        registerIngredientInfo(registration);
    }

    private List<ModularAnvilRecipe> createAnvilRecipes() {
        List<ItemStack> blades = List.of(
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.NETHERITE_INGOT),
                new ItemStack(Items.LAPIS_LAZULI),
                new ItemStack(Items.REDSTONE),
                new ItemStack(Items.EMERALD),
                new ItemStack(Items.AMETHYST_SHARD),
                new ItemStack(Items.OBSIDIAN),
                new ItemStack(Items.FLINT),
                new ItemStack(Items.BONE)
        );

        List<ItemStack> guards = List.of(
                new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.COPPER_INGOT),
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.NETHERITE_INGOT),
                new ItemStack(Items.LAPIS_LAZULI),
                new ItemStack(Items.REDSTONE),
                new ItemStack(Items.EMERALD)
        );

        List<ItemStack> handles = List.of(
                new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.SPRUCE_PLANKS),
                new ItemStack(Items.BIRCH_PLANKS),
                new ItemStack(Items.JUNGLE_PLANKS),
                new ItemStack(Items.ACACIA_PLANKS),
                new ItemStack(Items.DARK_OAK_PLANKS),
                new ItemStack(Items.MANGROVE_PLANKS),
                new ItemStack(Items.CHERRY_PLANKS),
                new ItemStack(Items.BAMBOO_PLANKS),
                new ItemStack(Items.CRIMSON_PLANKS),
                new ItemStack(Items.WARPED_PLANKS)
        );

        List<ItemStack> cores = List.of(
                new ItemStack(ModItems.SABERTOOTH_CORE.get()),
                ItemStack.EMPTY
        );

        float manaCost = MushokuConfig.MODULAR_ANVIL_BASE_MANA_COST != null ?
                MushokuConfig.MODULAR_ANVIL_BASE_MANA_COST.get().floatValue() : 50.0f;

        List<ModularAnvilRecipe> recipes = new ArrayList<>();
        for (WeaponForm form : WeaponForm.values()) {
            ItemStack preview = ModularWeaponItem.createWeapon(
                    form,
                    WeaponMaterialRegistry.IRON,
                    WeaponMaterialRegistry.IRON,
                    WeaponMaterialRegistry.OAK,
                    WeaponCoreRegistry.SABERTOOTH_WOLF,
                    "Blacksmith",
                    WeaponQuality.MASTERPIECE,
                    0.85f,
                    null
            );
            recipes.add(new ModularAnvilRecipe(form, blades, guards, handles, cores, preview, manaCost));
        }

        return recipes;
    }

    private void registerIngredientInfo(IRecipeRegistration registration) {
        // Magic Books
        registration.addIngredientInfo(ModItems.WATER_MAGIC_BOOK.get(),
                Component.translatable("jei.mushokucraft.info.water_magic_book"));
        registration.addIngredientInfo(ModItems.FIRE_MAGIC_BOOK.get(),
                Component.translatable("jei.mushokucraft.info.fire_magic_book"));
        registration.addIngredientInfo(ModItems.EARTH_MAGIC_BOOK.get(),
                Component.translatable("jei.mushokucraft.info.earth_magic_book"));
        registration.addIngredientInfo(ModItems.WIND_MAGIC_BOOK.get(),
                Component.translatable("jei.mushokucraft.info.wind_magic_book"));

        // Sword God Scroll
        registration.addIngredientInfo(ModItems.SWORD_GOD_SCROLL.get(),
                Component.translatable("jei.mushokucraft.info.sword_god_scroll"));

        // Manuscripts & Canvas
        registration.addIngredientInfo(ModItems.BLANK_CANVAS.get(),
                Component.translatable("jei.mushokucraft.info.blank_canvas"));
        registration.addIngredientInfo(ModItems.INSCRIBED_MANUSCRIPT.get(),
                Component.translatable("jei.mushokucraft.info.inscribed_manuscript"));
        registration.addIngredientInfo(ModItems.ANCIENT_MANUSCRIPT.get(),
                Component.translatable("jei.mushokucraft.info.ancient_manuscript"));

        // Workstations
        registration.addIngredientInfo(ModItems.MODULAR_ANVIL.get(),
                Component.translatable("jei.mushokucraft.info.modular_anvil"));
        registration.addIngredientInfo(ModItems.MAGE_WORKBENCH.get(),
                Component.translatable("jei.mushokucraft.info.mage_workbench"));
        registration.addIngredientInfo(ModItems.MAGIC_CIRCLE.get(),
                Component.translatable("jei.mushokucraft.info.magic_circle"));

        // Monster Hunting & Materials
        registration.addIngredientInfo(ModItems.HUNTING_KNIFE.get(),
                Component.translatable("jei.mushokucraft.info.hunting_knife"));
        registration.addIngredientInfo(ModItems.SABERTOOTH_CORE.get(),
                Component.translatable("jei.mushokucraft.info.sabertooth_core"));
        registration.addIngredientInfo(ModItems.SABERTOOTH_LEATHER.get(),
                Component.translatable("jei.mushokucraft.info.sabertooth_leather"));

        // Mana Crystals
        registration.addIngredientInfo(ModItems.SMALL_MANA_CRYSTAL.get(),
                Component.translatable("jei.mushokucraft.info.mana_crystals"));
        registration.addIngredientInfo(ModItems.MEDIUM_MANA_CRYSTAL.get(),
                Component.translatable("jei.mushokucraft.info.mana_crystals"));
        registration.addIngredientInfo(ModItems.LARGE_MANA_CRYSTAL.get(),
                Component.translatable("jei.mushokucraft.info.mana_crystals"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Workbench
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MAGE_WORKBENCH.get()), RecipeTypes.CRAFTING);

        // Modular Anvil
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MODULAR_ANVIL.get()), ModularAnvilCategory.RECIPE_TYPE);

        // Magic Circles
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MAGIC_CIRCLE.get()), CreationCircleCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MAGIC_CIRCLE.get()), CrystallizationCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BLANK_CANVAS.get()), CreationCircleCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.SMALL_MANA_CRYSTAL.get()), CrystallizationCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ModularAnvilScreen.class, 162, 19, 54, 18, ModularAnvilCategory.RECIPE_TYPE);
    }
}
