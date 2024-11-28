package net.alshanex.alshanexspells.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.TargetAreaCastData;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.util.AUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@AutoSpellConfig
public class EndMayhemSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(AlshanexSpellsMod.MODID, "end_mayhem");
    List<Entity> summons = new ArrayList<>();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(16)
            .build();

    public EndMayhemSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 300;
        this.baseManaCost = 5;

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
        return Optional.of(SoundRegistry.ENDER_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, .35f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData != null && (playerMagicData.getCastDurationRemaining() + 1) % 20 == 0){
            if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData && entity instanceof Player player && level instanceof ServerLevel serverLevel) {
                LivingEntity target = castTargetingData.getTarget(serverLevel);
                if (target != null) {
                    Random randomForEntity = new Random();
                    int number = randomForEntity.nextInt(100) + 1;

                    Random random = new Random();

                    double theta = random.nextDouble() * 2 * Math.PI;
                    double phi = random.nextDouble() * Math.PI / 2;

                    double radius = getRadius(entity);
                    double x = radius * Math.sin(phi) * Math.cos(theta);
                    double y = radius * Math.cos(phi);
                    double z = radius * Math.sin(phi) * Math.sin(theta);

                    Vec3 portalPosition = target.position().add(x, y + target.getBbHeight() / 2, z);

                    Vec3 targetCenterPosition = target.position().add(0, target.getBbHeight() / 2, 0);
                    Vec3 directionToTarget = targetCenterPosition.subtract(portalPosition).normalize();

                    float rotationX = (float) -Math.toDegrees(Math.asin(directionToTarget.y));
                    float rotationY = (float) Math.toDegrees(Math.atan2(directionToTarget.z, directionToTarget.x)) - 90;

                    var portalData = new PortalData();
                    portalData.setPortalDuration(15);
                    PortalEntity portalEntity = setupPortalEntity(level, portalData, player, portalPosition, rotationY, rotationX);
                    level.addFreshEntity(portalEntity);

                    if(number <= 10) {
                        EnderMan enderman = new EnderMan(EntityType.ENDERMAN, level);
                        enderman.setPos(portalPosition);
                        summons.add(enderman);
                        level.addFreshEntity(enderman);
                        enderman.setTarget(target);
                    } else {
                        AbstractMagicProjectile projectile = AUtils.getMagicProjectile(entity, target);
                        projectile.setPos(portalPosition);
                        projectile.setXRot(rotationX);
                        projectile.setYRot(rotationY);

                        projectile.shoot(directionToTarget);

                        projectile.setDamage(getDamage(spellLevel, entity));

                        level.addFreshEntity(projectile);
                    }
                }
            }
        }
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        for (Entity summon : summons) {
            if (summon != null && summon.isAlive()) {
                MagicManager.spawnParticles(level, ParticleTypes.POOF, summon.getX(), summon.getY(), summon.getZ(), 25, .4, .8, .4, .03, false);
                summon.discard();
            }
        }
        summons.clear();
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
    }

    private PortalEntity setupPortalEntity(Level level, PortalData portalData, Player owner, Vec3 spawnPos, float rotationY, float rotationX) {
        var portalEntity = new PortalEntity(level, portalData);
        portalEntity.setOwnerUUID(owner.getUUID());
        portalEntity.moveTo(spawnPos);
        portalEntity.setYRot(rotationY);
        portalEntity.setXRot(rotationX);
        level.addFreshEntity(portalEntity);
        return portalEntity;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * .5f;
    }

    private float getRadius(LivingEntity caster) {
        return 6f;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }
}
