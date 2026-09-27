package com.mushokucraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.client.render.ClientSandstormTracker;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void mushokucraft$renderCamouflagedHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (ClientSandstormTracker.isCamouflaged(player)) {
            ResourceLocation skin = player.getSkin().texture();
            VertexConsumer armConsumer = buffer.getBuffer(RenderType.entityTranslucent(skin));
            arm.render(poseStack, armConsumer, packedLight, OverlayTexture.NO_OVERLAY, ClientSandstormTracker.SAND_CAMOUFLAGE_COLOR);
            sleeve.render(poseStack, armConsumer, packedLight, OverlayTexture.NO_OVERLAY, ClientSandstormTracker.SAND_CAMOUFLAGE_COLOR);
            ci.cancel();
        }
    }
}
