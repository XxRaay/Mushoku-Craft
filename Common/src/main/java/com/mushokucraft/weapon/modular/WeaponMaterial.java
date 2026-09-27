package com.mushokucraft.weapon.modular;

import net.minecraft.network.chat.Component;

public record WeaponMaterial(
        String id,
        String translationKey,
        int color,
        float damageBonus,
        float speedModifier,
        int durability,
        float reachBonus,
        String perkTranslationKey
) {
    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public Component getPerkDescription() {
        return Component.translatable(perkTranslationKey);
    }
}
