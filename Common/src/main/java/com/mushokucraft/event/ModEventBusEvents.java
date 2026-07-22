package com.mushokucraft.event;

import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import com.mushokucraft.init.ModEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;


public class ModEventBusEvents {
    public static void register() {
        EntityAttributeRegistry.register(ModEntities.SABERTOOTH_WOLF, SabertoothWolfEntity::createAttributes);
        // SpawnPlacementRegistry.register(ModEntities.SABERTOOTH_WOLF.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
    }
}


