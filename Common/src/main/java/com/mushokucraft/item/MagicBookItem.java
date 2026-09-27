package com.mushokucraft.item;

import com.mushokucraft.client.gui.MagicBookScreen;
import com.mushokucraft.item.IMagicBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    @Environment(EnvType.CLIENT)
    private void openLearningScreen() {
        Minecraft.getInstance().setScreen((Screen)new MagicBookScreen(this.elementPrefix, this.spellIds));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController((GeoAnimatable)this, "controller", 0, state -> PlayState.STOP).triggerableAnim("cast", CAST_ANIM).triggerableAnim("idle", RawAnimation.begin().thenPlay("idle")));
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Screen getLearningScreen() {
        return new MagicBookScreen(this.elementPrefix, this.spellIds);
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


