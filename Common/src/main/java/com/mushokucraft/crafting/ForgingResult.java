package com.mushokucraft.crafting;

import net.minecraft.world.item.ItemStack;

/**
 * Represents the result of a forging session on the Magic Anvil.
 * Quality is determined by the player's performance in the hammer mini-game.
 * <p>
 * Quality affects:
 * <ul>
 *   <li>Weapon durability (higher quality → more durability → fewer repairs)</li>
 *   <li>Weapon stats (damage, speed bonuses)</li>
 * </ul>
 */
public class ForgingResult {
    private final ItemStack blade;
    private final ItemStack hilt;
    private final ItemStack monsterCore;
    private final float quality; // 0.0 (poor) to 1.0 (perfect)
    private final String weaponName;
    private final String smithName;

    public ForgingResult(ItemStack blade, ItemStack hilt, ItemStack monsterCore,
                         float quality, String weaponName, String smithName) {
        this.blade = blade;
        this.hilt = hilt;
        this.monsterCore = monsterCore;
        this.quality = quality;
        this.weaponName = weaponName;
        this.smithName = smithName;
    }

    public ItemStack getBlade() { return blade; }
    public ItemStack getHilt() { return hilt; }
    public ItemStack getMonsterCore() { return monsterCore; }
    public float getQuality() { return quality; }
    public String getWeaponName() { return weaponName; }
    public String getSmithName() { return smithName; }

    /**
     * Calculate durability multiplier based on forging quality.
     * Perfect forge (1.0) → 1.5x base durability.
     * Poor forge (0.0) → 0.7x base durability.
     */
    public float getDurabilityMultiplier() {
        return 0.7f + (quality * 0.8f);
    }

    /**
     * Calculate stat bonus from quality.
     * Perfect forge → +15% damage.
     */
    public float getDamageBonus() {
        return quality * 0.15f;
    }
}





