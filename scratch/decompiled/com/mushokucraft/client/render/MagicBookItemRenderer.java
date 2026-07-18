/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 */
package com.mushokucraft.client.render;

import com.mushokucraft.client.render.MagicBookItemModel;
import com.mushokucraft.item.MagicBookItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MagicBookItemRenderer
extends GeoItemRenderer<MagicBookItem> {
    public MagicBookItemRenderer(String textureName) {
        super((GeoModel)new MagicBookItemModel(textureName));
    }
}

