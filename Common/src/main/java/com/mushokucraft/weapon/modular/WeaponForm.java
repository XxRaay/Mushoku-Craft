package com.mushokucraft.weapon.modular;

import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum WeaponForm {
    SWORD("sword", "item.mushokucraft.modular_sword", 3.0f, -2.4f, 0.0f, 1.0f, "weapon.form.mushokucraft.sword.perk"),
    GREATSWORD("greatsword", "item.mushokucraft.modular_greatsword", 7.0f, -3.1f, 0.8f, 1.5f, "weapon.form.mushokucraft.greatsword.perk"),
    SCYTHE("scythe", "item.mushokucraft.modular_scythe", 5.0f, -2.8f, 0.6f, 1.2f, "weapon.form.mushokucraft.scythe.perk"),
    DAGGER("dagger", "item.mushokucraft.modular_dagger", 1.5f, -1.2f, -0.4f, 0.7f, "weapon.form.mushokucraft.dagger.perk"),
    KATANA("katana", "item.mushokucraft.modular_katana", 4.0f, -2.0f, 0.2f, 1.1f, "weapon.form.mushokucraft.katana.perk"),
    RAPIER("rapier", "item.mushokucraft.modular_rapier", 2.5f, -1.6f, 0.4f, 0.9f, "weapon.form.mushokucraft.rapier.perk");

    private final String id;
    private final String translationKey;
    private final float baseDamage;
    private final float baseSpeed;
    private final float reachBonus;
    private final float durabilityMultiplier;
    private final String perkTranslationKey;

    WeaponForm(String id, String translationKey, float baseDamage, float baseSpeed, float reachBonus, float durabilityMultiplier, String perkTranslationKey) {
        this.id = id;
        this.translationKey = translationKey;
        this.baseDamage = baseDamage;
        this.baseSpeed = baseSpeed;
        this.reachBonus = reachBonus;
        this.durabilityMultiplier = durabilityMultiplier;
        this.perkTranslationKey = perkTranslationKey;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public Component getShortName() {
        return Component.translatable("weapon.form.mushokucraft." + id + ".short");
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public float getReachBonus() {
        return reachBonus;
    }

    public float getDurabilityMultiplier() {
        return durabilityMultiplier;
    }

    public String getPerkTranslationKey() {
        return perkTranslationKey;
    }

    public Component getPerkDescription() {
        return Component.translatable(perkTranslationKey);
    }

    public WeaponForm next() {
        WeaponForm[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }

    public WeaponForm previous() {
        WeaponForm[] vals = values();
        return vals[(this.ordinal() - 1 + vals.length) % vals.length];
    }

    public static WeaponForm byId(String id) {
        if (id == null) return SWORD;
        for (WeaponForm form : values()) {
            if (form.id.equalsIgnoreCase(id)) {
                return form;
            }
        }
        return SWORD;
    }
}
