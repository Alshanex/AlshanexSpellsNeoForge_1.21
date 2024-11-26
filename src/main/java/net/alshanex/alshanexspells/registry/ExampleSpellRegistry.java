package net.alshanex.alshanexspells.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.alshanex.alshanexspells.spells.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ExampleSpellRegistry {

    public static final ResourceKey<Registry<AbstractSpell>> SPELL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "spells"));
    private static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_KEY, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    private static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final Supplier<AbstractSpell> HIKEN = registerSpell(new HikenSpell());
    public static final Supplier<AbstractSpell> HIBASHIRA = registerSpell(new HibashiraSpell());
    public static final Supplier<AbstractSpell> ICE_AGE = registerSpell(new IceAgeSpell());
    public static final Supplier<AbstractSpell> ICE_CHAMBER = registerSpell(new IceChamberSpell());
    public static final Supplier<AbstractSpell> MEGIDO = registerSpell(new MegidoSpell());
    public static final Supplier<AbstractSpell> FLOWER = registerSpell(new FlowerSpell());
    public static final Supplier<AbstractSpell> PAW = registerSpell(new PawSpell());
    public static final Supplier<AbstractSpell> MAYHEM = registerSpell(new EndMayhemSpell());
    public static final Supplier<AbstractSpell> FLAG_SPELL = registerSpell(new HolyFlagSpell());
}
