package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.registry.ExampleSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DragonEggEntity extends AbstractMagicProjectile {

    public DragonEggEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public DragonEggEntity(Level pLevel, LivingEntity owner) {
        this(ModEntities.DRAGON_EGG_ENTITY.get(), pLevel);
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

    private float horizontalDistanceSqr(LivingEntity livingEntity, Vec3 vec3) {
        var dx = livingEntity.getX() - vec3.x;
        var dz = livingEntity.getZ() - vec3.z;
        return (float) (dx * dx + dz * dz);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if(!this.level().isClientSide){
            Entity targetEntity = pResult.getEntity();

            DamageSources.applyDamage(targetEntity, getDamage(), ExampleSpellRegistry.MAYHEM.get().getDamageSource(this, getOwner()));
            targetEntity.invulnerableTime = 0;

            Vec3 targetArea = targetEntity.position();
            var radius = 3f;
            var radiusSqr = radius * radius;
            var damage = getDamage();
            var source = ExampleSpellRegistry.MAYHEM.get().getDamageSource(this, getOwner());
            this.level().getEntitiesOfClass(LivingEntity.class, new AABB(targetArea.subtract(radius, radius, radius), targetArea.add(radius, radius, radius)),
                            livingEntity -> livingEntity != getOwner() &&
                                    horizontalDistanceSqr(livingEntity, targetArea) < radiusSqr &&
                                    livingEntity.isPickable() &&
                                    !DamageSources.isFriendlyFireBetween(livingEntity, getOwner()) &&
                                    Utils.hasLineOfSight(this.level(), targetArea.add(0, 1.5, 0), livingEntity.getBoundingBox().getCenter(), true))
                    .forEach(livingEntity -> {
                        DamageSources.applyDamage(livingEntity, damage, source);
                        DamageSources.ignoreNextKnockback(livingEntity);
                    });
            DragonCircleEntity dragonFire = new DragonCircleEntity(this.level());
            dragonFire.setOwner(getOwner());
            dragonFire.setDuration(100);
            dragonFire.setDamage(damage * .1f);
            dragonFire.setRadius(radius);
            dragonFire.setCircular();
            dragonFire.moveTo(targetArea);
            this.level().addFreshEntity(dragonFire);
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if(!this.level().isClientSide){
            Vec3 targetArea = blockHitResult.getBlockPos().getCenter();
            var radius = 3f;
            var radiusSqr = radius * radius;
            var damage = getDamage();
            var source = ExampleSpellRegistry.MAYHEM.get().getDamageSource(this, getOwner());
            this.level().getEntitiesOfClass(LivingEntity.class, new AABB(targetArea.subtract(radius, radius, radius), targetArea.add(radius, radius, radius)),
                            livingEntity -> livingEntity != getOwner() &&
                                    horizontalDistanceSqr(livingEntity, targetArea) < radiusSqr &&
                                    livingEntity.isPickable() &&
                                    !DamageSources.isFriendlyFireBetween(livingEntity, getOwner()) &&
                                    Utils.hasLineOfSight(this.level(), targetArea.add(0, 1.5, 0), livingEntity.getBoundingBox().getCenter(), true))
                    .forEach(livingEntity -> {
                        DamageSources.applyDamage(livingEntity, damage, source);
                        DamageSources.ignoreNextKnockback(livingEntity);
                    });
            DragonCircleEntity dragonFire = new DragonCircleEntity(this.level());
            dragonFire.setOwner(getOwner());
            dragonFire.setDuration(100);
            dragonFire.setDamage(damage * .1f);
            dragonFire.setRadius(radius);
            dragonFire.setCircular();
            dragonFire.moveTo(targetArea);
            this.level().addFreshEntity(dragonFire);
            discard();
        }
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
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.DRAGON_FIREBALL_EXPLODE));
    }
}
