package net.alshanex.alshanexspells.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.render.GeoLivingEntityRenderer;
import net.alshanex.alshanexspells.entity.custom.FlagEntity;
import net.alshanex.alshanexspells.entity.models.FlagModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class FlagEntityRenderer extends GeoLivingEntityRenderer<FlagEntity> {
    public FlagEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new FlagModel());
        this.shadowRadius = .2f;
    }

    @Override
    public void preRender(PoseStack poseStack, FlagEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
