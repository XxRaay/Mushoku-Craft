/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package com.mushokucraft.crafting;

import net.minecraft.world.item.ItemStack;

public class ForgingResult {
    private final ItemStack blade;
    private final ItemStack hilt;
    private final ItemStack monsterCore;
    private final float quality;
    private final String weaponName;
    private final String smithName;

    public ForgingResult(ItemStack blade, ItemStack hilt, ItemStack monsterCore, float quality, String weaponName, String smithName) {
        this.blade = blade;
        this.hilt = hilt;
        this.monsterCore = monsterCore;
        this.quality = quality;
        this.weaponName = weaponName;
        this.smithName = smithName;
    }

    public ItemStack getBlade() {
        return this.blade;
    }

    public ItemStack getHilt() {
        return this.hilt;
    }

    public ItemStack getMonsterCore() {
        return this.monsterCore;
    }

    public float getQuality() {
        return this.quality;
    }

    public String getWeaponName() {
        return this.weaponName;
    }

    public String getSmithName() {
        return this.smithName;
    }

    public float getDurabilityMultiplier() {
        return 0.7f + this.quality * 0.8f;
    }

    public float getDamageBonus() {
        return this.quality * 0.15f;
    }
}

