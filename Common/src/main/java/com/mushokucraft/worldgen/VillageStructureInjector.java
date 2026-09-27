package com.mushokucraft.worldgen;

import com.mojang.datafixers.util.Pair;
import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.config.MushokuConfig;
import dev.architectury.event.events.common.LifecycleEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class VillageStructureInjector {
    private static final List<ResourceLocation> VILLAGE_HOUSE_POOLS = List.of(
            ResourceLocation.withDefaultNamespace("village/plains/houses"),
            ResourceLocation.withDefaultNamespace("village/taiga/houses"),
            ResourceLocation.withDefaultNamespace("village/savanna/houses"),
            ResourceLocation.withDefaultNamespace("village/snowy/houses"),
            ResourceLocation.withDefaultNamespace("village/desert/houses")
    );

    public static void register() {
        LifecycleEvent.SERVER_STARTING.register(server -> {
            Registry<StructureTemplatePool> poolRegistry = server.registryAccess().registry(Registries.TEMPLATE_POOL).orElse(null);
            if (poolRegistry == null) return;

            int weight = MushokuConfig.MAGE_HOUSE_VILLAGE_WEIGHT.get();
            if (weight <= 0) return;

            StructurePoolElement mageHouseElement = StructurePoolElement.legacy("mushokucraft:village/houses/mage_house")
                    .apply(StructureTemplatePool.Projection.RIGID);

            for (ResourceLocation poolId : VILLAGE_HOUSE_POOLS) {
                StructureTemplatePool pool = poolRegistry.get(poolId);
                if (pool != null) {
                    injectPiece(pool, mageHouseElement, weight, poolId);
                }
            }
        });
    }

    private static void injectPiece(StructureTemplatePool pool, StructurePoolElement element, int weight, ResourceLocation poolId) {
        try {
            Field rawTemplatesField = null;
            Field templatesField = null;

            for (Field f : StructureTemplatePool.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType()) && !ObjectArrayList.class.isAssignableFrom(f.getType())) {
                    rawTemplatesField = f;
                } else if (ObjectArrayList.class.isAssignableFrom(f.getType())) {
                    templatesField = f;
                }
            }

            if (rawTemplatesField != null && templatesField != null) {
                rawTemplatesField.setAccessible(true);
                templatesField.setAccessible(true);

                @SuppressWarnings("unchecked")
                List<Pair<StructurePoolElement, Integer>> raw = (List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool);
                List<Pair<StructurePoolElement, Integer>> mutableRaw = new ArrayList<>(raw);
                mutableRaw.add(Pair.of(element, weight));
                rawTemplatesField.set(pool, mutableRaw);

                @SuppressWarnings("unchecked")
                ObjectArrayList<StructurePoolElement> templates = (ObjectArrayList<StructurePoolElement>) templatesField.get(pool);
                for (int i = 0; i < weight; i++) {
                    templates.add(element);
                }
                MushokuCraftCommon.LOGGER.info("Injected mage_house into village pool {} with weight {}", poolId, weight);
            }
        } catch (Exception e) {
            MushokuCraftCommon.LOGGER.error("Failed to inject mage_house into pool " + poolId, e);
        }
    }
}
