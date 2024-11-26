package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.ball_lightning.BallLightning;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MegidoEntity extends AbstractMagicProjectile {
    List<Entity> lastVictims;
    Entity initialVictim;
    public int maxConnections = Integer.MAX_VALUE;
    public float range = 5f;
    private final static Supplier<AbstractSpell> SPELL = SpellRegistry.CHAIN_LIGHTNING_SPELL;

    public MegidoEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        lastVictims = new ArrayList<>();
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public MegidoEntity(Level level, Entity owner, Entity initialVictim) {
        this(EntityRegistry.CHAIN_LIGHTNING.get(), level);
        this.setOwner(owner);
        this.setPos(initialVictim.position());
        this.initialVictim = initialVictim;
    }

    int hits;

    @Override
    public void tick() {
        super.tick();
        int f = tickCount - 1;
        if (!this.level().isClientSide && f % 6 == 0) {
            if (f == 0) {
                //First time zap
                doHurt(initialVictim);
                if (getOwner() != null) {
                    Vec3 start = getOwner().position().add(0, getOwner().getBbHeight() / 2, 0);
                    var dest = initialVictim.position().add(0, initialVictim.getBbHeight() / 2, 0);
                    ((ServerLevel) level()).sendParticles(new ZapParticleOption(dest), start.x, start.y, start.z, 1, 0, 0, 0, 0);
                }

            } else if (f < 70){
                var entity = lastVictims.get(lastVictims.size() - 1);
                Entity victim;
                if(entity instanceof BallLightning){
                    victim = getClosestEntity(entity, range);
                } else {
                    victim = getRandomBall(entity, range);
                }
                if(victim != null){
                    if (hits < maxConnections && victim.distanceToSqr(entity) < range * range) {
                        doHurt(victim);
                        victim.playSound(SoundRegistry.CHAIN_LIGHTNING_CHAIN.get(), 2, 1);
                        Vec3 start = new Vec3(entity.xOld, entity.yOld, entity.zOld).add(0, entity.getBbHeight() / 2, 0);
                        var dest = victim.position().add(0, victim.getBbHeight() / 2, 0);
                        ((ServerLevel) level()).sendParticles(new ZapParticleOption(dest), start.x, start.y, start.z, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    public Entity getClosestEntity(Entity baseEntity, double range) {
        List<Entity> nearbyEntities = baseEntity.level().getEntities(baseEntity, baseEntity.getBoundingBox().inflate(range), entity -> entity != baseEntity && canHitEntity(entity));
        Random random = new Random();

        Entity closestNonBallLightning = nearbyEntities.stream()
                .filter(entity -> !(entity instanceof BallLightning))
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(baseEntity)))
                .orElse(null);

        if (closestNonBallLightning == null) {
            List<Entity> ballLightnings = nearbyEntities.stream()
                    .filter(entity -> entity instanceof BallLightning)
                    .toList();

            if (!ballLightnings.isEmpty()) {
                return ballLightnings.get(random.nextInt(ballLightnings.size()));
            }
        }

        return closestNonBallLightning;
    }

    public Entity getRandomBall(Entity baseEntity, double range) {
        List<Entity> nearbyEntities = baseEntity.level().getEntities(baseEntity, baseEntity.getBoundingBox().inflate(range), entity -> entity != baseEntity && canHitEntity(entity));
        Random random = new Random();

        List<Entity> ballLightnings = nearbyEntities.stream()
                .filter(entity -> entity instanceof BallLightning)
                .toList();

        if (!ballLightnings.isEmpty()) {
            return ballLightnings.get(random.nextInt(ballLightnings.size()));
        }

        return nearbyEntities.stream()
                .filter(entity -> !(entity instanceof BallLightning))
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(baseEntity)))
                .orElse(null);
    }

    public void doHurt(Entity victim) {
        hits++;
        if(!(victim instanceof BallLightning)){
            DamageSources.applyDamage(victim, damage, SPELL.get().getDamageSource(this, getOwner()));
        }
        MagicManager.spawnParticles(level(), ParticleHelper.ELECTRICITY, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 10, victim.getBbWidth() / 3, victim.getBbHeight() / 3, victim.getBbWidth() / 3, 0.1, false);
        lastVictims.add(victim);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return (target instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(target, getOwner()) && target != getOwner() && super.canHitEntity(target)) || target instanceof BallLightning;
    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY));
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
