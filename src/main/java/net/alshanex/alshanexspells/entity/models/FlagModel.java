package net.alshanex.alshanexspells.entity.models;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.FlagEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FlagModel extends GeoModel<FlagEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "textures/entity/flag.png");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "geo/flag.geo.json");
    public static final ResourceLocation ANIMS = ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "animations/flag_animations.json");

    @Override
    public ResourceLocation getModelResource(FlagEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlagEntity mob) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FlagEntity animatable) {
        return ANIMS;
    }
}
