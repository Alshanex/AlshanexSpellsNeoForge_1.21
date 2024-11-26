package net.alshanex.alshanexspells.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.PawEntity;
import net.alshanex.alshanexspells.entity.models.PawModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class PawRenderer extends GeoEntityRenderer<PawEntity> {

    private static final ResourceLocation TEXTURE_0 = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "textures/entity/paw.png");
    private static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "textures/entity/paw_red.png");

    public PawRenderer(EntityRendererProvider.Context context) {
        super(context, new PawModel());
        this.shadowRadius = .75f;
    }

    @Override
    public void preRender(PoseStack poseStack, PawEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        Vec3 motion = animatable.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public RenderType getRenderType(PawEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public ResourceLocation getTextureLocation(PawEntity animatable) {
        if(animatable.getColor() == 1){
            return TEXTURE_1;
        } else {
            return TEXTURE_0;
        }
    }
}
