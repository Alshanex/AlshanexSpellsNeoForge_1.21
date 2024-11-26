package net.alshanex.alshanexspells.item.custom;

import com.google.common.collect.ImmutableMultimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.alshanex.alshanexspells.registry.ExampleSpellRegistry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;


public class MeraMeraItem extends UniqueSpellBook {

    //Texture by GONEGOZZLE (https://pixeljoint.com/p/240990.htm)

    public MeraMeraItem() {
        super(SpellDataRegistryHolder.of(
                new SpellDataRegistryHolder(ExampleSpellRegistry.HIKEN, 5),
                new SpellDataRegistryHolder(ExampleSpellRegistry.HIBASHIRA, 10)
        ), 3);
        withSpellbookAttributes(new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE));
        withSpellbookAttributes(new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER.getDelegate(), .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    }
}
