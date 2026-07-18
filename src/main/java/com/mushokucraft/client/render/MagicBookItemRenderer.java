package com.mushokucraft.client.render;

import com.mushokucraft.item.MagicBookItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MagicBookItemRenderer extends GeoItemRenderer<MagicBookItem> {
    public MagicBookItemRenderer(String textureName) {
        super(new MagicBookItemModel(textureName));
    }
}





