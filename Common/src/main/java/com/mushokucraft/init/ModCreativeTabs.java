package com.mushokucraft.init;

import com.mushokucraft.init.ModItems;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import dev.architectury.registry.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create("mushokucraft", Registries.CREATIVE_MODE_TAB);
    public static final Supplier<CreativeModeTab> MUSHOKU_TAB = TABS.register("mushoku_tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title((Component)Component.translatable((String)"itemGroup.mushokucraft")).icon(() -> Items.DIAMOND_SWORD.getDefaultInstance()).displayItems((params, output) -> {
        output.accept((ItemLike)ModItems.WATER_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.FIRE_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.EARTH_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.WIND_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.SWORD_GOD_SCROLL.get());
        output.accept((ItemLike)ModItems.SABERTOOTH_WOLF_SPAWN_EGG.get());
        output.accept((ItemLike)ModItems.HUNTING_KNIFE.get());
        output.accept((ItemLike)ModItems.MAGE_MEAT.get());
        output.accept((ItemLike)ModItems.SABERTOOTH_LEATHER.get());
        output.accept((ItemLike)ModItems.MODULAR_ANVIL.get());
        output.accept((ItemLike)ModItems.BLANK_CANVAS.get());
        output.accept((ItemLike)ModItems.INSCRIBED_MANUSCRIPT.get());
        // Ancient Manuscripts with specific circle blueprints
        for (com.mushokucraft.magic.circle.MagicCircleType circleType : com.mushokucraft.magic.circle.MagicCircleRegistry.getAll()) {
            output.accept(com.mushokucraft.item.AncientManuscriptItem.createForType(circleType.getId()));
        }
        output.accept((ItemLike)ModItems.MAGIC_CIRCLE.get());
        output.accept((ItemLike)ModItems.SMALL_MANA_CRYSTAL.get());
        output.accept((ItemLike)ModItems.MEDIUM_MANA_CRYSTAL.get());
        output.accept((ItemLike)ModItems.LARGE_MANA_CRYSTAL.get());
    }).build());

    public static void register() {
        TABS.register();
    }
}
