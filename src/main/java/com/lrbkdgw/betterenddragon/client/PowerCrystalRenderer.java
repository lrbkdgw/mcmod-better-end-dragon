package com.lrbkdgw.betterenddragon.client;

import com.lrbkdgw.betterenddragon.crystal.CrystalKind;
import com.lrbkdgw.betterenddragon.crystal.PowerCrystalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * Renders a power crystal: the vanilla end crystal model tinted with the colour
 * of the crystal (and with an enchantment glint for the truth crystal).
 */
public class PowerCrystalRenderer extends EntityRenderer<PowerCrystalEntity> {
    private static final ResourceLocation END_CRYSTAL_LOCATION =
            new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4.0D);

    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public PowerCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        ModelPart root = context.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = root.getChild("glass");
        this.cube = root.getChild("cube");
        this.base = root.getChild("base");
    }

    @Override
    public void render(PowerCrystalEntity crystal, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        CrystalKind kind = crystal.getKind();
        float red = kind.red();
        float green = kind.green();
        float blue = kind.blue();

        poseStack.pushPose();
        float bob = getY(crystal, partialTick);
        float spin = ((float) crystal.time + partialTick) * 3.0F;
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, RENDER_TYPE, false, kind.glint());
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0F, -0.5F, 0.0F);
        int overlay = OverlayTexture.NO_OVERLAY;
        if (crystal.showsBottom()) {
            this.base.render(poseStack, consumer, light, overlay, red, green, blue, 1.0F);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.translate(0.0F, 1.5F + bob / 2.0F, 0.0F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3.0D), SIN_45, 0.0F, SIN_45));
        this.glass.render(poseStack, consumer, light, overlay, red, green, blue, 1.0F);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3.0D), SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        this.glass.render(poseStack, consumer, light, overlay, red, green, blue, 1.0F);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3.0D), SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        this.cube.render(poseStack, consumer, light, overlay, red, green, blue, 1.0F);
        poseStack.popPose();
        poseStack.popPose();

        BlockPos beamTarget = crystal.getBeamTarget();
        if (beamTarget != null) {
            float x = (float) beamTarget.getX() + 0.5F;
            float y = (float) beamTarget.getY() + 0.5F;
            float z = (float) beamTarget.getZ() + 0.5F;
            float dx = (float) ((double) x - crystal.getX());
            float dy = (float) ((double) y - crystal.getY());
            float dz = (float) ((double) z - crystal.getZ());
            poseStack.translate(dx, dy, dz);
            EnderDragonRenderer.renderCrystalBeams(-dx, -dy + bob, -dz, partialTick, crystal.time,
                    poseStack, buffer, light);
        }

        super.render(crystal, yaw, partialTick, poseStack, buffer, light);
    }

    public static float getY(PowerCrystalEntity crystal, float partialTick) {
        float time = (float) crystal.time + partialTick;
        float value = Mth.sin(time * 0.2F) / 2.0F + 0.5F;
        value = (value * value + value) * 0.4F;
        return value - 1.4F;
    }

    @Override
    public ResourceLocation getTextureLocation(PowerCrystalEntity crystal) {
        return END_CRYSTAL_LOCATION;
    }

    @Override
    public boolean shouldRender(PowerCrystalEntity crystal, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(crystal, frustum, x, y, z) || crystal.getBeamTarget() != null;
    }
}
