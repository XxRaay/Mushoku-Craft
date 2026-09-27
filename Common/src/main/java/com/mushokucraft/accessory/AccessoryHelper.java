package com.mushokucraft.accessory;

import com.mushokucraft.magic.MagicSchool;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AccessoryHelper {

    @FunctionalInterface
    public interface AccessoryProvider {
        List<ItemStack> getAccessories(LivingEntity entity);
    }

    private static AccessoryProvider provider = null;

    public static void registerProvider(AccessoryProvider customProvider) {
        provider = customProvider;
    }

    public static List<ItemStack> getEquippedAccessories(LivingEntity entity) {
        List<ItemStack> list = new ArrayList<>();
        if (entity == null) return list;

        if (provider != null) {
            try {
                list.addAll(provider.getAccessories(entity));
            } catch (Throwable ignored) {}
        }

        // Fallback for environments without Curios or if offhand holds an accessory
        if (list.isEmpty()) {
            if (entity.getOffhandItem().getItem() instanceof AccessoryItem) {
                list.add(entity.getOffhandItem());
            }
        }

        return list;
    }

    public static float getMaxManaBonus(Player player) {
        float total = 0f;
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                total += acc.getMaxManaBonus();
            }
        }
        return total;
    }

    public static float getManaRegenBonus(Player player) {
        float total = 0f;
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                total += acc.getManaRegenBonus();
                // Rain / Water synergy for ring of eternal tempest
                if ("tempest_vortex".equals(acc.getPassiveId()) && (player.isInWaterOrRain() || player.isInWater())) {
                    total += 3.5f; // Double regen in storm/water
                }
            }
        }
        return total;
    }

    public static float getSchoolDamageMultiplier(net.minecraft.world.entity.Entity caster, MagicSchool school) {
        if (!(caster instanceof LivingEntity living)) return 1.0f;
        float bonus = 0.0f;
        for (ItemStack stack : getEquippedAccessories(living)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                bonus += acc.getSchoolDamageBonus(school);
                bonus += acc.getAllSchoolDamageBonus();
            }
        }
        return 1.0f + bonus;
    }

    public static float applyMagicDamageBonus(net.minecraft.world.entity.Entity caster, MagicSchool school, float baseDamage) {
        return baseDamage * getSchoolDamageMultiplier(caster, school);
    }

    public static float getManaCostDiscount(Player player) {
        float discount = 0f;
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                discount += acc.getManaCostDiscount();
            }
        }
        return Math.min(0.75f, discount); // Capped at 75% max discount
    }

    public static float getCastTimeReduction(Player player) {
        float reduction = 0f;
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                reduction += acc.getCastTimeReduction();
            }
        }
        return Math.min(0.60f, reduction); // Capped at 60% max reduction
    }

    public static boolean isFizzleImmune(Player player) {
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                if (acc.isFizzleImmune()) return true;
            }
        }
        return false;
    }

    public static float getToukiDrainDiscount(Player player) {
        float discount = 0f;
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                discount += acc.getToukiDrainDiscount();
            }
        }
        return Math.min(0.80f, discount); // Capped at 80% Touki drain discount
    }

    public static boolean hasLifeline(Player player) {
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                if ("mana_shield".equals(acc.getPassiveId())) return true;
            }
        }
        return false;
    }

    public static boolean hasVolcanicBurn(Player player) {
        for (ItemStack stack : getEquippedAccessories(player)) {
            if (stack.getItem() instanceof AccessoryItem acc) {
                if ("volcanic_burn".equals(acc.getPassiveId())) return true;
            }
        }
        return false;
    }
}
