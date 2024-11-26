package net.alshanex.alshanexspells.item.custom;

import com.google.common.collect.ImmutableMultimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.alshanex.alshanexspells.registry.ExampleSpellRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class HieHieItem extends UniqueSpellBook {

    //Texture by GONEGOZZLE (https://pixeljoint.com/p/240990.htm)

    public HieHieItem() {
        super(SpellDataRegistryHolder.of(
                new SpellDataRegistryHolder(ExampleSpellRegistry.ICE_CHAMBER, 5),
                new SpellDataRegistryHolder(ExampleSpellRegistry.ICE_AGE, 10)
        ), 3);
        withSpellbookAttributes(new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE));
        withSpellbookAttributes(new AttributeContainer(AttributeRegistry.ICE_SPELL_POWER.getDelegate(), .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return true;
    }
}
