package net.alshanex.alshanexspells.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.ball_lightning.BallLightning;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.custom.MegidoEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@AutoSpellConfig
public class MegidoSpell extends AbstractSpell {
    //Texture by Amadhe

    private final ResourceLocation spellId = new ResourceLocation(AlshanexSpellsMod.MODID, "megido");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public MegidoSpell() {
        this.manaCostPerLevel = 7;
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
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 30, .35f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null) {
                int numberOfEntities = 8;
                double maxRadius = getRadius();

                double minY = targetEntity.getY() + targetEntity.getBbHeight() + 1;
                double maxY = targetEntity.getY() + targetEntity.getBbHeight() + 4;

                for (int i = 0; i < numberOfEntities; i++) {
                    double randomAngle = Math.random() * 2 * Math.PI;
                    double randomRadius = Math.random() * maxRadius;
                    double x = targetEntity.getX() + (targetEntity.getBbWidth() / 2) + randomRadius * Math.cos(randomAngle);
                    double z = targetEntity.getZ() + (targetEntity.getBbWidth() / 2) + randomRadius * Math.sin(randomAngle);

                    double y = minY + Math.random() * (maxY - minY);

                    BallLightning ballLightning = new BallLightning(world, entity);
                    ballLightning.setPos(new Vec3(x, y, z));
                    ballLightning.setDamage(0);

                    world.addFreshEntity(ballLightning);
                }

                MegidoEntity chainLightning = new MegidoEntity(world, entity, targetEntity);
                chainLightning.setDamage(getDamage(spellLevel, entity));
                chainLightning.range = getRange();
                chainLightning.maxConnections = getMaxConnections(spellLevel, entity);
                world.addFreshEntity(chainLightning);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    public float getRadius(){
        return 10f;
    }

    public int getMaxConnections(int spellLevel, LivingEntity caster) {
        return Integer.MAX_VALUE;
    }

    public float getRange() {
        return 15f;
    }
}
