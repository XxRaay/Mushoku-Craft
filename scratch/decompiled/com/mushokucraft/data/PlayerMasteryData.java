/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.common.util.INBTSerializable
 */
package com.mushokucraft.data;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.MagicSchool;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerMasteryData
implements INBTSerializable<CompoundTag> {
    private float mana;
    private float maxMana;
    private float manaRegenRate;
    private float waterMastery;
    private float fireMastery = 0.0f;
    private float earthMastery = 0.0f;
    private float windMastery = 0.0f;
    private boolean unlockedLongswordOfSilence = false;
    private final Map<ResourceLocation, Float> spellMasteryMap = new HashMap<ResourceLocation, Float>();
    private float baseSwordMastery;
    private float swordGodMastery;
    private float waterGodMastery;
    private float northGodMastery;
    private SwordStyle activeStance = null;
    public int parryTicks = 0;
    private boolean unlockedSwordGod = false;
    private boolean unlockedWaterGod = false;
    private boolean unlockedNorthGod = false;
    private boolean isToukiActive = false;
    private int shieldBlocks = 0;
    private final Map<ResourceLocation, Long> itemCooldownEnds = new HashMap<ResourceLocation, Long>();

    public PlayerMasteryData() {
        this.mana = ((Double)MushokuConfig.DEFAULT_MANA.get()).floatValue();
        this.maxMana = ((Double)MushokuConfig.DEFAULT_MAX_MANA.get()).floatValue();
        this.manaRegenRate = ((Double)MushokuConfig.DEFAULT_MANA_REGEN_RATE.get()).floatValue();
    }

    public float getMana() {
        return this.mana;
    }

    public float getMaxMana() {
        return this.maxMana;
    }

    public boolean consumeMana(float amount) {
        if (this.mana >= amount) {
            this.mana -= amount;
            return true;
        }
        return false;
    }

    public void regenMana(float amount) {
        this.mana = Math.min(this.mana + amount, this.maxMana);
    }

    public void fullRestore() {
        this.mana = this.maxMana;
    }

    public float getSchoolMastery(MagicSchool school) {
        return switch (school) {
            default -> throw new MatchException(null, null);
            case MagicSchool.WATER -> this.waterMastery;
            case MagicSchool.FIRE -> this.fireMastery;
            case MagicSchool.EARTH -> this.earthMastery;
            case MagicSchool.WIND -> this.windMastery;
            case MagicSchool.SWORD_ARTS -> this.getStanceMastery(SwordStyle.SWORD_GOD);
        };
    }

    public void addSchoolMastery(MagicSchool school, float amount) {
        switch (school) {
            case WATER: {
                this.waterMastery = Math.min(1.0f, this.waterMastery + amount);
                break;
            }
            case FIRE: {
                this.fireMastery = Math.min(1.0f, this.fireMastery + amount);
                break;
            }
            case EARTH: {
                this.earthMastery = Math.min(1.0f, this.earthMastery + amount);
                break;
            }
            case WIND: {
                this.windMastery = Math.min(1.0f, this.windMastery + amount);
                break;
            }
        }
    }

    public void setSchoolMastery(MagicSchool school, float amount) {
        switch (school) {
            case WATER: {
                this.waterMastery = Math.clamp(amount, 0.0f, 1.0f);
                break;
            }
            case FIRE: {
                this.fireMastery = Math.clamp(amount, 0.0f, 1.0f);
                break;
            }
            case EARTH: {
                this.earthMastery = Math.clamp(amount, 0.0f, 1.0f);
                break;
            }
            case WIND: {
                this.windMastery = Math.clamp(amount, 0.0f, 1.0f);
                break;
            }
        }
    }

    public float getSpellMastery(ResourceLocation spellId) {
        return this.spellMasteryMap.getOrDefault(spellId, Float.valueOf(0.0f)).floatValue();
    }

    public void addSpellMastery(ResourceLocation spellId, float amount) {
        float current = this.getSpellMastery(spellId);
        this.spellMasteryMap.put(spellId, Float.valueOf(Math.min(1.0f, current + amount)));
    }

    public void setSpellMastery(ResourceLocation spellId, float amount) {
        this.spellMasteryMap.put(spellId, Float.valueOf(Math.clamp(amount, 0.0f, 1.0f)));
    }

    public float getStanceMastery(SwordStyle stance) {
        if (stance == null) {
            return this.baseSwordMastery;
        }
        return switch (stance) {
            default -> throw new MatchException(null, null);
            case SwordStyle.SWORD_GOD -> this.swordGodMastery;
            case SwordStyle.WATER_GOD -> this.waterGodMastery;
            case SwordStyle.NORTH_GOD -> this.northGodMastery;
        };
    }

    public void setSwordGodMastery(float mastery) {
        this.swordGodMastery = Math.clamp(mastery, 0.0f, 1.0f);
    }

    public void setWaterGodMastery(float mastery) {
        this.waterGodMastery = Math.clamp(mastery, 0.0f, 1.0f);
    }

    public void setNorthGodMastery(float mastery) {
        this.northGodMastery = Math.clamp(mastery, 0.0f, 1.0f);
    }

    public SwordStyle getActiveStance() {
        return this.activeStance;
    }

    public void setActiveStance(SwordStyle activeStance) {
        this.activeStance = activeStance;
    }

    public boolean isStyleUnlocked(SwordStyle style) {
        if (style == null) {
            return true;
        }
        return switch (style) {
            default -> throw new MatchException(null, null);
            case SwordStyle.SWORD_GOD -> this.unlockedSwordGod;
            case SwordStyle.WATER_GOD -> this.unlockedWaterGod;
            case SwordStyle.NORTH_GOD -> this.unlockedNorthGod;
        };
    }

    public void unlockStyle(SwordStyle style) {
        if (style == null) {
            return;
        }
        switch (style) {
            case SWORD_GOD: {
                this.unlockedSwordGod = true;
                break;
            }
            case WATER_GOD: {
                this.unlockedWaterGod = true;
                break;
            }
            case NORTH_GOD: {
                this.unlockedNorthGod = true;
            }
        }
    }

    public boolean isToukiActive() {
        return this.isToukiActive;
    }

    public void setToukiActive(boolean active) {
        this.isToukiActive = active;
    }

    public int getShieldBlocks() {
        return this.shieldBlocks;
    }

    public void incrementShieldBlocks() {
        ++this.shieldBlocks;
    }

    public void setMana(float amount) {
        this.mana = Math.min(amount, this.maxMana);
    }

    public void setMaxMana(float amount) {
        this.maxMana = amount;
        if (this.mana > this.maxMana) {
            this.mana = this.maxMana;
        }
    }

    public void setManaRegenRate(float amount) {
        this.manaRegenRate = amount;
    }

    public float getManaRegenRate() {
        return this.manaRegenRate;
    }

    public boolean hasUnlockedLongswordOfSilence() {
        return this.unlockedLongswordOfSilence;
    }

    public void setUnlockedLongswordOfSilence(boolean unlocked) {
        this.unlockedLongswordOfSilence = unlocked;
    }

    public void setItemCooldownEnd(ResourceLocation itemId, long endTimeMillis) {
        this.itemCooldownEnds.put(itemId, endTimeMillis);
    }

    public Map<ResourceLocation, Long> getItemCooldownEnds() {
        return this.itemCooldownEnds;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Mana", this.mana);
        tag.putFloat("MaxMana", this.maxMana);
        tag.putFloat("ManaRegenRate", this.manaRegenRate);
        tag.putFloat("WaterMastery", this.waterMastery);
        tag.putFloat("FireMastery", this.fireMastery);
        tag.putFloat("EarthMastery", this.earthMastery);
        tag.putFloat("WindMastery", this.windMastery);
        CompoundTag spellsTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Float> entry : this.spellMasteryMap.entrySet()) {
            spellsTag.putFloat(entry.getKey().toString(), entry.getValue().floatValue());
        }
        tag.put("SpellMastery", (Tag)spellsTag);
        tag.putFloat("BaseSwordMastery", this.baseSwordMastery);
        tag.putFloat("SwordGodMastery", this.swordGodMastery);
        tag.putFloat("WaterGodMastery", this.waterGodMastery);
        tag.putFloat("NorthGodMastery", this.northGodMastery);
        if (this.activeStance != null) {
            tag.putString("activeStance", this.activeStance.name());
        }
        tag.putBoolean("UnlockedSwordGod", this.unlockedSwordGod);
        tag.putBoolean("UnlockedWaterGod", this.unlockedWaterGod);
        tag.putBoolean("UnlockedNorthGod", this.unlockedNorthGod);
        tag.putInt("ShieldBlocks", this.shieldBlocks);
        tag.putBoolean("UnlockedLongswordOfSilence", this.unlockedLongswordOfSilence);
        CompoundTag cdTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Long> entry : this.itemCooldownEnds.entrySet()) {
            cdTag.putLong(entry.getKey().toString(), entry.getValue().longValue());
        }
        tag.put("ItemCooldownEnds", (Tag)cdTag);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.mana = tag.getFloat("Mana");
        this.maxMana = tag.getFloat("MaxMana");
        this.manaRegenRate = tag.getFloat("ManaRegenRate");
        this.waterMastery = tag.getFloat("WaterMastery");
        this.fireMastery = tag.getFloat("FireMastery");
        this.earthMastery = tag.getFloat("EarthMastery");
        this.windMastery = tag.getFloat("WindMastery");
        this.spellMasteryMap.clear();
        if (tag.contains("SpellMastery")) {
            CompoundTag spellsTag = tag.getCompound("SpellMastery");
            for (String key : spellsTag.getAllKeys()) {
                this.spellMasteryMap.put(ResourceLocation.parse((String)key), Float.valueOf(spellsTag.getFloat(key)));
            }
        }
        this.baseSwordMastery = tag.getFloat("BaseSwordMastery");
        this.swordGodMastery = tag.getFloat("SwordGodMastery");
        this.waterGodMastery = tag.getFloat("WaterGodMastery");
        this.northGodMastery = tag.getFloat("NorthGodMastery");
        if (tag.contains("activeStance")) {
            try {
                this.activeStance = SwordStyle.valueOf(tag.getString("activeStance"));
            }
            catch (IllegalArgumentException e) {
                this.activeStance = null;
            }
        } else {
            this.activeStance = null;
        }
        this.unlockedSwordGod = tag.getBoolean("UnlockedSwordGod");
        this.unlockedWaterGod = tag.getBoolean("UnlockedWaterGod");
        this.unlockedNorthGod = tag.getBoolean("UnlockedNorthGod");
        this.shieldBlocks = tag.getInt("ShieldBlocks");
        if (tag.contains("UnlockedLongswordOfSilence")) {
            this.unlockedLongswordOfSilence = tag.getBoolean("UnlockedLongswordOfSilence");
        }
        this.itemCooldownEnds.clear();
        if (tag.contains("ItemCooldownEnds")) {
            CompoundTag cdTag = tag.getCompound("ItemCooldownEnds");
            for (String key : cdTag.getAllKeys()) {
                this.itemCooldownEnds.put(ResourceLocation.parse((String)key), cdTag.getLong(key));
            }
        }
        this.isToukiActive = false;
    }
}

