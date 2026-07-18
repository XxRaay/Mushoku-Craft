/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mushokucraft.client.render;

import com.mushokucraft.item.MagicBookItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MagicBookItemModel
extends GeoModel<MagicBookItem> {
    private final String textureName;

    public MagicBookItemModel(String textureName) {
        this.textureName = textureName;
    }

    public ResourceLocation getModelResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"geo/waterbook.geo.json");
    }

    public ResourceLocation getTextureResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)("textures/item/" + this.textureName));
    }

    public ResourceLocation getAnimationResource(MagicBookItem animatable) {
        return ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"animations/waterbookl.animation.json");
    }
}

