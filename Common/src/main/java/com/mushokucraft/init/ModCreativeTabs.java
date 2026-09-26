package com.mushokucraft.init;

import com.mushokucraft.item.AncientManuscriptItem;
import com.mushokucraft.item.InscribedManuscriptItem;
import com.mushokucraft.magic.circle.ClientMagicCircleState;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import com.mushokucraft.magic.circle.MagicCirclePatterns;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create("mushokucraft", Registries.CREATIVE_MODE_TAB);

    public static final Supplier<CreativeModeTab> MUSHOKU_TAB = TABS.register("mushoku_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.mushokucraft"))
            .icon(() -> Items.DIAMOND_SWORD.getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(ModItems.WATER_MAGIC_BOOK.get());
                output.accept(ModItems.FIRE_MAGIC_BOOK.get());
                output.accept(ModItems.EARTH_MAGIC_BOOK.get());
                output.accept(ModItems.WIND_MAGIC_BOOK.get());
                output.accept(ModItems.SWORD_GOD_SCROLL.get());
                output.accept(ModItems.SABERTOOTH_WOLF_SPAWN_EGG.get());
                output.accept(ModItems.HUNTING_KNIFE.get());
                output.accept(ModItems.MAGE_MEAT.get());
                output.accept(ModItems.SABERTOOTH_LEATHER.get());
                output.accept(ModItems.MODULAR_ANVIL.get());
                output.accept(ModItems.BLANK_CANVAS.get());
                output.accept(ModItems.MAGIC_CIRCLE.get());
                output.accept(ModItems.SMALL_MANA_CRYSTAL.get());
                output.accept(ModItems.MEDIUM_MANA_CRYSTAL.get());
                output.accept(ModItems.LARGE_MANA_CRYSTAL.get());
            }).build());

    public static final Supplier<CreativeModeTab> MAGIC_CIRCLES_TAB = TABS.register("magic_circles_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
            .title(Component.translatable("itemGroup.mushokucraft.magic_circles"))
            .icon(() -> ModItems.INSCRIBED_MANUSCRIPT.get().getDefaultInstance())
            .displayItems((params, output) -> {
                // 1. Basic tools & crafting supplies for magic circles
                output.accept(ModItems.BLANK_CANVAS.get());
                output.accept(ModItems.INSCRIBED_MANUSCRIPT.get());
                output.accept(ModItems.MAGIC_CIRCLE.get());
                output.accept(ModItems.SMALL_MANA_CRYSTAL.get());
                output.accept(ModItems.MEDIUM_MANA_CRYSTAL.get());
                output.accept(ModItems.LARGE_MANA_CRYSTAL.get());

                // 2. All Ancient Manuscripts (Blueprints for studying glyphs)
                for (MagicCircleType circleType : MagicCircleRegistry.getAll()) {
                    output.accept(AncientManuscriptItem.createForType(circleType.getId()));
                }

                // 3. Pre-inscribed 1x1 Manuscripts (Ready drawn scrolls)
                for (MagicCircleType circleType : MagicCircleRegistry.getAll()) {
                    MagicCirclePattern pattern = ClientMagicCircleState.getPatternForType(circleType.getId());
                    if (pattern == null) {
                        pattern = MagicCirclePatterns.getPatternForSeed(0L, circleType.getId());
                    }
                    output.accept(InscribedManuscriptItem.create(pattern, circleType.getId(), 1));
                }

                // 4. Pre-inscribed 3x3 Manuscripts (Ready drawn scrolls for large 3x3 circles & rituals)
                for (MagicCircleType circleType : MagicCircleRegistry.getAll()) {
                    MagicCirclePattern pattern = ClientMagicCircleState.getPatternForType(circleType.getId());
                    if (pattern == null) {
                        pattern = MagicCirclePatterns.getPatternForSeed(0L, circleType.getId());
                    }
                    output.accept(InscribedManuscriptItem.create(pattern, circleType.getId(), 3));
                }
            }).build());

    public static final Supplier<CreativeModeTab> ACCESSORIES_TAB = TABS.register("accessories_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
            .title(Component.translatable("itemGroup.mushokucraft.accessories"))
            .icon(() -> ModItems.EYE_OF_LAPLACE.get().getDefaultInstance())
            .displayItems((params, output) -> {
                // Tier 1 Accessories
                output.accept(ModItems.APPRENTICE_RING.get());
                output.accept(ModItems.MANA_COPPER_RING.get());
                output.accept(ModItems.MANA_SILVER_RING.get());
                output.accept(ModItems.PYROMANCER_SPARK_CHARM.get());
                output.accept(ModItems.AQUAMANCER_DROP_PENDANT.get());
                output.accept(ModItems.ZEPHYR_FEATHER_CHARM.get());
                output.accept(ModItems.GEOMANCER_STONE_RING.get());
                output.accept(ModItems.SWORDSMAN_LEATHER_BELT.get());

                // Tier 2 Accessories
                output.accept(ModItems.MAGE_KNIGHT_AMULET.get());
                output.accept(ModItems.SORCERER_BAND.get());
                output.accept(ModItems.INFERNO_RING.get());
                output.accept(ModItems.FROST_NECKLACE.get());
                output.accept(ModItems.TEMPEST_SASH.get());
                output.accept(ModItems.TERRA_BUCKLER_CHARM.get());
                output.accept(ModItems.DUELIST_RING.get());
                output.accept(ModItems.VITALITY_MANA_RING.get());

                // Tier 3 Unique Mythic Curios (Forged on Magic Creation Circle)
                output.accept(ModItems.EYE_OF_LAPLACE.get());
                output.accept(ModItems.ARCHMAGE_HEART.get());
                output.accept(ModItems.SWORD_GOD_BELT.get());
                output.accept(ModItems.RING_OF_ETERNAL_TEMPEST.get());
                output.accept(ModItems.VOLCANIC_SOVEREIGN_AMULET.get());
                output.accept(ModItems.TITAN_GEOMANCER_BRACELET.get());
                output.accept(ModItems.CHRONOS_POCKET_WATCH.get());
                output.accept(ModItems.CELESTIAL_MANA_CORE.get());

                // Ritual catalysts and Magic Creation blueprints
                output.accept(ModItems.SMALL_MANA_CRYSTAL.get());
                output.accept(ModItems.MEDIUM_MANA_CRYSTAL.get());
                output.accept(ModItems.LARGE_MANA_CRYSTAL.get());
                output.accept(AncientManuscriptItem.createForType(com.mushokucraft.magic.circle.MagicCreationCircleType.ID));
                MagicCirclePattern creationPattern = ClientMagicCircleState.getPatternForType(com.mushokucraft.magic.circle.MagicCreationCircleType.ID);
                if (creationPattern == null) {
                    creationPattern = MagicCirclePatterns.getPatternForSeed(0L, com.mushokucraft.magic.circle.MagicCreationCircleType.ID);
                }
                output.accept(InscribedManuscriptItem.create(creationPattern, com.mushokucraft.magic.circle.MagicCreationCircleType.ID, 1));
                output.accept(InscribedManuscriptItem.create(creationPattern, com.mushokucraft.magic.circle.MagicCreationCircleType.ID, 3));
            }).build());

    public static void register() {
        TABS.register();
    }
}
