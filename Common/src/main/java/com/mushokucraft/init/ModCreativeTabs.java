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

    public static void register() {
        TABS.register();
    }
}
