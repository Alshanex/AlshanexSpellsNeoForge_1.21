package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FlowerEntity  extends AbstractMagicProjectile {

    private UUID targetUUID;
    private Entity cachedTarget;
    private float healAmount;

    public FlowerEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public FlowerEntity(Level pLevel, LivingEntity owner, LivingEntity target, float heal_amount) {
        this(ModEntities.SPORE_BLOSSOM_ENTITY.get(), pLevel);
        this.healAmount = heal_amount;
        this.setOwner(owner);
        this.setTarget(target);
    }

    int airTime;

    public void setAirTime(int airTimeInTicks) {
        airTime = airTimeInTicks;
    }

    public void setTarget(@Nullable Entity pOwner) {
        if (pOwner != null) {
            this.targetUUID = pOwner.getUUID();
            this.cachedTarget = pOwner;
        }

    }

    @Nullable
    public Entity getTarget() {
        if (this.cachedTarget != null && !this.cachedTarget.isRemoved()) {
            return this.cachedTarget;
        } else if (this.targetUUID != null && this.level() instanceof ServerLevel) {
            this.cachedTarget = ((ServerLevel) this.level()).getEntity(this.targetUUID);
            return this.cachedTarget;
        } else {
            return null;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetUUID != null) {
            tag.putUUID("Target", this.targetUUID);
        }
        if (this.airTime > 0) {
            tag.putInt("airTime", airTime);
        }
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) {
            this.targetUUID = tag.getUUID("Target");
        }
        if (tag.contains("airTime")) {
            this.airTime = tag.getInt("airTime");
        }
    }

    @Override
    public void trailParticles() {
        for (int i = 0; i < 1; i++) {
            Vec3 random = new Vec3(
                    Utils.getRandomScaled(this.getBbWidth() * .5f),
                    0,
                    Utils.getRandomScaled(this.getBbWidth() * .5f)
            );
            level().addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, getX() + random.x, getY() + .5f, getZ() + random.z, 0, -.05, 0);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        float rotationSpeed = 2.0f;
        this.setYRot(this.getYRot() + rotationSpeed);

        if (this.getYRot() >= 360.0f) {
            this.setYRot(this.getYRot() - 360.0f);
        }

        this.yRotO = this.getYRot();

        this.firstTick = false;
        xo = getX();
        yo = getY();
        zo = getZ();
        xOld = getX();
        yOld = getY();
        zOld = getZ();

        if (airTime-- > 0) {
            if (getTarget() != null) {
                var target = getTarget();
                if(target.isOnFire()){
                    target.extinguishFire();
                }
                if(target instanceof LivingEntity livingEntity){
                    for (MobEffectInstance effect : livingEntity.getActiveEffects()) {
                        if (!(effect.getEffect() == MobEffects.POISON || effect.getEffect() == MobEffects.WITHER || effect.getEffect() == MobEffects.DIG_SLOWDOWN || effect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN || effect.getEffect() == MobEffects.WEAKNESS || effect.getEffect() == MobEffects.BLINDNESS || effect.getEffect() == MobEffects.DARKNESS || effect.getEffect() == MobEffects.INFESTED || effect.getEffect() == MobEffects.HUNGER)) {
                            livingEntity.removeEffect(effect.getEffect());
                        }
                    }
                }
            }
        }

        if (!level().isClientSide) {
            if (airTime <= 0) {
                this.dismountTo(this.getX(), this.getY(), this.getZ());
                MagicManager.spawnParticles(level(), ParticleTypes.POOF, getX(), getY(), getZ(), 25, .4, .8, .4, .03, false);
                discard();
            }
            if (airTime-- > 0) {
                this.setDeltaMovement(getDeltaMovement().multiply(.95f, 0f, .95f));
                if (getTarget() != null) {
                    var target = getTarget();
                    if (airTime % 20 == 0 && airTime <= 200) {
                        if(target instanceof LivingEntity entity) {
                            entity.heal(healAmount / 10);
                            int count = 10;
                            float radius = 1.25f;
                            for (int i = 0; i < count; i++) {
                                double x, z;
                                double theta = Math.toRadians(360 / count) * i;
                                x = Math.cos(theta) * radius;
                                z = Math.sin(theta) * radius;
                                MagicManager.spawnParticles(level(), ParticleTypes.HEART, entity.position().x + x, entity.position().y, entity.position().z + z, 1, 0, 0, 0, 0.1, false);
                            }
                        }
                    }
                }
            }
        } else {
            if (airTime % 20 == 0 && airTime <= 200) {
                trailParticles();
            }
        }
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY));
    }
}
