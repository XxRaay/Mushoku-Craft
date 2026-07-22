package com.mushokucraft.client.render;

import com.mushokucraft.item.MagicBookItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MagicBookItemModel extends GeoModel<MagicBookItem> {
    private final String texturePath;
    private final String modelPath;
    private final String animPath;

    public MagicBookItemModel(String texturePath, String modelPath, String animPath) {
        this.texturePath = texturePath;
        this.modelPath = modelPath;
        this.animPath = animPath;
    }

    @Override
    public ResourceLocation getModelResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "geo/" + this.modelPath + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/item/" + this.texturePath + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "animations/" + this.animPath + ".animation.json");
    }
}





