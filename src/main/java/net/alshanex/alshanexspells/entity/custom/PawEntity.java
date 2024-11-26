package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.registry.ExampleSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class PawEntity extends AbstractMagicProjectile implements GeoEntity {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PawEntity.class, EntityDataSerializers.INT);
    private UUID targetUUID;
    private Entity cachedTarget;
    private float radius;

    public PawEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public PawEntity(Level pLevel, LivingEntity pShooter, int color, LivingEntity target, float radius) {
        this(ModEntities.PAW_ENTITY.get(), pLevel);
        this.setOwner(pShooter);
        this.setColor(color);
        this.setTarget(target);
        this.radius = radius;
        this.played = false;

        this.setNoGravity(color == 0);
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
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return pTarget != getOwner() && pTarget != getTarget() && super.canHitEntity(pTarget);
    }

    public void setRotation(float x, float y) {
        this.setXRot(x);
        this.xRotO = x;
        this.setYRot(y);
        this.yRotO = y;
    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level().isClientSide) {
            if(hitResult instanceof EntityHitResult entityHitResult){
                Entity hitted = entityHitResult.getEntity();
                if(hitted instanceof LivingEntity livingEntity){
                    double knockbackStrength = 3.0;
                    Vec3 knockback = new Vec3(
                            -Mth.sin(this.getYRot() * (float) (Math.PI / 180)),
                            0.0,
                            Mth.cos(this.getYRot() * (float) (Math.PI / 180))
                    ).normalize().scale(knockbackStrength);

                    livingEntity.push(-knockback.x, 0.1, -knockback.z);
                }
            }

            impactParticles(xOld, yOld, zOld);
            float explosionRadius = this.radius;
            var explosionRadiusSqr = explosionRadius * explosionRadius;
            var entities = level().getEntities(this, this.getBoundingBox().inflate(explosionRadius));
            Vec3 losPoint = Utils.raycastForBlock(level(), this.position(), this.position().add(0, 2, 0), ClipContext.Fluid.NONE).getLocation();
            for (Entity entity : entities) {
                double distanceSqr = entity.distanceToSqr(hitResult.getLocation());
                if (distanceSqr < explosionRadiusSqr && canHitEntity(entity) && Utils.hasLineOfSight(level(), losPoint, entity.getBoundingBox().getCenter(), true)) {
                    double p = (1 - distanceSqr / explosionRadiusSqr);
                    float damage = (float) (this.damage * p);
                    DamageSources.applyDamage(entity, damage, ExampleSpellRegistry.PAW.get().getDamageSource(this, getOwner()));
                }
            }
            if(getColor() == 0){
                MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(SchoolRegistry.EVOCATION.get().getTargetingColor(), radius), hitResult.getLocation().x, hitResult.getLocation().y + .165f, hitResult.getLocation().z, 1, 0, 0, 0, 0, true);
            } else {
                MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(SchoolRegistry.BLOOD.get().getTargetingColor(), radius), hitResult.getLocation().x, hitResult.getLocation().y + .165f, hitResult.getLocation().z, 1, 0, 0, 0, 0, true);
            }
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 4.0F, (1.0F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2F) * 0.7F);
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(COLOR, 0);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setColor(compound.getInt("Color"));
        if (compound.hasUUID("Target")) {
            this.targetUUID = compound.getUUID("Target");
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Color", this.getColor());
        if (this.targetUUID != null) {
            compound.putUUID("Target", this.targetUUID);
        }

    }

    public int getColor() {
        return Mth.clamp(this.getEntityData().get(COLOR).intValue(), 0, 1);
    }

    public void setColor(int color) {
        this.getEntityData().set(COLOR, color);
    }

    @Override
    public float getSpeed() {
        return .2f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(Holder.direct(SoundEvents.GENERIC_EXPLODE.value()));
    }

    private boolean played = false;

    private PlayState animationPredicate(software.bernie.geckolib.animation.AnimationState event) {
        var controller = event.getController();

        if (!played) {
            controller.setAnimation(ANIMATION);
            played = true;
        } else {
            controller.setAnimation(ANIMATION_LOOP);
        }

        return PlayState.CONTINUE;
    }

    private final RawAnimation ANIMATION = RawAnimation.begin().thenPlay("spawn");
    private final RawAnimation ANIMATION_LOOP = RawAnimation.begin().thenLoop("loop");

    private final AnimationController controller = new AnimationController(this, "paw_controller", 0, this::animationPredicate);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
}
