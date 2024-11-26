package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.registry.ExampleSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ChorusFlowerEntity extends AbstractMagicProjectile {

    public ChorusFlowerEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public ChorusFlowerEntity(Level pLevel, LivingEntity owner) {
        this(ModEntities.CHORUS_FLOWER_ENTITY.get(), pLevel);
        this.setOwner(owner);
    }

    @Override
    public void trailParticles() {
        for (int i = 0; i < 1; i++) {
            Vec3 random = new Vec3(
                    Utils.getRandomScaled(this.getBbWidth() * .5f),
                    0,
                    Utils.getRandomScaled(this.getBbWidth() * .5f)
            );
            level().addParticle(ParticleHelper.UNSTABLE_ENDER, getX() + random.x, getY() + .5f, getZ() + random.z, 0, -.05, 0);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity targetEntity = pResult.getEntity();

        if(targetEntity instanceof LivingEntity pEntityLiving && !this.level().isClientSide){

            DamageSources.applyDamage(pEntityLiving, getDamage(), ExampleSpellRegistry.MAYHEM.get().getDamageSource(this, getOwner()));
            targetEntity.invulnerableTime = 0;

            for(int i = 0; i < 16; ++i) {
                double d0 = pEntityLiving.getX() + (pEntityLiving.getRandom().nextDouble() - 0.5) * 16.0;
                double d1 = Mth.clamp(pEntityLiving.getY() + (double)(pEntityLiving.getRandom().nextInt(16) - 8), (double)level().getMinBuildHeight(), (double)(level().getMinBuildHeight() + ((ServerLevel)level()).getLogicalHeight() - 1));
                double d2 = pEntityLiving.getZ() + (pEntityLiving.getRandom().nextDouble() - 0.5) * 16.0;
                if (pEntityLiving.isPassenger()) {
                    pEntityLiving.stopRiding();
                }

                Vec3 vec3 = pEntityLiving.position();
                EntityTeleportEvent.ChorusFruit event = EventHooks.onChorusFruitTeleport(pEntityLiving, d0, d1, d2);

                if (pEntityLiving.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(pEntityLiving));
                    pEntityLiving.resetFallDistance();
                    break;
                }
            }

            if (pEntityLiving instanceof Player) {
                Player player = (Player)pEntityLiving;
                player.resetCurrentImpulseContext();
            }
        }

        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public float getSpeed() {
        return 1f;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.CHORUS_FRUIT_TELEPORT));
    }
}
