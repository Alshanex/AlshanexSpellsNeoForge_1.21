package net.alshanex.alshanexspells.entity.models;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.PawEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;

public class PawModel extends GeoModel<PawEntity> {
    private static final ResourceLocation TEXTURE_0 = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "textures/entity/paw.png");
    private static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "textures/entity/paw_red.png");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "geo/paw.geo.json");
    public static final ResourceLocation ANIMS = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "animations/paw_animation.json");


    public PawModel() {
    }

    @Override
    public ResourceLocation getTextureResource(PawEntity object) {
        if(object.getColor() == 1){
            return TEXTURE_1;
        } else {
            return TEXTURE_0;
        }
    }

    @Override
    public ResourceLocation getModelResource(PawEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(PawEntity animatable) {
        return ANIMS;
    }
}
