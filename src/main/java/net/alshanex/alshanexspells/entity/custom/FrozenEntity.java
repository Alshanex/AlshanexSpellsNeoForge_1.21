package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

public class FrozenEntity extends LivingEntity {
    private static final EntityDataAccessor<String> FROZEN_ENTITY_TYPE = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<CompoundTag> FROZEN_ENTITY_DATA = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Float> FROZEN_ENTITY_WIDTH = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FROZEN_ENTITY_HEIGHT = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FROZEN_ENTITY_SCALE = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> DATA_FROZEN_SPEED = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> DATA_ATTACK_TIME = SynchedEntityData.defineId(FrozenEntity.class, EntityDataSerializers.FLOAT);

    private EntityDimensions frozenEntitySize = EntityDimensions.fixed(0.5F, 0.5F);
    private boolean isAutoSpinAttack;
    private HumanoidArm mainArm = HumanoidArm.RIGHT;
    private float shatterProjectileDamage;
    private int deathTimer = -1;
    private UUID summonerUUID;
    private LivingEntity cachedSummoner;

    public FrozenEntity(EntityType<? extends LivingEntity> t, Level worldIn) {
        super(t, worldIn);
    }

    public static AttributeSupplier.Builder frozenEntity() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.FOLLOW_RANGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    public static FrozenEntity buildFrozenEntity(LivingEntity entityToFreeze, LivingEntity summoner) {
        FrozenEntity frozenEntity = ModEntities.FROZEN_ENTITY.get().create(entityToFreeze.level());
        CompoundTag entityTag = new CompoundTag();
        try {
            if (!(entityToFreeze instanceof Player)) {
                entityToFreeze.saveWithoutId(entityTag);
            }
        } catch (Exception e) {
            AlshanexSpellsMod.LOGGER.warn("Mob " + frozenEntity.getFrozenEntityTypeString() + " could not build Frozen Entity");
        }
        frozenEntity.setFrozenEntityTag(entityTag);
        frozenEntity.setFrozenEntityTypeString(
                entityToFreeze.getType().builtInRegistryHolder().key().location().toString()
        );
        frozenEntity.setFrozenEntityWidth(entityToFreeze.getBbWidth());
        frozenEntity.setFrozenEntityHeight(entityToFreeze.getBbHeight());
        frozenEntity.setFrozenEntityScale(entityToFreeze.getScale());

        frozenEntity.setYBodyRot(entityToFreeze.yBodyRot);
        frozenEntity.yBodyRotO = frozenEntity.yBodyRot;
        frozenEntity.setYHeadRot(frozenEntity.getYHeadRot());
        frozenEntity.yHeadRotO = frozenEntity.yHeadRot;

        frozenEntity.entityData.set(DATA_ATTACK_TIME, entityToFreeze.attackAnim);
        frozenEntity.setPose(entityToFreeze.getPose());
        frozenEntity.isAutoSpinAttack = entityToFreeze.isAutoSpinAttack();
        frozenEntity.mainArm = entityToFreeze.getMainArm();

        frozenEntity.setCustomNameVisible(false);

        if (summoner != null) {
            frozenEntity.summonerUUID = summoner.getUUID();
            frozenEntity.cachedSummoner = summoner;
        }

        return frozenEntity;
    }

    public void setSummoner(@javax.annotation.Nullable LivingEntity owner) {
        if (owner != null) {
            this.summonerUUID = owner.getUUID();
            this.cachedSummoner = owner;
        }
    }

    public LivingEntity getSummoner() {
        if (this.cachedSummoner != null && this.cachedSummoner.isAlive()) {
            return this.cachedSummoner;
        } else if (this.summonerUUID != null && this.level() instanceof ServerLevel) {
            if (((ServerLevel) this.level()).getEntity(this.summonerUUID) instanceof LivingEntity livingEntity)
                this.cachedSummoner = livingEntity;
            return this.cachedSummoner;
        } else {
            return null;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FROZEN_ENTITY_TYPE, "minecraft:zombie");
        builder.define(FROZEN_ENTITY_DATA, new CompoundTag());
        builder.define(FROZEN_ENTITY_WIDTH, 0.5F);
        builder.define(FROZEN_ENTITY_HEIGHT, 0.5F);
        builder.define(FROZEN_ENTITY_SCALE, 1F);
        builder.define(DATA_FROZEN_SPEED, 0f);
        builder.define(DATA_ATTACK_TIME, 0f);
    }

    public EntityType getFrozenEntityType() {
        String str = getFrozenEntityTypeString();
        return EntityType.byString(str).orElse(EntityType.ZOMBIE);
    }

    public String getFrozenEntityTypeString() {
        return this.entityData.get(FROZEN_ENTITY_TYPE);
    }

    public void setFrozenEntityTypeString(String type) {
        this.entityData.set(FROZEN_ENTITY_TYPE, type);
    }

    public CompoundTag getFrozenEntityTag() {
        return this.entityData.get(FROZEN_ENTITY_DATA);
    }

    public void setFrozenEntityTag(CompoundTag tag) {
        this.entityData.set(FROZEN_ENTITY_DATA, tag);
    }

    public float getFrozenEntityWidth() {
        return this.entityData.get(FROZEN_ENTITY_WIDTH);
    }

    public void setFrozenEntityWidth(float width) {
        this.entityData.set(FROZEN_ENTITY_WIDTH, width);
    }

    public float getFrozenEntityHeight() {
        return this.entityData.get(FROZEN_ENTITY_HEIGHT);
    }

    public void setFrozenEntityHeight(float height) {
        this.entityData.set(FROZEN_ENTITY_HEIGHT, height);
    }

    public float getFrozenEntityScale() {
        return this.entityData.get(FROZEN_ENTITY_SCALE);
    }

    public void setFrozenEntityScale(float scale) {
        this.entityData.set(FROZEN_ENTITY_SCALE, scale);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("FrozenEntityWidth", this.getFrozenEntityWidth());
        tag.putFloat("FrozenEntityHeight", this.getFrozenEntityHeight());
        tag.putFloat("FrozenEntityScale", this.getFrozenEntityScale());
        tag.putString("FrozenEntityType", this.getFrozenEntityTypeString());
        tag.put("FrozenEntityTag", this.getFrozenEntityTag());

        if (this.summonerUUID != null) {
            tag.putUUID("Summoner", this.summonerUUID);
        }
    }

    @Override
    public float getScale() {
        return this.getFrozenEntityScale();
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFrozenEntityWidth(tag.getFloat("FrozenEntityWidth"));
        this.setFrozenEntityHeight(tag.getFloat("FrozenEntityHeight"));
        this.setFrozenEntityScale(tag.getFloat("FrozenEntityScale"));
        this.setFrozenEntityTypeString(tag.getString("FrozenEntityType"));
        if (tag.contains("FrozenEntityTag")) {
            this.setFrozenEntityTag(tag.getCompound("FrozenEntityTag"));

        }
        if (tag.hasUUID("Summoner")) {
            this.summonerUUID = tag.getUUID("Summoner");
        }
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose poseIn) {
        return frozenEntitySize;
    }

    @Override
    public void tick() {
        super.tick();

        this.setYRot(this.yBodyRot);
        this.yHeadRot = this.getYRot();
        if (Math.abs(this.getBbWidth() - getFrozenEntityWidth()) > 0.01 || Math.abs(this.getBbHeight() - getFrozenEntityHeight()) > 0.01) {
            double prevX = this.getX();
            double prevZ = this.getZ();
            this.frozenEntitySize = EntityDimensions.scalable(getFrozenEntityWidth(), getFrozenEntityHeight());
            refreshDimensions();
            this.setPos(prevX, this.getY(), prevZ);
        }

        if (deathTimer > 0) {
            deathTimer--;

        }
        if (deathTimer == 0)
            this.hurt(level().damageSources().generic(), 100);
    }

    @Override
    public void kill() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slotIn, @NotNull ItemStack stack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return mainArm;
    }


    public void setDeathTimer(int timeInTicks) {
        this.deathTimer = timeInTicks;
    }

    public float getAttacktime() {
        return this.entityData.get(DATA_ATTACK_TIME);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.GLASS_BREAK;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (level().isClientSide || this.isInvulnerableTo(pSource))
            return false;

        spawnIcicleShards(this.getEyePosition(), this.shatterProjectileDamage);
        this.playHurtSound(pSource);
        this.discard();
        return true;
    }

    private void spawnIcicleShards(Vec3 origin, float damage) {
        int count = 8;
        int offset = 360 / count;
        for (int i = 0; i < count; i++) {

            Vec3 motion = new Vec3(0, 0, 0.55);
            motion = motion.xRot(30 * Mth.DEG_TO_RAD);
            motion = motion.yRot(offset * i * Mth.DEG_TO_RAD);


            IcicleProjectile shard = new IcicleProjectile(level(), getSummoner());
            shard.setDamage(damage);
            shard.setDeltaMovement(motion);

            Vec3 spawn = origin.add(motion.multiply(1, 0, 1).normalize().scale(.5f));
            var angle = Utils.rotationFromDirection(motion);

            shard.moveTo(spawn.x, spawn.y - shard.getBoundingBox().getYsize() / 2, spawn.z, angle.y, angle.x);
            level().addFreshEntity(shard);
        }
    }

    public void setShatterDamage(float damage) {
        this.shatterProjectileDamage = damage;
    }

    @Override
    public boolean isAutoSpinAttack() {
        return this.isAutoSpinAttack;
    }
}
