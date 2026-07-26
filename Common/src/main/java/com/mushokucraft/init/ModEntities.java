package com.mushokucraft.init;

import com.mushokucraft.combat.entity.ThrownSwordEntity;
import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import com.mushokucraft.magic.entity.AirStrikeEntity;
import com.mushokucraft.magic.entity.FireballEntity;
import com.mushokucraft.magic.entity.RockBulletEntity;
import com.mushokucraft.magic.entity.WaterSliceEntity;
import com.mushokucraft.magic.entity.WaterballEntity;
import com.mushokucraft.magic.entity.IcicleBreakTargetEntity;
import com.mushokucraft.magic.entity.IcicleEntity;
import com.mushokucraft.magic.entity.CumulonimbusStormEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create("mushokucraft", Registries.ENTITY_TYPE);
    public static final RegistrySupplier<EntityType<SabertoothWolfEntity>> SABERTOOTH_WOLF = ENTITIES.register("sabertooth_wolf", () -> EntityType.Builder.<SabertoothWolfEntity>of(SabertoothWolfEntity::new, MobCategory.MONSTER).sized(1.4f, 1.5f).clientTrackingRange(8).build("sabertooth_wolf"));
    public static final Supplier<EntityType<DeadSabertoothWolfEntity>> DEAD_SABERTOOTH_WOLF = ENTITIES.register("dead_sabertooth_wolf", () -> EntityType.Builder.<DeadSabertoothWolfEntity>of(DeadSabertoothWolfEntity::new, MobCategory.MISC).sized(1.6f, 0.5f).clientTrackingRange(8).build("dead_sabertooth_wolf"));
    public static final Supplier<EntityType<WaterballEntity>> WATERBALL = ENTITIES.register("waterball", () -> EntityType.Builder.<WaterballEntity>of(WaterballEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("waterball"));
    public static final Supplier<EntityType<WaterSliceEntity>> WATER_SLICE = ENTITIES.register("water_slice", () -> EntityType.Builder.<WaterSliceEntity>of(WaterSliceEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("water_slice"));
    public static final Supplier<EntityType<FireballEntity>> FIREBALL = ENTITIES.register("fireball", () -> EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("fireball"));
    public static final Supplier<EntityType<RockBulletEntity>> ROCK_BULLET = ENTITIES.register("rockbullet", () -> EntityType.Builder.<RockBulletEntity>of(RockBulletEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("rockbullet"));
    public static final Supplier<EntityType<AirStrikeEntity>> AIR_STRIKE = ENTITIES.register("airstrike", () -> EntityType.Builder.<AirStrikeEntity>of(AirStrikeEntity::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(4).updateInterval(10).build("airstrike"));
    public static final Supplier<EntityType<ThrownSwordEntity>> THROWN_SWORD = ENTITIES.register("thrown_sword", () -> EntityType.Builder.<ThrownSwordEntity>of(ThrownSwordEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("thrown_sword"));
    public static final Supplier<EntityType<IcicleBreakTargetEntity>> ICICLE_BREAK_TARGET = ENTITIES.register("icicle_break_target", () -> EntityType.Builder.<IcicleBreakTargetEntity>of(IcicleBreakTargetEntity::new, MobCategory.MISC).sized(6.0f, 0.1f).clientTrackingRange(10).updateInterval(2).build("icicle_break_target"));
    public static final Supplier<EntityType<IcicleEntity>> ICICLE_ENTITY = ENTITIES.register("icicle_entity", () -> EntityType.Builder.<IcicleEntity>of(IcicleEntity::new, MobCategory.MISC).sized(0.5f, 1.5f).clientTrackingRange(10).updateInterval(2).build("icicle_entity"));
    public static final Supplier<EntityType<CumulonimbusStormEntity>> CUMULONIMBUS_STORM = ENTITIES.register("cumulonimbus_storm", () -> EntityType.Builder.<CumulonimbusStormEntity>of(CumulonimbusStormEntity::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(10).updateInterval(2).build("cumulonimbus_storm"));

    public static void register() {
        ENTITIES.register();
    }
}


