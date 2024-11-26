package net.alshanex.alshanexspells.entity.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.FrozenEntity;
import net.alshanex.alshanexspells.util.FrozenEntityRenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.HashMap;
import java.util.Map;

public class FrozenEntityRenderer extends EntityRenderer<FrozenEntity> {
    private final Map<String, EntityModel> modelMap = new HashMap();
    private final Map<String, Entity> hollowEntityMap = new HashMap();
    private final EntityRendererProvider.Context context;

    public FrozenEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FrozenEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    protected void preRenderCallback(FrozenEntity entity, PoseStack matrixStackIn, float partialTickTime) {
        float scale = entity.getScale() < 0.01F ? 1F : entity.getScale();
        matrixStackIn.scale(scale, scale, scale);
    }

    @Override
    public void render(FrozenEntity entityToFreeze, float entityYaw, float partialTicks, @NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        EntityModel model = new ZombieModel(context.bakeLayer(ModelLayers.ZOMBIE));

        if (modelMap.get(entityToFreeze.getFrozenEntityTypeString()) != null) {
            model = modelMap.get(entityToFreeze.getFrozenEntityTypeString());
        } else {
            Entity dummyEntity = entityToFreeze.getFrozenEntityType().create(Minecraft.getInstance().level);
            if (dummyEntity != null) {
                EntityRenderer renderer = Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .getRenderer(dummyEntity);

                if (renderer instanceof RenderLayerParent) {
                    model = ((RenderLayerParent<?, ?>) renderer).getModel();
                }
            }

            //EntityRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityToFreeze.getFrozenEntityType());
            modelMap.put(entityToFreeze.getFrozenEntityTypeString(), model);
        }
        if (model == null)
            return;

        Entity fakeEntity = null;
        if (this.hollowEntityMap.get(entityToFreeze.getFrozenEntityTypeString()) == null) {
            Entity build = entityToFreeze.getFrozenEntityType().create(Minecraft.getInstance().level);
            if (build != null) {
                try {
                    build.load(entityToFreeze.getFrozenEntityTag());
                } catch (Exception e) {
                    AlshanexSpellsMod.LOGGER.warn("Mob " + entityToFreeze.getFrozenEntityTypeString() + " could not build Frozen Entity");
                }
                fakeEntity = this.hollowEntityMap.putIfAbsent(entityToFreeze.getFrozenEntityTypeString(), build);
            }
        } else {
            fakeEntity = this.hollowEntityMap.get(entityToFreeze.getFrozenEntityTypeString());
        }
        RenderType tex = FrozenEntityRenderType.getFrozenEntityRenderType(200, 200);

        VertexConsumer ivertexbuilder = bufferIn.getBuffer(tex);


        matrixStackIn.pushPose();
        float yaw = entityToFreeze.yRotO + (entityToFreeze.getYRot() - entityToFreeze.yRotO) * partialTicks;
        boolean shouldSit = entityToFreeze.isPassenger() && (entityToFreeze.getVehicle() != null && entityToFreeze.getVehicle().shouldRiderSit());
        model.young = entityToFreeze.isBaby();
        model.riding = shouldSit;
        model.attackTime = entityToFreeze.getAttackAnim(partialTicks);
        if (fakeEntity != null) {
            model.setupAnim(fakeEntity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
        }
        preRenderCallback(entityToFreeze, matrixStackIn, partialTicks);
        matrixStackIn.translate(0, 1.5F, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw));
        model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
        matrixStackIn.popPose();
    }
}
