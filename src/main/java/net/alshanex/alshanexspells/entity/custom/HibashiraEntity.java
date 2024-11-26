package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HibashiraEntity extends AoeEntity {

    private DamageSource damageSource;

    public HibashiraEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public HibashiraEntity(Level level) {
        this(ModEntities.HIBASHIRA_ENTITY.get(), level);
    }

    List<Entity> trackingEntities = new ArrayList<>();

    @Override
    public void applyEffect(LivingEntity target) {
        if (damageSource == null) {
            damageSource = new DamageSource(DamageSources.getHolderFromResource(target, ISSDamageTypes.FIRE_FIELD), this, getOwner());
        }
        target.hurt(damageSource, getDamage());
        target.setRemainingFireTicks(60);
    }

    @Override
    protected void checkHits() {
        if (level().isClientSide)
            return;
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(this.getInflation().x, 15, this.getInflation().z));
        boolean hit = false;
        var radiusSqr = getRadius();
        radiusSqr *= radiusSqr;
        for (LivingEntity target : targets) {
            if (canHitEntity(target) && (!isCircular() || target.distanceToSqr(this) < radiusSqr || (target.getY() > this.getY() && target.getY() < this.getY() + 15))) {
                applyEffect(target);
                hit = true;
            }
        }
        if (hit) {
            this.setRadius(getRadius() + radiusOnUse);
            this.duration += durationOnUse;
            onPostHit();
        }
    }

    @Override
    public void tick() {
        super.tick();
        int update = Math.max((int) (getRadius() / 2), 2);
        if (tickCount % update == 0) {
            updateTrackingEntities();
        }
        var bb = this.getBoundingBox();
        float radius = (float) (bb.getXsize()) * 2.5f;
        for (Entity entity : trackingEntities) {
            if (entity != getOwner() && !DamageSources.isFriendlyFireBetween(getOwner(), entity)) {
                Vec3 center = new Vec3(bb.getCenter().x, bb.getCenter().y + 7.5f, bb.getCenter().z);
                float distance = (float) center.distanceTo(entity.position());
                if (distance > radius) {
                    continue;
                }
                float f = 1 - distance / radius;
                float scale = f * f * f * f * .25f;

                Vec3 diff = center.subtract(entity.position()).scale(scale);
                entity.push(diff.x, diff.y, diff.z);
                entity.fallDistance = 0;
            }
        }
    }

    private void updateTrackingEntities() {
        trackingEntities = level().getEntities(this, this.getBoundingBox().inflate(this.getBbWidth() * 3, 15, this.getBbWidth() * 3));
    }

    @Override
    public float getParticleCount() {
        return 1.2f * getRadius();
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(ParticleHelper.FIRE);
    }
}
