package com.mushokucraft.item;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MagicBookItem
extends Item
implements GeoItem,
IMagicBook {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    public static final RawAnimation CAST_ANIM = RawAnimation.begin().thenPlay("cast");
    private final String elementPrefix;
    private final java.util.List<String> spellIds;

    public MagicBookItem(Item.Properties properties, String elementPrefix, java.util.List<String> spellIds) {
        super(properties);
        this.elementPrefix = elementPrefix;
        this.spellIds = spellIds;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            this.openLearningScreen();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void openLearningScreen() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.mushokucraft.client.gui.ClientScreenOpener.openMagicBookScreen(this.elementPrefix, this.spellIds));
    }

    @Override
    public void registerControllers(software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new software.bernie.geckolib.animation.AnimationController((software.bernie.geckolib.animatable.GeoAnimatable)this, "controller", 0, state -> software.bernie.geckolib.animation.PlayState.STOP).triggerableAnim("cast", CAST_ANIM).triggerableAnim("idle", software.bernie.geckolib.animation.RawAnimation.begin().thenPlay("idle")));
    }

    @Override
    public software.bernie.geckolib.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void triggerCastAnimation(Player player, ItemStack stack) {
        long id = GeoItem.getId((ItemStack)stack);
        this.triggerAnim((Entity)player, id, "controller", "cast");
    }

    @Override
    public void createGeoRenderer(java.util.function.Consumer<software.bernie.geckolib.animatable.client.GeoRenderProvider> consumer) {
        consumer.accept(new software.bernie.geckolib.animatable.client.GeoRenderProvider() {
            private com.mushokucraft.client.render.MagicBookItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new com.mushokucraft.client.render.MagicBookItemRenderer(
                        elementPrefix + "_magic_book",
                        elementPrefix + "_magic_book",
                        elementPrefix + "_magic_book"
                    );
                }
                return this.renderer;
            }
        });
    }
}


