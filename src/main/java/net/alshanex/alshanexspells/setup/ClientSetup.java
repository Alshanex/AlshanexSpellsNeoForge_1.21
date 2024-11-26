package net.alshanex.alshanexspells.setup;

import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedleRenderer;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.block.ModBlocks;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.entity.renderers.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AlshanexSpellsMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HIKEN_ENTITY.get(), (context) -> new HikenEntityRenderer(context, 2f));
        event.registerEntityRenderer(ModEntities.HIBASHIRA_ENTITY.get(), (context) -> new HikenEntityRenderer(context, 5f));
        event.registerEntityRenderer(ModEntities.FROZEN_ENTITY.get(), FrozenEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.SPORE_BLOSSOM_ENTITY.get(), FlowerEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.PAW_ENTITY.get(), PawRenderer::new);
        event.registerEntityRenderer(ModEntities.END_STONE_ENTITY.get(), EndStoneEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_EGG_ENTITY.get(), DragonEggEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.CHORUS_FLOWER_ENTITY.get(), ChorusFlowerEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.PURPUR_BRICKS_ENTITY.get(), PurpurBricksEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.PURPUR_PILAR_ENTITY.get(), PurpurPilarEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.FLAG.get(), FlagEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_CIRCLE.get(), (context) -> new HikenEntityRenderer(context, 5f));
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ICE_SURFACE_BLOCK.get(), RenderType.translucent());
    }
}
