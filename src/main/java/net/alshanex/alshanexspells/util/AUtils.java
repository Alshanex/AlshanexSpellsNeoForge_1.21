package net.alshanex.alshanexspells.util;

import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.entity.spells.magic_missile.MagicMissileProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.alshanex.alshanexspells.block.ModBlocks;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.entity.custom.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AUtils {

    public static Set<Block> nonFreezeableBlocks(){
        Set<Block> excludedBlocks = new HashSet<>();

        Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;

        for (Block block : blockRegistry) {
            if (block instanceof TrapDoorBlock || block instanceof DoorBlock || block instanceof SignBlock || block instanceof BedBlock
                    || block instanceof AbstractBannerBlock || block instanceof AbstractCandleBlock || block == Blocks.GLASS_PANE || block instanceof StainedGlassPaneBlock
                    || block instanceof AbstractChestBlock<?> || block instanceof BarrierBlock || block instanceof AbstractSkullBlock
                    || block instanceof CoralBlock || block instanceof CoralFanBlock || block instanceof CoralPlantBlock
                    || block instanceof CoralWallFanBlock || block instanceof PressurePlateBlock || block instanceof RailBlock
                    || block instanceof BeehiveBlock || block instanceof BellBlock || block instanceof BrewingStandBlock
                    || block instanceof ButtonBlock || block instanceof CampfireBlock || block instanceof ChorusPlantBlock
                    || block instanceof StructureBlock || block instanceof CakeBlock || block instanceof CommandBlock
                    || block instanceof ShulkerBoxBlock || block instanceof EnchantingTableBlock || block instanceof CactusBlock
                    || block instanceof BeaconBlock || block instanceof FlowerPotBlock || block instanceof EndGatewayBlock
                    || block instanceof EndPortalBlock || block instanceof EndRodBlock || block instanceof NetherPortalBlock
                    || block instanceof EndPortalFrameBlock || block instanceof RedStoneWireBlock || block instanceof HopperBlock
                    || block instanceof ComparatorBlock || block instanceof RepeaterBlock || block instanceof VineBlock
                    || block instanceof SpawnerBlock || block instanceof LadderBlock || block instanceof RespawnAnchorBlock
                    || block instanceof LeverBlock || block instanceof DaylightDetectorBlock || block instanceof ChorusFlowerBlock
                    || block instanceof WaterlilyBlock || block instanceof SculkShriekerBlock || block instanceof TripWireHookBlock
                    || block instanceof SculkSensorBlock || block instanceof DragonEggBlock || block instanceof TripWireBlock
                    || block instanceof TallSeagrassBlock || block instanceof  KelpPlantBlock || block instanceof TwistingVinesBlock
                    || block instanceof BambooStalkBlock || block instanceof BambooSaplingBlock || block instanceof SugarCaneBlock) {
                excludedBlocks.add(block);
            }
        }

        return excludedBlocks;
    }

    public static Set<Block> iceableBlocks(){
        Set<Block> excludedBlocks = new HashSet<>();

        Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;

        for (Block block : blockRegistry) {
            if (block instanceof TorchBlock || block instanceof CarpetBlock || block instanceof SculkVeinBlock
                    || block instanceof BushBlock || block instanceof TallGrassBlock || block instanceof GlowLichenBlock) {
                excludedBlocks.add(block);
            }
        }

        return excludedBlocks;
    }

    public static Set<Block> iceReplazableBlocks(){
        Set<Block> excludedBlocks = new HashSet<>();

        Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;

        for (Block block : blockRegistry) {
            if (block instanceof FenceBlock || block instanceof SlabBlock || block instanceof StairBlock || block instanceof DirtPathBlock) {
                excludedBlocks.add(block);
            }
        }

        return excludedBlocks;
    }

    public static boolean isIceOrSnow(Block block) {
        return block instanceof IceBlock || block instanceof SnowLayerBlock
                || block instanceof PowderSnowBlock || block == ModBlocks.ICE_SURFACE_BLOCK.get();
    }

    public static boolean hasItemInSpellbookSlot(ServerPlayer player, Item itemToCheck) {
        return CuriosApi.getCuriosInventory(player).map(inventory ->
                inventory.getStacksHandler(Curios.SPELLBOOK_SLOT).map(stacksHandler -> {
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        if (stacksHandler.getStacks().getStackInSlot(i).is(itemToCheck)) {
                            return true;
                        }
                    }
                    return false;
                }).orElse(false)
        ).orElse(false);
    }

    public static boolean isNonFreezeable(EntityType type) {
        return type == EntityType.ENDER_DRAGON || type == EntityType.ELDER_GUARDIAN || type == EntityType.EVOKER ||
                type == EntityType.GHAST || type == EntityType.WARDEN || type == EntityType.GIANT ||
                type == EntityType.ILLUSIONER || type == EntityType.STRAY || type == EntityType.STRIDER ||
                type == EntityRegistry.DEBUG_WIZARD.get() || type == EntityRegistry.ARCHEVOKER.get()
                || type == EntityRegistry.PYROMANCER.get() || type == EntityRegistry.NECROMANCER.get()
                || type == EntityRegistry.CRYOMANCER.get() || type == EntityRegistry.DEAD_KING.get()
                || type == EntityRegistry.DEAD_KING_CORPSE.get() || type == EntityRegistry.CATACOMBS_ZOMBIE.get()
                || type == EntityRegistry.SUMMONED_POLAR_BEAR.get() || type == EntityRegistry.SUMMONED_SKELETON.get()
                || type == EntityRegistry.SUMMONED_VEX.get() || type == EntityRegistry.SUMMONED_ZOMBIE.get()
                || type == EntityRegistry.CULTIST.get() || type == EntityRegistry.KEEPER.get() || type == EntityType.POLAR_BEAR
                || type == EntityRegistry.SPECTRAL_STEED.get() || type == ModEntities.FROZEN_ENTITY.get();
    }

    public static boolean isFireDamage(DamageType type) {
        return type.equals(DamageTypes.IN_FIRE) || type.equals(DamageTypes.ON_FIRE) ||
                type.equals(DamageTypes.LAVA) || type.equals(DamageTypes.HOT_FLOOR);
    }

    public static AbstractMagicProjectile getMagicProjectile(LivingEntity caster, LivingEntity target){
        Random random = new Random();
        int number = random.nextInt(20) + 1;

        if (number == 1) {
            return new DragonEggEntity(caster.level(), caster);
        } else if (number == 2) {
            return new ChorusFlowerEntity(caster.level(), caster);
        } else if (number <= 6) {
            return new EndStoneEntity(caster.level(), caster);
        } else if (number <= 9) {
            return new PurpurBricksEntity(caster.level(), caster);
        } else if (number <= 12) {
            return new PurpurPilarEntity(caster.level(), caster);
        } else if (number <= 16) {
            return new MagicArrowProjectile(caster.level(), caster);
        } else {
            return new MagicMissileProjectile(caster.level(), caster);
        }
    }
}
