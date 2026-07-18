package com.mushokucraft.client.render;

import com.mushokucraft.item.MagicBookItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MagicBookItemModel extends GeoModel<MagicBookItem> {
    private final String textureName;

    public MagicBookItemModel(String textureName) {
        this.textureName = textureName;
    }

    @Override
    public ResourceLocation getModelResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "geo/waterbook.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/item/" + textureName);
    }

    @Override
    public ResourceLocation getAnimationResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "animations/waterbookl.animation.json");
    }
}





