package com.mushokucraft.init;

import com.google.common.collect.ImmutableSet;
import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.item.AncientManuscriptItem;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import dev.architectury.registry.level.entity.trade.SimpleTrade;
import dev.architectury.registry.level.entity.trade.TradeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModProfessions {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(MushokuCraftCommon.MOD_ID, Registries.POINT_OF_INTEREST_TYPE);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(MushokuCraftCommon.MOD_ID, Registries.VILLAGER_PROFESSION);

    public static final ResourceKey<PoiType> MAGE_POI_KEY = ResourceKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "mage")
    );

    public static final RegistrySupplier<PoiType> MAGE_POI = POI_TYPES.register("mage", () ->
            new PoiType(ImmutableSet.copyOf(ModBlocks.MAGE_WORKBENCH.get().getStateDefinition().getPossibleStates()), 1, 1)
    );

    public static final RegistrySupplier<VillagerProfession> MAGE = PROFESSIONS.register("mage", () ->
            new VillagerProfession(
                    "mage",
                    holder -> holder.is(MAGE_POI_KEY) || (MAGE_POI.isPresent() && holder.value() == MAGE_POI.get()),
                    holder -> holder.is(MAGE_POI_KEY) || (MAGE_POI.isPresent() && holder.value() == MAGE_POI.get()),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.ENCHANTMENT_TABLE_USE
            )
    );

    public static void register() {
        POI_TYPES.register();
        PROFESSIONS.register();
    }

    private static boolean tradesRegistered = false;

    public static void registerPoiStates() {
        try {
            if (!ModBlocks.MAGE_WORKBENCH.isPresent() || !MAGE_POI.isPresent()) {
                return;
            }
            Set<BlockState> states = ImmutableSet.copyOf(ModBlocks.MAGE_WORKBENCH.get().getStateDefinition().getPossibleStates());
            Field mapField = null;
            for (Field f : PoiTypes.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    mapField = f;
                    break;
                }
            }
            if (mapField != null) {
                mapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<BlockState, Holder<PoiType>> map = (Map<BlockState, Holder<PoiType>>) mapField.get(null);
                Optional<Holder.Reference<PoiType>> opt = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(MAGE_POI_KEY);
                Holder<PoiType> holder = opt.isPresent() ? opt.get() : BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(MAGE_POI.get());
                for (BlockState state : states) {
                    map.put(state, holder);
                }
                MushokuCraftCommon.LOGGER.info("Registered Mage POI states into PoiTypes.TYPE_BY_STATE: " + states.size() + " states with holder " + holder);
            }
        } catch (Exception e) {
            MushokuCraftCommon.LOGGER.error("Failed to register Mage POI states into PoiTypes", e);
        }
    }

    public static void registerTrades() {
        if (tradesRegistered) return;
        try {
            if (!MAGE.isPresent()) {
                return;
            }
            tradesRegistered = true;
            VillagerProfession mageProf = MAGE.get();

            // Level 1: Novice
            List<VillagerTrades.ItemListing> level1 = new ArrayList<>();
            level1.add(new SimpleTrade(new ItemCost(Items.PAPER, 24), Optional.empty(), new ItemStack(Items.EMERALD, 1), 16, 2, 0.05f));
            level1.add(new SimpleTrade(new ItemCost(Items.INK_SAC, 5), Optional.empty(), new ItemStack(Items.EMERALD, 1), 12, 2, 0.05f));
            level1.add(new SimpleTrade(new ItemCost(Items.EMERALD, 1), Optional.empty(), new ItemStack(ModItems.BLANK_CANVAS.get(), 2), 12, 2, 0.05f));
            level1.add(new SimpleTrade(new ItemCost(Items.EMERALD, 1), Optional.empty(), new ItemStack(Items.GLASS_BOTTLE, 4), 12, 2, 0.05f));
            TradeRegistry.registerVillagerTrade(mageProf, 1, level1.toArray(new VillagerTrades.ItemListing[0]));

            // Level 2: Apprentice
            List<VillagerTrades.ItemListing> level2 = new ArrayList<>();
            level2.add(new SimpleTrade(new ItemCost(Items.GOLD_INGOT, 4), Optional.empty(), new ItemStack(Items.EMERALD, 1), 12, 5, 0.05f));
            level2.add(new SimpleTrade(new ItemCost(Items.EMERALD, 4), Optional.empty(), new ItemStack(ModItems.SMALL_MANA_CRYSTAL.get(), 1), 12, 5, 0.05f));
            level2.add(new SimpleTrade(new ItemCost(Items.EMERALD, 5), Optional.empty(), new ItemStack(ModItems.HUNTING_KNIFE.get(), 1), 6, 5, 0.05f));
            addTrade(level2, ModItems.APPRENTICE_RING.get(), 1, 12, 3, 7, 0.05f);
            TradeRegistry.registerVillagerTrade(mageProf, 2, level2.toArray(new VillagerTrades.ItemListing[0]));

            // Level 3: Journeyman
            List<VillagerTrades.ItemListing> level3 = new ArrayList<>();
            level3.add(new SimpleTrade(new ItemCost(ModItems.SABERTOOTH_LEATHER.get(), 2), Optional.empty(), new ItemStack(Items.EMERALD, 3), 10, 10, 0.05f));
            level3.add(new SimpleTrade(new ItemCost(Items.EMERALD, 8), Optional.empty(), new ItemStack(ModItems.MEDIUM_MANA_CRYSTAL.get(), 1), 8, 10, 0.05f));
            addTrade(level3, ModItems.PYROMANCER_SPARK_CHARM.get(), 1, 14, 3, 10, 0.05f);
            addTrade(level3, ModItems.AQUAMANCER_DROP_PENDANT.get(), 1, 14, 3, 10, 0.05f);
            addTrade(level3, ModItems.ZEPHYR_FEATHER_CHARM.get(), 1, 14, 3, 10, 0.05f);
            addTrade(level3, ModItems.GEOMANCER_STONE_RING.get(), 1, 14, 3, 10, 0.05f);
            TradeRegistry.registerVillagerTrade(mageProf, 3, level3.toArray(new VillagerTrades.ItemListing[0]));

            // Level 4: Expert - Magic Books (Rare chance!)
            List<VillagerTrades.ItemListing> level4 = new ArrayList<>();
            level4.add(new SimpleTrade(new ItemCost(Items.EMERALD, 16), Optional.empty(), new ItemStack(ModItems.LARGE_MANA_CRYSTAL.get(), 1), 6, 15, 0.05f));
            level4.add(new RandomManuscriptTrade(18, 4, 15));
            level4.add(new SimpleTrade(new ItemCost(Items.EMERALD, 24), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.WATER_MAGIC_BOOK.get(), 1), 1, 20, 0.05f));
            level4.add(new SimpleTrade(new ItemCost(Items.EMERALD, 24), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.FIRE_MAGIC_BOOK.get(), 1), 1, 20, 0.05f));
            level4.add(new SimpleTrade(new ItemCost(Items.EMERALD, 24), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.EARTH_MAGIC_BOOK.get(), 1), 1, 20, 0.05f));
            level4.add(new SimpleTrade(new ItemCost(Items.EMERALD, 24), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.WIND_MAGIC_BOOK.get(), 1), 1, 20, 0.05f));
            TradeRegistry.registerVillagerTrade(mageProf, 4, level4.toArray(new VillagerTrades.ItemListing[0]));

            // Level 5: Master - Ancient Manuscript & Relics (Very rare chance!)
            List<VillagerTrades.ItemListing> level5 = new ArrayList<>();
            level5.add(new RandomManuscriptTrade(32, 2, 30));
            addTrade(level5, ModItems.SORCERER_BAND.get(), 1, 28, 2, 25, 0.05f);
            addTrade(level5, ModItems.MAGE_KNIGHT_AMULET.get(), 1, 28, 2, 25, 0.05f);
            level5.add(new SimpleTrade(new ItemCost(Items.EMERALD, 32), Optional.empty(), new ItemStack(ModItems.SWORD_GOD_SCROLL.get(), 1), 1, 30, 0.05f));
            TradeRegistry.registerVillagerTrade(mageProf, 5, level5.toArray(new VillagerTrades.ItemListing[0]));

            // Wandering Trader trades:
            TradeRegistry.registerTradeForWanderingTrader(true,
                    new RandomManuscriptTrade(24, 2, 8),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 28), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.WATER_MAGIC_BOOK.get(), 1), 1, 10, 0.05f),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 28), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.FIRE_MAGIC_BOOK.get(), 1), 1, 10, 0.05f),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 28), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.EARTH_MAGIC_BOOK.get(), 1), 1, 10, 0.05f),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 28), Optional.of(new ItemCost(Items.BOOK, 1)), new ItemStack(ModItems.WIND_MAGIC_BOOK.get(), 1), 1, 10, 0.05f)
            );
            TradeRegistry.registerTradeForWanderingTrader(false,
                    new SimpleTrade(new ItemCost(Items.EMERALD, 2), Optional.empty(), new ItemStack(ModItems.BLANK_CANVAS.get(), 3), 8, 1, 0.05f),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 4), Optional.empty(), new ItemStack(ModItems.SMALL_MANA_CRYSTAL.get(), 1), 5, 2, 0.05f),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 8), Optional.empty(), new ItemStack(ModItems.MEDIUM_MANA_CRYSTAL.get(), 1), 3, 3, 0.05f),
                    new SimpleTrade(new ItemCost(Items.EMERALD, 5), Optional.empty(), new ItemStack(ModItems.HUNTING_KNIFE.get(), 1), 2, 2, 0.05f)
            );

            MushokuCraftCommon.LOGGER.info("Successfully registered Wandering Mage trades.");
        } catch (Exception e) {
            MushokuCraftCommon.LOGGER.error("Failed to register Wandering Mage trades", e);
        }
    }

    private static void addTrade(List<VillagerTrades.ItemListing> list, Item item, int count, int emeraldCost, int maxUses, int xp, float priceMultiplier) {
        if (item != null && item != Items.AIR) {
            list.add(new SimpleTrade(new ItemCost(Items.EMERALD, emeraldCost), Optional.empty(), new ItemStack(item, count), maxUses, xp, priceMultiplier));
        }
    }

    /**
     * Custom trade that creates an Ancient Manuscript with a random circle type each time.
     */
    private static class RandomManuscriptTrade implements VillagerTrades.ItemListing {
        private final int emeraldCost;
        private final int maxUses;
        private final int xp;

        RandomManuscriptTrade(int emeraldCost, int maxUses, int xp) {
            this.emeraldCost = emeraldCost;
            this.maxUses = maxUses;
            this.xp = xp;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            List<ResourceLocation> allIds = MagicCircleRegistry.getAllIdsSorted();
            if (allIds.isEmpty()) return null;
            ResourceLocation circleType = allIds.get(random.nextInt(allIds.size()));
            ItemStack manuscript = AncientManuscriptItem.createForType(circleType);
            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    Optional.of(new ItemCost(Items.BOOK, 1)),
                    manuscript,
                    maxUses, xp, 0.05f
            );
        }
    }
}
