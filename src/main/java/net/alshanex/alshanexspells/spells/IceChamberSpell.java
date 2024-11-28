package net.alshanex.alshanexspells.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.block.ModBlocks;
import net.alshanex.alshanexspells.datagen.EntityTagGenerator;
import net.alshanex.alshanexspells.entity.custom.FrozenEntity;
import net.alshanex.alshanexspells.util.AUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@AutoSpellConfig
public class IceChamberSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(AlshanexSpellsMod.MODID, "ice_chamber");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.freeze_time", Utils.timeFromTicks(getFreezeTime(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(spellLevel, caster), 1))
        );
    }

    public IceChamberSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 10;
        this.baseManaCost = 25;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.RAY_OF_FROST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var hitResult = Utils.raycastForEntity(level, entity, getRange(spellLevel, entity), true, .15f);
        level.addFreshEntity(new RayOfFrostVisualEntity(level, entity.getEyePosition(), hitResult.getLocation(), entity));
        float radius = 3f;
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            //Set freeze time right here because it scales off of level and power
            DamageSources.applyDamage(target, getDamage(spellLevel, entity), getDamageSource(entity).setFreezeTicks(target.getTicksRequiredToFreeze() + getFreezeTime(spellLevel, entity)));

            if (target instanceof LivingEntity livingEntity && !DamageSources.isFriendlyFireBetween(target, entity)) {
                if((livingEntity.getHealth() < (livingEntity.getMaxHealth() * 0.1) || livingEntity.getHealth() <= getDamage(spellLevel, entity))){
                    if(!(livingEntity instanceof Player) && livingEntity.getType().is(EntityTagGenerator.FREEZEABLE_ENTITIES)){
                        FrozenEntity frozenEntity = FrozenEntity.buildFrozenEntity(livingEntity, entity);
                        frozenEntity.absMoveTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), livingEntity.getYRot(), livingEntity.getXRot());
                        frozenEntity.yBodyRot = livingEntity.getYRot();
                        frozenEntity.setShatterDamage(getDamage(spellLevel, entity));
                        frozenEntity.setDeathTimer(200);
                        livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                        livingEntity.kill();
                        livingEntity.remove(Entity.RemovalReason.KILLED);
                        level.addFreshEntity(frozenEntity);
                    }
                }
            }

            BlockPos center = target.blockPosition();
            Set<Block> excludedBlocks = AUtils.nonFreezeableBlocks();
            Set<Block> iceableBlocks = AUtils.iceableBlocks();
            Set<Block> iceReplazableBlocks = AUtils.iceReplazableBlocks();

            for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++) {
                for (int y = -1; y <= 3; y++) {
                    for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++) {
                        BlockPos currentPos = center.offset(x, y, z);
                        var blockState = level.getBlockState(currentPos);

                        double distance = Math.sqrt(x * x + z * z);

                        boolean isAboveCenter = currentPos.equals(center.above()) || currentPos.equals(center.above(2));

                        if (distance <= radius && !excludedBlocks.contains(blockState.getBlock()) &&
                                blockState.getFluidState().getType() == Fluids.EMPTY && !blockState.isAir() &&
                                !AUtils.isIceOrSnow(blockState.getBlock()) && !iceReplazableBlocks.contains(blockState.getBlock())) {

                            for (Direction direction : Direction.values()) {
                                BlockPos adjacentPos = currentPos.relative(direction);
                                var adjacentState = level.getBlockState(adjacentPos);

                                if (adjacentState.isAir() || iceableBlocks.contains(adjacentState.getBlock())) {
                                    BlockState iceSurfaceState = ModBlocks.ICE_SURFACE_BLOCK.get().defaultBlockState()
                                            .setValue(MultifaceBlock.getFaceProperty(direction.getOpposite()), true);

                                    level.setBlockAndUpdate(adjacentPos, iceSurfaceState);
                                } else if (adjacentState.getBlock() == ModBlocks.ICE_SURFACE_BLOCK.get()) {
                                    BlockState updatedState = adjacentState.setValue(MultifaceBlock.getFaceProperty(direction.getOpposite()), true);
                                    level.setBlockAndUpdate(adjacentPos, updatedState);
                                }
                            }

                        } else if (distance <= radius
                                && (blockState.getBlock() == Blocks.WATER || iceReplazableBlocks.contains(blockState.getBlock()))
                                && !isAboveCenter) {
                            if(iceReplazableBlocks.contains(blockState.getBlock())){
                                if(ServerConfigs.SPELL_GREIFING.get()){
                                    level.setBlockAndUpdate(currentPos, Blocks.ICE.defaultBlockState());
                                }
                            } else {
                                level.setBlockAndUpdate(currentPos, Blocks.ICE.defaultBlockState());
                            }
                        }  else if (distance <= radius && blockState.getBlock() == Blocks.LAVA && !isAboveCenter) {
                            level.setBlockAndUpdate(currentPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                        }
                    }
                }
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos center = new BlockPos((int)hitResult.getLocation().x, (int)hitResult.getLocation().y, (int)hitResult.getLocation().z);
            Set<Block> excludedBlocks = AUtils.nonFreezeableBlocks();
            Set<Block> iceableBlocks = AUtils.iceableBlocks();
            Set<Block> iceReplazableBlocks = AUtils.iceReplazableBlocks();

            for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++) {
                        BlockPos currentPos = center.offset(x, y, z);
                        var blockState = level.getBlockState(currentPos);

                        double distance = Math.sqrt(x * x + z * z);

                        boolean isAboveCenter = currentPos.equals(center.above()) || currentPos.equals(center.above(2));

                        if (distance <= radius && !excludedBlocks.contains(blockState.getBlock()) &&
                                blockState.getFluidState().getType() == Fluids.EMPTY && !blockState.isAir() &&
                                !AUtils.isIceOrSnow(blockState.getBlock()) && !iceReplazableBlocks.contains(blockState.getBlock())) {

                            for (Direction direction : Direction.values()) {
                                BlockPos adjacentPos = currentPos.relative(direction);
                                var adjacentState = level.getBlockState(adjacentPos);

                                if (adjacentState.isAir() || iceableBlocks.contains(adjacentState.getBlock())) {
                                    BlockState iceSurfaceState = ModBlocks.ICE_SURFACE_BLOCK.get().defaultBlockState()
                                            .setValue(MultifaceBlock.getFaceProperty(direction.getOpposite()), true);

                                    level.setBlockAndUpdate(adjacentPos, iceSurfaceState);
                                } else if (adjacentState.getBlock() == ModBlocks.ICE_SURFACE_BLOCK.get()) {
                                    BlockState updatedState = adjacentState.setValue(MultifaceBlock.getFaceProperty(direction.getOpposite()), true);
                                    level.setBlockAndUpdate(adjacentPos, updatedState);
                                }
                            }

                        } else if (distance <= radius
                                && (blockState.getBlock() == Blocks.WATER || iceReplazableBlocks.contains(blockState.getBlock()))
                                && !isAboveCenter) {
                            if(iceReplazableBlocks.contains(blockState.getBlock())){
                                if(ServerConfigs.SPELL_GREIFING.get()){
                                    level.setBlockAndUpdate(currentPos, Blocks.ICE.defaultBlockState());
                                }
                            } else {
                                level.setBlockAndUpdate(currentPos, Blocks.ICE.defaultBlockState());
                            }
                        }  else if (distance <= radius && blockState.getBlock() == Blocks.LAVA && !isAboveCenter) {
                            level.setBlockAndUpdate(currentPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                        }
                    }
                }
            }
        }
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, 50, 0, 0, 0, .3, false);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static float getRange(int level, LivingEntity caster) {
        return 30;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.5f;
    }

    private int getFreezeTime(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 15);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CHARGED_CAST;
    }
}
