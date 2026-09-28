package com.mushokucraft.init;

import com.mushokucraft.init.ModEntities;
import com.mushokucraft.item.MagicBookItem;
import com.mushokucraft.item.SwordGodScrollItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create("mushokucraft", Registries.ITEM);
    public static final Supplier<Item> WATER_MAGIC_BOOK = ITEMS.register("water_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "water", java.util.List.of("waterball", "water_slice", "icicle_break", "cumulonimbus")));
    public static final Supplier<Item> FIRE_MAGIC_BOOK = ITEMS.register("fire_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "fire", java.util.List.of("fireball", "flame_pillar", "exodus_flame", "flashover")));
    public static final Supplier<Item> EARTH_MAGIC_BOOK = ITEMS.register("earth_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "earth", java.util.List.of("rockbullet", "earth_lance", "stone_pillar", "sandstorm")));
    public static final Supplier<Item> WIND_MAGIC_BOOK = ITEMS.register("wind_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "wind", java.util.List.of("airstrike", "air_cushion", "tornado", "typhoon")));
    public static final Supplier<Item> SABERTOOTH_WOLF_SPAWN_EGG = ITEMS.register("sabertooth_wolf_spawn_egg", () -> new ArchitecturySpawnEggItem(ModEntities.SABERTOOTH_WOLF, 9139029, 4864810, new Item.Properties()));
    public static final Supplier<Item> SWORD_GOD_SCROLL = ITEMS.register("sword_god_scroll", () -> new SwordGodScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final Supplier<Item> HUNTING_KNIFE = ITEMS.register("hunting_knife", () -> new com.mushokucraft.item.HuntingKnifeItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> MAGE_MEAT = ITEMS.register("mage_meat", () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build())));
    public static final Supplier<Item> SABERTOOTH_LEATHER = ITEMS.register("sabertooth_leather", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SABERTOOTH_CORE = ITEMS.register("sabertooth_core", () -> new Item(new Item.Properties().rarity(Rarity.RARE).stacksTo(16)));

    public static final Supplier<Item> MODULAR_ANVIL = ITEMS.register("modular_anvil", () -> new net.minecraft.world.item.BlockItem(ModBlocks.MODULAR_ANVIL.get(), new Item.Properties()));

    public static final Supplier<Item> MODULAR_SWORD = ITEMS.register("modular_sword", () -> new com.mushokucraft.weapon.modular.ModularWeaponItem(com.mushokucraft.weapon.modular.WeaponForm.SWORD, new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MODULAR_GREATSWORD = ITEMS.register("modular_greatsword", () -> new com.mushokucraft.weapon.modular.ModularWeaponItem(com.mushokucraft.weapon.modular.WeaponForm.GREATSWORD, new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MODULAR_SCYTHE = ITEMS.register("modular_scythe", () -> new com.mushokucraft.weapon.modular.ModularWeaponItem(com.mushokucraft.weapon.modular.WeaponForm.SCYTHE, new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MODULAR_DAGGER = ITEMS.register("modular_dagger", () -> new com.mushokucraft.weapon.modular.ModularWeaponItem(com.mushokucraft.weapon.modular.WeaponForm.DAGGER, new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MODULAR_KATANA = ITEMS.register("modular_katana", () -> new com.mushokucraft.weapon.modular.ModularWeaponItem(com.mushokucraft.weapon.modular.WeaponForm.KATANA, new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MODULAR_RAPIER = ITEMS.register("modular_rapier", () -> new com.mushokucraft.weapon.modular.ModularWeaponItem(com.mushokucraft.weapon.modular.WeaponForm.RAPIER, new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> BLANK_CANVAS = ITEMS.register("blank_canvas", () -> new com.mushokucraft.item.BlankCanvasItem(new Item.Properties().stacksTo(64)));
    public static final Supplier<Item> INSCRIBED_MANUSCRIPT = ITEMS.register("inscribed_manuscript", () -> new com.mushokucraft.item.InscribedManuscriptItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ANCIENT_MANUSCRIPT = ITEMS.register("ancient_manuscript", () -> new com.mushokucraft.item.AncientManuscriptItem(new Item.Properties()));
    public static final Supplier<Item> MAGIC_CIRCLE = ITEMS.register("magic_circle", () -> new net.minecraft.world.item.BlockItem(ModBlocks.MAGIC_CIRCLE.get(), new Item.Properties()));
    public static final Supplier<Item> MAGE_WORKBENCH = ITEMS.register("mage_workbench", () -> new net.minecraft.world.item.BlockItem(ModBlocks.MAGE_WORKBENCH.get(), new Item.Properties()));

    public static final Supplier<Item> SMALL_MANA_CRYSTAL = ITEMS.register("small_mana_crystal", () -> new com.mushokucraft.item.ManaCrystalItem(new Item.Properties().stacksTo(16), 50.0f));
    public static final Supplier<Item> MEDIUM_MANA_CRYSTAL = ITEMS.register("medium_mana_crystal", () -> new com.mushokucraft.item.ManaCrystalItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 150.0f));
    public static final Supplier<Item> LARGE_MANA_CRYSTAL = ITEMS.register("large_mana_crystal", () -> new com.mushokucraft.item.ManaCrystalItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), 400.0f));

    private static Supplier<Item> registerAccessory(String id, Supplier<Item> supplier) {
        return ITEMS.register(id, supplier);
    }

    // --- Tier 1 Accessories (Beginner / Adventurer) ---
    public static final Supplier<Item> APPRENTICE_RING = registerAccessory("apprentice_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .maxMana(50.0f).manaRegen(0.5f)));

    public static final Supplier<Item> MANA_COPPER_RING = registerAccessory("mana_copper_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .maxMana(35.0f).manaRegen(0.3f)));

    public static final Supplier<Item> MANA_SILVER_RING = registerAccessory("mana_silver_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .maxMana(75.0f).manaRegen(0.8f)));

    public static final Supplier<Item> PYROMANCER_SPARK_CHARM = registerAccessory("pyromancer_spark_charm",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.CHARM)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.FIRE, 0.15f)));

    public static final Supplier<Item> AQUAMANCER_DROP_PENDANT = registerAccessory("aquamancer_drop_pendant",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.NECKLACE)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.WATER, 0.15f).manaRegen(0.5f)));

    public static final Supplier<Item> ZEPHYR_FEATHER_CHARM = registerAccessory("zephyr_feather_charm",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.CHARM)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.WIND, 0.15f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.10)));

    public static final Supplier<Item> GEOMANCER_STONE_RING = registerAccessory("geomancer_stone_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.EARTH, 0.15f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 1.0)));

    public static final Supplier<Item> SWORDSMAN_LEATHER_BELT = registerAccessory("swordsman_leather_belt",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.BELT)
                            .toukiDiscount(0.15f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 1.0)));

    // --- Tier 2 Accessories (Intermediate / Mage Knight) ---
    public static final Supplier<Item> MAGE_KNIGHT_AMULET = registerAccessory("mage_knight_amulet",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.NECKLACE)
                            .maxMana(150.0f).manaRegen(1.5f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 1.0)));

    public static final Supplier<Item> SORCERER_BAND = registerAccessory("sorcerer_band",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .maxMana(200.0f).manaDiscount(0.10f)));

    public static final Supplier<Item> INFERNO_RING = registerAccessory("inferno_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.FIRE, 0.25f).maxMana(100.0f)));

    public static final Supplier<Item> FROST_NECKLACE = registerAccessory("frost_necklace",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.NECKLACE)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.WATER, 0.25f).manaRegen(1.2f)));

    public static final Supplier<Item> TEMPEST_SASH = registerAccessory("tempest_sash",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.BELT)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.WIND, 0.25f).manaDiscount(0.15f)));

    public static final Supplier<Item> TERRA_BUCKLER_CHARM = registerAccessory("terra_buckler_charm",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.CHARM)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.EARTH, 0.25f)
                            .maxMana(100.0f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 2.0)));

    public static final Supplier<Item> DUELIST_RING = registerAccessory("duelist_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .toukiDiscount(0.25f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, 0.10)));

    public static final Supplier<Item> VITALITY_MANA_RING = registerAccessory("vitality_mana_ring",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .maxMana(150.0f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 4.0)));

    // --- Tier 3 Unique & Mythic Accessories (Forged on "Magic Creation" Circle) ---
    public static final Supplier<Item> EYE_OF_LAPLACE = registerAccessory("eye_of_laplace",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.CHARM)
                            .maxMana(500.0f)
                            .allSchoolDamage(0.35f)
                            .manaDiscount(0.25f)
                            .passive("laplace_sight")));

    public static final Supplier<Item> ARCHMAGE_HEART = registerAccessory("archmage_heart",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.NECKLACE)
                            .maxMana(1000.0f)
                            .manaRegen(6.0f)
                            .passive("mana_shield")));

    public static final Supplier<Item> SWORD_GOD_BELT = registerAccessory("sword_god_belt",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.BELT)
                            .toukiDiscount(0.50f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 4.0)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.20)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.30)));

    public static final Supplier<Item> RING_OF_ETERNAL_TEMPEST = registerAccessory("ring_of_eternal_tempest",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.RING)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.WATER, 0.35f)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.WIND, 0.35f)
                            .manaRegen(3.5f)
                            .passive("tempest_vortex")));

    public static final Supplier<Item> VOLCANIC_SOVEREIGN_AMULET = registerAccessory("volcanic_sovereign_amulet",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.NECKLACE)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.FIRE, 0.40f)
                            .passive("volcanic_burn")));

    public static final Supplier<Item> TITAN_GEOMANCER_BRACELET = registerAccessory("titan_geomancer_bracelet",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.HANDS)
                            .schoolDamage(com.mushokucraft.magic.MagicSchool.EARTH, 0.40f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 4.0)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS, 2.0)
                            .passive("titan_bastion")));

    public static final Supplier<Item> CHRONOS_POCKET_WATCH = registerAccessory("chronos_pocket_watch",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.CHARM)
                            .castReduction(0.30f)
                            .fizzleImmune()
                            .manaRegen(2.5f)
                            .maxMana(250.0f)));

    public static final Supplier<Item> CELESTIAL_MANA_CORE = registerAccessory("celestial_mana_core",
            () -> new com.mushokucraft.accessory.AccessoryItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC),
                    new com.mushokucraft.accessory.AccessoryItem.Builder(com.mushokucraft.accessory.AccessoryType.BODY)
                            .maxMana(800.0f)
                            .manaRegen(5.0f)
                            .allSchoolDamage(0.25f)
                            .manaDiscount(0.20f)
                            .attribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 4.0)));

    public static void register() {
        ITEMS.register();
    }
}


