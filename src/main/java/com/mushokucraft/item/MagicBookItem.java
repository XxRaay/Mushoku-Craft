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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    private final String spellId;

    public MagicBookItem(Item.Properties properties, String elementPrefix, String spellId) {
        super(properties);
        this.elementPrefix = elementPrefix;
        this.spellId = spellId;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            this.openLearningScreen();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @OnlyIn(value=Dist.CLIENT)
    private void openLearningScreen() {
        Minecraft.getInstance().setScreen((Screen)new MagicBookScreen(this.elementPrefix, this.spellId));
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController((GeoAnimatable)this, "controller", 0, state -> PlayState.STOP).triggerableAnim("cast", CAST_ANIM).triggerableAnim("idle", RawAnimation.begin().thenPlay("idle")));
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public Screen getLearningScreen() {
        return new MagicBookScreen(this.elementPrefix, this.spellId);
    }

    @Override
    public void triggerCastAnimation(Player player, ItemStack stack) {
        long id = GeoItem.getId((ItemStack)stack);
        this.triggerAnim((Entity)player, id, "controller", "cast");
    }
}


