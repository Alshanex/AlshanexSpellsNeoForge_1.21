package net.alshanex.alshanexspells.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.PawEntity;
import net.alshanex.alshanexspells.util.ASpellAnimations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class PawSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(AlshanexSpellsMod.MODID, "paw_spell");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", getRadius(spellLevel, caster))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25)
            .build();

    public PawSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 10;
        this.baseManaCost = 60;
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
        return Optional.of(SoundRegistry.FIREBALL_START.get());
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 3, .35f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {

        Vec3 spawn = null;
        LivingEntity target = null;

        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            target = castTargetingData.getTarget((ServerLevel) level);
            if (target != null){
                spawn = target.position();
                PawEntity paw1 = new PawEntity(level, entity, 1, target, getRadius(spellLevel,entity));

                float spellDamage = getDamage(spellLevel, entity);

                if(getLostHealth(target) >= spellDamage){
                    target.heal(spellDamage);
                    paw1.setDamage(spellDamage);
                } else if(getLostHealth(target) <= (spellDamage/2)){
                    target.heal(getLostHealth(target));
                    paw1.setDamage(spellDamage/2);
                } else {
                    target.heal(getLostHealth(target));
                    paw1.setDamage(getLostHealth(target));
                }

                paw1.setExplosionRadius(getRadius(spellLevel, entity));

                paw1.setPos(spawn.x, spawn.y + target.getBbHeight() * 3, spawn.z);
                paw1.setDeltaMovement(0, -0.01, 0);

                paw1.setRotation(entity.getXRot(), entity.getYRot());

                level.addFreshEntity(paw1);
            }
        }
        if (spawn == null) {
            spawn = Utils.raycastForEntity(level, entity, 2f, true).getLocation();

            PawEntity paw2 = new PawEntity(level, entity, 0, null, getRadius(spellLevel,entity));

            paw2.setDamage(getDamage(spellLevel, entity));
            paw2.setExplosionRadius(getRadius(spellLevel, entity));

            paw2.setPos(spawn.x, spawn.y - (paw2.getBbHeight()/2),spawn.z);
            paw2.shoot(entity.getLookAngle());

            paw2.setRotation(entity.getXRot(), entity.getYRot());

            level.addFreshEntity(paw2);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getLostHealth(LivingEntity entity){
        return entity.getMaxHealth() - entity.getHealth();
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return (5 + 5 * getSpellPower(spellLevel, caster)) * .7f;
    }

    public int getRadius(int spellLevel, LivingEntity caster) {
        return 2 + (int) getSpellPower(spellLevel, caster);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ASpellAnimations.FIST_START_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ASpellAnimations.FIST_RELEASE_ANIMATION;
    }
}
