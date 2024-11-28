package net.alshanex.alshanexspells.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.effect.ModEffects;
import net.alshanex.alshanexspells.entity.custom.FlagEntity;
import net.alshanex.alshanexspells.event.ModEvents;
import net.alshanex.alshanexspells.util.ASpellAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class HolyFlagSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(AlshanexSpellsMod.MODID, "holy_flag");
    private FlagEntity currentFlag = null;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(45)
            .build();

    public HolyFlagSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 280;
        this.baseManaCost = 3;
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
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if(this.currentFlag == null){
            Vec3 spawn = null;
            HitResult raycast = Utils.raycastForEntity(level, entity, 1.5f, true, .25f);
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
            } else {
                spawn = raycast.getLocation().subtract(entity.getForward().normalize());
            }

            FlagEntity flag = new FlagEntity(level, entity, getRadius(spellLevel, entity));
            flag.setPos(spawn);
            flag.setYRot(entity.getYRot());

            this.currentFlag = flag;

            level.addFreshEntity(flag);

            entity.addEffect(new MobEffectInstance(ModEffects.ENCORE.getDelegate(), 280, 0));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        if(this.currentFlag != null){
            this.currentFlag.age = 280;
        }
        this.currentFlag = null;
        entity.removeEffect(ModEffects.ENCORE.getDelegate());
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
    }

    public int getRadius(int spellLevel, LivingEntity caster) {
        return (2 + spellLevel + (int) getSpellPower(spellLevel, caster) * 2);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ASpellAnimations.FLAG_SPAWN_ANIMATION;
    }
}
