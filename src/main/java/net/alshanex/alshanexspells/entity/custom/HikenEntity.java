package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.network.particles.FieryExplosionParticlesPacket;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.registry.ExampleSpellRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class HikenEntity extends AbstractMagicProjectile {
    public HikenEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public HikenEntity(Level pLevel, LivingEntity pShooter) {
        this(ModEntities.HIKEN_ENTITY.get(), pLevel);
        this.setOwner(pShooter);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 1.15f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(Holder.direct(SoundEvents.GENERIC_EXPLODE.value()));
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level().isClientSide) {
            impactParticles(xOld, yOld, zOld);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                if (ServerConfigs.SPELL_GREIFING.get()) {
                    Explosion explosion = new Explosion(
                            level(),
                            null,
                            ExampleSpellRegistry.HIKEN.get().getDamageSource(this, getOwner()),
                            null,
                            this.getX(), this.getY(), this.getZ(),
                            this.getExplosionRadius() / 2,
                            true,
                            Explosion.BlockInteraction.DESTROY,
                            ParticleTypes.EXPLOSION,
                            ParticleTypes.EXPLOSION_EMITTER,
                            SoundEvents.GENERIC_EXPLODE);
                    if (!NeoForge.EVENT_BUS.post(new ExplosionEvent.Start(level(), explosion)).isCanceled()) {
                        explosion.explode();
                        explosion.finalizeExplosion(false);
                    }
                }
                List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0D));

                for (LivingEntity targetEntity : entitiesInRange) {
                    if (targetEntity != this.getOwner() && targetEntity.isAlive()) {
                        targetEntity.hurt(ExampleSpellRegistry.HIKEN.get().getDamageSource(this, getOwner()), this.getDamage());

                        Vec3 knockbackDirection = targetEntity.position().subtract(this.position()).normalize().scale(1);
                        targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(knockbackDirection));

                        targetEntity.setRemainingFireTicks(200);
                    }
                }
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(getOwner(), new FieryExplosionParticlesPacket(new Vec3(getX(), getY() + .15f, getZ()), getExplosionRadius()));
                playSound(SoundEvents.BLAZE_SHOOT, 4.0F, (1.0F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2F) * 0.7F);
                this.discard();
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                List<LivingEntity> entitiesInRange = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.0D));

                for (LivingEntity targetEntity : entitiesInRange) {
                    if (targetEntity != this.getOwner() && targetEntity.isAlive()) {
                        targetEntity.hurt(ExampleSpellRegistry.HIKEN.get().getDamageSource(this, getOwner()), this.getDamage());

                        Vec3 knockbackDirection = targetEntity.position().subtract(this.position()).normalize().scale(1);
                        targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(knockbackDirection));

                        targetEntity.setRemainingFireTicks(200);
                    }
                }
            }
        }
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = getDeltaMovement();
        double d0 = this.getX() - vec3.x;
        double d1 = this.getY() - vec3.y;
        double d2 = this.getZ() - vec3.z;
        var count = Mth.clamp((int) (vec3.lengthSqr() * 4), 1, 4);
        for (int i = 0; i < count; i++) {
            Vec3 random = Utils.getRandomVec3(.25);
            var f = i / ((float) count);
            var x = Mth.lerp(f, d0, this.getX());
            var y = Mth.lerp(f, d1, this.getY());
            var z = Mth.lerp(f, d2, this.getZ());
            this.level().addParticle(ParticleHelper.EMBERS, x - random.x, y + 0.5D - random.y, z - random.z, random.x * .5f, random.y * .5f, random.z * .5f);
        }
    }

    @Override
    public void tick() {
        super.tick();

        createParticleSphere();
    }

    private void createParticleSphere() {
        double radius = this.getBbWidth() + 1;

        int particleCount = 15;
        for (int i = 0; i < particleCount; i++) {
            double theta = Math.toRadians(this.level().random.nextDouble() * 360);
            double phi = Math.toRadians(this.level().random.nextDouble() * 180);

            double randomRadius = radius * Math.pow(this.level().random.nextDouble(), 1.0 / 3.0);

            double xOffset = randomRadius * Math.sin(phi) * Math.cos(theta);
            double yOffset = randomRadius * Math.cos(phi);
            double zOffset = randomRadius * Math.sin(phi) * Math.sin(theta);

            this.level().addParticle(ParticleHelper.FIRE,
                    this.getX() + xOffset,
                    this.getY() + yOffset,
                    this.getZ() + zOffset,
                    0, 0, 0);
        }
    }
}
