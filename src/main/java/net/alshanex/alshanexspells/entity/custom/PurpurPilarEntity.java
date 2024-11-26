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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PurpurPilarEntity extends AbstractMagicProjectile {

    public PurpurPilarEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public PurpurPilarEntity(Level pLevel, LivingEntity owner) {
        this(ModEntities.PURPUR_PILAR_ENTITY.get(), pLevel);
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

        double knockbackStrength = 1.3;
        Vec3 knockback = new Vec3(
                -Mth.sin(this.getYRot() * (float) (Math.PI / 180)),
                0.0,
                Mth.cos(this.getYRot() * (float) (Math.PI / 180))
        ).normalize().scale(knockbackStrength);

        targetEntity.push(-knockback.x, 0.1, -knockback.z);

        DamageSources.applyDamage(targetEntity, getDamage(), ExampleSpellRegistry.MAYHEM.get().getDamageSource(this, getOwner()));

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
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.STONE_HIT));
    }
}
