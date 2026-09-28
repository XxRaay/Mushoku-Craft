package com.mushokucraft.magic.circle;

import com.mushokucraft.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreationCircleRecipes {

    private static final List<CreationCircleRecipe> RECIPES = new ArrayList<>();

    static {
        // --- 1. Eye of Laplace (Unique Mythic Artifact) ---
        // Primary (Diamond Block)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "eye_of_laplace"),
                () -> new ItemStack(ModItems.EYE_OF_LAPLACE.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHER_STAR, 1,
                        Items.ENDER_EYE, 1,
                        Items.DIAMOND_BLOCK, 1
                ),
                3000.0f,
                1,
                Component.translatable("item.mushokucraft.eye_of_laplace")
        ));
        // Alternate (4 Diamonds)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "eye_of_laplace_diamonds"),
                () -> new ItemStack(ModItems.EYE_OF_LAPLACE.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHER_STAR, 1,
                        Items.ENDER_EYE, 1,
                        Items.DIAMOND, 4
                ),
                3000.0f,
                1,
                Component.translatable("item.mushokucraft.eye_of_laplace")
        ));

        // --- 2. Archmage's Heart (Unique Mythic Artifact) ---
        // Primary (Netherite Ingot)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "archmage_heart"),
                () -> new ItemStack(ModItems.ARCHMAGE_HEART.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 2,
                        Items.NETHERITE_INGOT, 1,
                        Items.TOTEM_OF_UNDYING, 1,
                        Items.GHAST_TEAR, 1
                ),
                4000.0f,
                1,
                Component.translatable("item.mushokucraft.archmage_heart")
        ));
        // Alternate (4 Netherite Scraps)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "archmage_heart_scrap"),
                () -> new ItemStack(ModItems.ARCHMAGE_HEART.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 2,
                        Items.NETHERITE_SCRAP, 4,
                        Items.TOTEM_OF_UNDYING, 1,
                        Items.GHAST_TEAR, 1
                ),
                4000.0f,
                1,
                Component.translatable("item.mushokucraft.archmage_heart")
        ));

        // --- 3. Sword God's Belt of Indomitability ---
        // Primary (Iron Block)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "sword_god_belt"),
                () -> new ItemStack(ModItems.SWORD_GOD_BELT.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHERITE_INGOT, 1,
                        ModItems.SABERTOOTH_LEATHER.get(), 2,
                        Items.IRON_BLOCK, 1
                ),
                2500.0f,
                1,
                Component.translatable("item.mushokucraft.sword_god_belt")
        ));
        // Alternate (4 Iron Ingots)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "sword_god_belt_ingots"),
                () -> new ItemStack(ModItems.SWORD_GOD_BELT.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHERITE_INGOT, 1,
                        ModItems.SABERTOOTH_LEATHER.get(), 2,
                        Items.IRON_INGOT, 4
                ),
                2500.0f,
                1,
                Component.translatable("item.mushokucraft.sword_god_belt")
        ));
        // Alternate (4 Netherite Scraps)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "sword_god_belt_scraps"),
                () -> new ItemStack(ModItems.SWORD_GOD_BELT.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHERITE_SCRAP, 4,
                        ModItems.SABERTOOTH_LEATHER.get(), 2,
                        Items.IRON_BLOCK, 1
                ),
                2500.0f,
                1,
                Component.translatable("item.mushokucraft.sword_god_belt")
        ));

        // --- 4. Ring of the Eternal Tempest ---
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "ring_of_eternal_tempest"),
                () -> new ItemStack(ModItems.RING_OF_ETERNAL_TEMPEST.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.HEART_OF_THE_SEA, 1,
                        Items.PRISMARINE_SHARD, 1,
                        Items.FEATHER, 1
                ),
                2000.0f,
                1,
                Component.translatable("item.mushokucraft.ring_of_eternal_tempest")
        ));

        // --- 5. Volcanic Sovereign Amulet ---
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "volcanic_sovereign_amulet"),
                () -> new ItemStack(ModItems.VOLCANIC_SOVEREIGN_AMULET.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.BLAZE_ROD, 2,
                        Items.MAGMA_BLOCK, 1,
                        Items.FIRE_CHARGE, 1
                ),
                2000.0f,
                1,
                Component.translatable("item.mushokucraft.volcanic_sovereign_amulet")
        ));

        // --- 6. Titan Geomancer Bracelet ---
        // Primary (Netherite Scrap)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "titan_geomancer_bracelet"),
                () -> new ItemStack(ModItems.TITAN_GEOMANCER_BRACELET.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHERITE_SCRAP, 1,
                        Items.OBSIDIAN, 2,
                        Items.AMETHYST_BLOCK, 1
                ),
                2500.0f,
                1,
                Component.translatable("item.mushokucraft.titan_geomancer_bracelet")
        ));
        // Alternate (Netherite Ingot)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "titan_geomancer_bracelet_ingot"),
                () -> new ItemStack(ModItems.TITAN_GEOMANCER_BRACELET.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.NETHERITE_INGOT, 1,
                        Items.OBSIDIAN, 2,
                        Items.AMETHYST_BLOCK, 1
                ),
                2500.0f,
                1,
                Component.translatable("item.mushokucraft.titan_geomancer_bracelet")
        ));

        // --- 7. Chronos Pocket Watch ---
        // Primary (Gold Block)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "chronos_pocket_watch"),
                () -> new ItemStack(ModItems.CHRONOS_POCKET_WATCH.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.CLOCK, 1,
                        Items.GOLD_BLOCK, 1,
                        Items.PHANTOM_MEMBRANE, 1
                ),
                2200.0f,
                1,
                Component.translatable("item.mushokucraft.chronos_pocket_watch")
        ));
        // Alternate (4 Gold Ingots)
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "chronos_pocket_watch_ingots"),
                () -> new ItemStack(ModItems.CHRONOS_POCKET_WATCH.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 1,
                        Items.CLOCK, 1,
                        Items.GOLD_INGOT, 4,
                        Items.PHANTOM_MEMBRANE, 1
                ),
                2200.0f,
                1,
                Component.translatable("item.mushokucraft.chronos_pocket_watch")
        ));

        // --- 8. Celestial Mana Core (Unique Mythic Artifact) ---
        register(new CreationCircleRecipe(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "celestial_mana_core"),
                () -> new ItemStack(ModItems.CELESTIAL_MANA_CORE.get()),
                Map.of(
                        ModItems.LARGE_MANA_CRYSTAL.get(), 2,
                        Items.NETHER_STAR, 1,
                        Items.ECHO_SHARD, 1,
                        Items.AMETHYST_BLOCK, 1
                ),
                4500.0f,
                1,
                Component.translatable("item.mushokucraft.celestial_mana_core")
        ));
    }

    public static void register(CreationCircleRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static List<CreationCircleRecipe> getAllRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static CreationCircleRecipe findRecipe(List<ItemEntity> items, int circleSize) {
        if (items.isEmpty()) return null;

        Map<Item, Integer> counts = new HashMap<>();
        for (ItemEntity e : items) {
            if (!e.isAlive()) continue;
            counts.put(e.getItem().getItem(), counts.getOrDefault(e.getItem().getItem(), 0) + e.getItem().getCount());
        }

        for (CreationCircleRecipe recipe : RECIPES) {
            if (recipe.matches(counts, circleSize)) {
                return recipe;
            }
        }
        return null;
    }

    public static CreationCircleRecipe findClosestRecipe(List<ItemEntity> items) {
        if (items.isEmpty()) return null;

        Map<Item, Integer> counts = new HashMap<>();
        for (ItemEntity e : items) {
            if (!e.isAlive()) continue;
            counts.put(e.getItem().getItem(), counts.getOrDefault(e.getItem().getItem(), 0) + e.getItem().getCount());
        }

        CreationCircleRecipe bestMatch = null;
        int maxMatchedIngredients = 0;

        for (CreationCircleRecipe recipe : RECIPES) {
            int matched = 0;
            for (Map.Entry<Item, Integer> req : recipe.getRequiredIngredients().entrySet()) {
                if (counts.containsKey(req.getKey())) {
                    matched++;
                }
            }
            if (matched > maxMatchedIngredients) {
                maxMatchedIngredients = matched;
                bestMatch = recipe;
            }
        }

        return bestMatch;
    }

    public static Component getMissingIngredientsDescription(CreationCircleRecipe recipe, List<ItemEntity> items) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemEntity e : items) {
            if (!e.isAlive()) continue;
            counts.put(e.getItem().getItem(), counts.getOrDefault(e.getItem().getItem(), 0) + e.getItem().getCount());
        }

        List<Component> missing = new ArrayList<>();
        for (Map.Entry<Item, Integer> req : recipe.getRequiredIngredients().entrySet()) {
            int have = counts.getOrDefault(req.getKey(), 0);
            int need = req.getValue() - have;
            if (need > 0) {
                Component itemName = new ItemStack(req.getKey()).getHoverName();
                missing.add(Component.empty().append(itemName).append(" x" + need));
            }
        }

        MutableComponent res = Component.empty();
        for (int i = 0; i < missing.size(); i++) {
            if (i > 0) res.append(Component.literal(", "));
            res.append(missing.get(i));
        }
        return res;
    }
}
