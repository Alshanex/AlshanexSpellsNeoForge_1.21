package net.alshanex.alshanexspells.setup;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.entity.custom.FlagEntity;
import net.alshanex.alshanexspells.entity.custom.FrozenEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = AlshanexSpellsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FLAG.get(), FlagEntity.createLivingAttributes().build());
        event.put(ModEntities.FROZEN_ENTITY.get(), FrozenEntity.frozenEntity().build());
    }
}
