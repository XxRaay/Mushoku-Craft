package com.mushokucraft.combat;

import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class SwordCombatHelper {

    public static final TagKey<Item> C_SWORDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "swords"));
    public static final TagKey<Item> C_TOOLS_SWORD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/sword"));
    public static final TagKey<Item> C_TOOLS_SWORDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/swords"));
    public static final TagKey<Item> C_MELEE_WEAPONS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "melee_weapons"));
    public static final TagKey<Item> C_WEAPONS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "weapons"));
    public static final TagKey<Item> MUSHOKU_SWORDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("mushokucraft", "swords"));

    public static boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        if (item instanceof SwordItem) {
            return true;
        }

        // Vanilla sword tag
        if (stack.is(ItemTags.SWORDS)) {
            return true;
        }

        // Cross-platform convention tags (Fabric / NeoForge)
        if (stack.is(C_SWORDS) || stack.is(C_TOOLS_SWORD) || stack.is(C_TOOLS_SWORDS) || stack.is(C_MELEE_WEAPONS) || stack.is(C_WEAPONS)) {
            return true;
        }

        // Mod-specific tag for modpacks / custom datapacks
        if (stack.is(MUSHOKU_SWORDS)) {
            return true;
        }

        // Fallback for custom mod weapons:
        // Non-tool items dealing attack damage
        if (MushokuConfig.ALLOW_ALL_MELEE_WEAPONS_FOR_STANCES.get()) {
            if (!stack.is(ItemTags.AXES) && !stack.is(ItemTags.PICKAXES) && !stack.is(ItemTags.SHOVELS) && !stack.is(ItemTags.HOES)) {
                ItemAttributeModifiers attributeModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (attributeModifiers != null) {
                    for (var entry : attributeModifiers.modifiers()) {
                        if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value() && entry.modifier().amount() > 0) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean isSword(Item item) {
        if (item == null) {
            return false;
        }
        return isSword(item.getDefaultInstance());
    }
}
