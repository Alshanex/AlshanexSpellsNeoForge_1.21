package net.alshanex.alshanexspells.entity;

import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES  = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AlshanexSpellsMod.MODID);

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    public static final DeferredHolder<EntityType<?>, EntityType<HikenEntity>> HIKEN_ENTITY =
            ENTITY_TYPES.register("hiken", () -> EntityType.Builder.<HikenEntity>of(HikenEntity::new, MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(4)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "hiken").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HibashiraEntity>> HIBASHIRA_ENTITY =
            ENTITY_TYPES.register("hibashira", () -> EntityType.Builder.<HibashiraEntity>of(HibashiraEntity::new, MobCategory.MISC)
                    .sized(4f, .8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "hibashira").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FrozenEntity>> FROZEN_ENTITY =
            ENTITY_TYPES.register("frozen_entity", () -> EntityType.Builder.<FrozenEntity>of(FrozenEntity::new, MobCategory.CREATURE)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "frozen_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FlowerEntity>> SPORE_BLOSSOM_ENTITY =
            ENTITY_TYPES.register("flower_entity", () -> EntityType.Builder.<FlowerEntity>of(FlowerEntity::new, MobCategory.MISC)
                    .sized(1.25f, 1)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "flower_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PawEntity>> PAW_ENTITY =
            ENTITY_TYPES.register("paw_entity", () -> EntityType.Builder.<PawEntity>of(PawEntity::new, MobCategory.MISC)
                    .sized(2f, 2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "paw_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EndStoneEntity>> END_STONE_ENTITY =
            ENTITY_TYPES.register("end_stone_entity", () -> EntityType.Builder.<EndStoneEntity>of(EndStoneEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "end_stone_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<DragonEggEntity>> DRAGON_EGG_ENTITY =
            ENTITY_TYPES.register("dragon_egg_entity", () -> EntityType.Builder.<DragonEggEntity>of(DragonEggEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "dragon_egg_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ChorusFlowerEntity>> CHORUS_FLOWER_ENTITY =
            ENTITY_TYPES.register("chorus_flower_entity", () -> EntityType.Builder.<ChorusFlowerEntity>of(ChorusFlowerEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "chorus_flower_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PurpurBricksEntity>> PURPUR_BRICKS_ENTITY =
            ENTITY_TYPES.register("purpur_bricks_entity", () -> EntityType.Builder.<PurpurBricksEntity>of(PurpurBricksEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "purpur_bricks_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PurpurPilarEntity>> PURPUR_PILAR_ENTITY =
            ENTITY_TYPES.register("purpur_pilar_entity", () -> EntityType.Builder.<PurpurPilarEntity>of(PurpurPilarEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "purpur_pilar_entity").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<DragonCircleEntity>> DRAGON_CIRCLE =
            ENTITY_TYPES.register("dragon_circle", () -> EntityType.Builder.<DragonCircleEntity>of(DragonCircleEntity::new, MobCategory.MISC)
                    .sized(4f, 1.2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "dragon_circle").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FlagEntity>> FLAG =
            ENTITY_TYPES.register("flag_entity", () -> EntityType.Builder.<FlagEntity>of(FlagEntity::new, MobCategory.MISC)
                    .sized(.2f, 2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID, "flag_entity").toString()));
}
