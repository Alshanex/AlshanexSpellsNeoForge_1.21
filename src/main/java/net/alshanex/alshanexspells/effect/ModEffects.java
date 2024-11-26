package net.alshanex.alshanexspells.effect;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER  =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AlshanexSpellsMod.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final Holder<MobEffect> ENCORE = MOB_EFFECT_DEFERRED_REGISTER.register("encore",
            () -> new EncoreEffect(MobEffectCategory.BENEFICIAL, 0x9f0be3));

}
