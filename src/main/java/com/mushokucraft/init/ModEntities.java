package com.mushokucraft.init;

import com.mushokucraft.combat.entity.ThrownSwordEntity;
import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import com.mushokucraft.magic.entity.AirStrikeEntity;
import com.mushokucraft.magic.entity.FireballEntity;
import com.mushokucraft.magic.entity.RockBulletEntity;
import com.mushokucraft.magic.entity.WaterballEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"mushokucraft");
    public static final Supplier<EntityType<SabertoothWolfEntity>> SABERTOOTH_WOLF = ENTITIES.register("sabertooth_wolf", () -> EntityType.Builder.<SabertoothWolfEntity>of(SabertoothWolfEntity::new, MobCategory.MONSTER).sized(1.4f, 1.5f).clientTrackingRange(8).build("sabertooth_wolf"));
    public static final Supplier<EntityType<DeadSabertoothWolfEntity>> DEAD_SABERTOOTH_WOLF = ENTITIES.register("dead_sabertooth_wolf", () -> EntityType.Builder.<DeadSabertoothWolfEntity>of(DeadSabertoothWolfEntity::new, MobCategory.MISC).sized(1.6f, 0.5f).clientTrackingRange(8).build("dead_sabertooth_wolf"));
    public static final Supplier<EntityType<WaterballEntity>> WATERBALL = ENTITIES.register("waterball", () -> EntityType.Builder.<WaterballEntity>of(WaterballEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("waterball"));
    public static final Supplier<EntityType<FireballEntity>> FIREBALL = ENTITIES.register("fireball", () -> EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("fireball"));
    public static final Supplier<EntityType<RockBulletEntity>> ROCK_BULLET = ENTITIES.register("rockbullet", () -> EntityType.Builder.<RockBulletEntity>of(RockBulletEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("rockbullet"));
    public static final Supplier<EntityType<AirStrikeEntity>> AIR_STRIKE = ENTITIES.register("airstrike", () -> EntityType.Builder.<AirStrikeEntity>of(AirStrikeEntity::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(4).updateInterval(10).build("airstrike"));
    public static final Supplier<EntityType<ThrownSwordEntity>> THROWN_SWORD = ENTITIES.register("thrown_sword", () -> EntityType.Builder.<ThrownSwordEntity>of(ThrownSwordEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10).build("thrown_sword"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}


