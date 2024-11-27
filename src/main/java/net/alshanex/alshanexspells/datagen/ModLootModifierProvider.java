package net.alshanex.alshanexspells.datagen;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.datagen.loot.ModLootModifier;
import net.alshanex.alshanexspells.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ModLootModifierProvider extends GlobalLootModifierProvider {
    public ModLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AlshanexSpellsMod.MODID);
    }
    @Override
    protected void start() {

        add("mera_fruit_from_buried_treasure", new ModLootModifier(new LootItemCondition[] {
                LootItemRandomChanceCondition.randomChance(0.03f).build(),
                new LootTableIdCondition.Builder(ResourceLocation.parse("chests/buried_treasure")).build() },
                ModItems.MERAMERA.get()));

        add("hie_fruit_from_buried_treasure", new ModLootModifier(new LootItemCondition[] {
                LootItemRandomChanceCondition.randomChance(0.03f).build(),
                new LootTableIdCondition.Builder(ResourceLocation.parse("chests/buried_treasure")).build() },
                ModItems.HIEHIE.get()));
    }
}
