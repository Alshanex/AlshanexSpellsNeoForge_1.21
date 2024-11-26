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
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.network.particles.ShockwaveParticlesPacket;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.block.ModBlocks;
import net.alshanex.alshanexspells.datagen.EntityTagGenerator;
import net.alshanex.alshanexspells.entity.custom.FrozenEntity;
import net.alshanex.alshanexspells.util.ASpellAnimations;
import net.alshanex.alshanexspells.util.AUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.redspace.ironsspellbooks.api.util.Utils.random;

@AutoSpellConfig
public class IceAgeSpell extends AbstractSpell {
    //Texture by Amadhe

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "ice_age");

    private int initialRadius = 0;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public IceAgeSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 60 - 5;
        this.baseManaCost = 5;
    }

    @Override
    public int getCastTime(int spellLevel) {
        return castTime + 5 * spellLevel;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
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
        return Optional.of(SoundRegistry.CONE_OF_COLD_LOOP.get());
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData != null && (playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
            initialRadius+=2;
            if(initialRadius <= getRadius(spellLevel, entity)){
                freezeAround(level, spellLevel, entity, initialRadius);
            }
        }

        if (playerMagicData != null && (playerMagicData.getCastDurationRemaining() + 1) > 20 && (playerMagicData.getCastDurationRemaining() + 1) % 20 == 0) {
            Vec3 targetArea = entity.position();
            level.playSound(null, targetArea.x, targetArea.y, targetArea.z, SoundRegistry.ICE_BLOCK_CAST.get(), SoundSource.PLAYERS, 2, random.nextIntBetweenInclusive(8, 12) * .1f);
        }
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        initialRadius = 0;
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
    }

    public void freezeAround(Level level, int spellLevel, LivingEntity entity, int radius) {
        float particleRadius = entity.getBbWidth() + 1f;
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(SchoolRegistry.ICE.get().getTargetingColor(), particleRadius), entity.getX(), entity.getY() + .165f, entity.getZ(), 1, 0, 0, 0, 0, true);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new ShockwaveParticlesPacket(new Vec3(entity.getX(), entity.getY() + .165f, entity.getZ()), radius, ParticleRegistry.SNOWFLAKE_PARTICLE.get()));

        level.getEntities(entity, entity.getBoundingBox().inflate(radius, 4, radius), (target) -> !DamageSources.isFriendlyFireBetween(target, entity) && Utils.hasLineOfSight(level, entity, target, true)).forEach(target -> {
            if (target instanceof LivingEntity livingEntity && livingEntity.distanceToSqr(entity) < radius * radius && !DamageSources.isFriendlyFireBetween(target, entity) && !(target instanceof FrozenEntity)) {
                DamageSources.applyDamage(livingEntity, getDamage(spellLevel, entity), getDamageSource(entity));
                MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, livingEntity.getX(), livingEntity.getY() + livingEntity.getBbHeight() * .5f, livingEntity.getZ(), 50, livingEntity.getBbWidth() * .5f, livingEntity.getBbHeight() * .5f, livingEntity.getBbWidth() * .5f, .03, false);

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
        });

        BlockPos center = entity.blockPosition();
        Set<Block> excludedBlocks = AUtils.nonFreezeableBlocks();
        Set<Block> iceableBlocks = AUtils.iceableBlocks();
        Set<Block> iceReplazableBlocks = AUtils.iceReplazableBlocks();

        for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++) {
            for (int y = -6; y <= 6; y++) {
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
                    } else if (distance <= radius && blockState.getBlock() == Blocks.LAVA && !isAboveCenter) {
                        level.setBlockAndUpdate(currentPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                    }
                }
            }
        }
    }


    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFreezeTicks(80);
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return 1 + getSpellPower(spellLevel, caster) * .4f;
    }

    public float getRadius(int spellLevel, LivingEntity caster) {
        return 6 + spellLevel * 2 * .75f;
    }

    @Override
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return mob.distanceToSqr(target) > (10 * 10) * 1.2;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ASpellAnimations.TOUCH_GROUND_ANIMATION;
    }
}
