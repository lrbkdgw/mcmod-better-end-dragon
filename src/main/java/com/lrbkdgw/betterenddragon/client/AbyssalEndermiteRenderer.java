package com.lrbkdgw.betterenddragon.client;

import com.lrbkdgw.betterenddragon.entity.AbyssalEndermite;
import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AbyssalEndermiteRenderer extends MobRenderer<AbyssalEndermite, EndermiteModel<AbyssalEndermite>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/endermite.png");

    public AbyssalEndermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermiteModel<>(context.bakeLayer(ModelLayers.ENDERMITE)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssalEndermite entity) {
        return TEXTURE;
    }
}
