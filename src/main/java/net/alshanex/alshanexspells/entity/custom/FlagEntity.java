package net.alshanex.alshanexspells.entity.custom;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.network.particles.ShockwaveParticlesPacket;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.entity.ModEntities;
import net.alshanex.alshanexspells.util.CylinderParticleManager;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FlagEntity extends LivingEntity implements GeoEntity, AntiMagicSusceptible {

    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;
    private float radius = 5;
    public int age;
    private List<UUID> players = new ArrayList<>();

    private final AttributeModifier holyModifier = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID,"holy_spell_power_modifier"), .25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private final AttributeModifier bloodModifier = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(AlshanexSpellsMod.MODID,"blood_spell_power_modifier"), -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    public FlagEntity(EntityType<? extends FlagEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public FlagEntity(Level level, LivingEntity owner, float radius) {
        this(ModEntities.FLAG.get(), level);
        setOwner(owner);
        setRadius(radius);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if(getOwner() != null){
            double yawRadians = Math.toRadians(getOwner().getYRot());

            double offsetX = -Math.sin(yawRadians) * 0.5;
            double offsetZ = Math.cos(yawRadians) * 0.5;
            double newX = getOwner().getX() + offsetX;
            double newY = getOwner().getY();
            double newZ = getOwner().getZ() + offsetZ;

            this.setPos(newX, newY, newZ);
            this.setYRot(getOwner().getYRot());
        }

        if (!level().isClientSide) {
            if (age > 280) {
                MagicManager.spawnParticles(level(), ParticleTypes.POOF, this.getX(), this.getY() + this.getBbHeight()/2, this.getZ(), 25, .4, .8, .4, .03, false);
                this.discard();
            } else {
                if (age < 280 && (age) % 10 == 0){
                    CylinderParticleManager.spawnParticles(level(), this.owner, 150, ParticleRegistry.WISP_PARTICLE.get(), CylinderParticleManager.ParticleDirection.UPWARD, this.radius, 5, 0);
                }
                if (age < 280 && (age) % 40 == 0) {
                    MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(SchoolRegistry.HOLY.get().getTargetingColor(), radius), this.getX(), this.getY() + .165f, this.getZ(), 1, 0, 0, 0, 0, true);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(getOwner(), new ShockwaveParticlesPacket(new Vec3(this.getX(), this.getY() + .165f, this.getZ()), radius, ParticleRegistry.WISP_PARTICLE.get()));
                    level().getEntities(this, this.getBoundingBox().inflate(radius, 4, radius), (target) -> Utils.hasLineOfSight(level(), this, target, true)).forEach(target -> {
                        if (target instanceof LivingEntity livingEntity && livingEntity.distanceToSqr(this) < radius * radius) {
                            if(!DamageSources.isFriendlyFireBetween(target, getOwner())){
                                Holder<EntityType<?>> holder = livingEntity.getType().builtInRegistryHolder();
                                if(holder.is(EntityTypeTags.UNDEAD)){
                                    livingEntity.setRemainingFireTicks(60);
                                }
                                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 42, 2));
                                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 42, 2));
                            } else {
                                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 42, 1));
                                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 42, 2));
                                livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 42, 2));
                            }
                            if(livingEntity instanceof ServerPlayer player){

                                if(!player.getAttributes().hasModifier(AttributeRegistry.HOLY_SPELL_POWER.getDelegate(), holyModifier.id())){
                                    AttributeInstance holyPower = player.getAttribute(AttributeRegistry.HOLY_SPELL_POWER.getDelegate());
                                    holyPower.addTransientModifier(holyModifier);
                                }
                                if(!player.getAttributes().hasModifier(AttributeRegistry.BLOOD_SPELL_POWER.getDelegate(), bloodModifier.id())){
                                    AttributeInstance bloodPower = player.getAttribute(AttributeRegistry.BLOOD_SPELL_POWER.getDelegate());
                                    bloodPower.addTransientModifier(bloodModifier);
                                }

                                if(!players.contains(player)){
                                    players.add(player.getUUID());
                                }
                            }
                        }
                    });
                }
            }
        }
        age++;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }



    @Override
    public void onRemovedFromLevel() {
        for (UUID playerID : players) {
            Player player = level().getPlayerByUUID(playerID);
            if (player != null) {
                player.getAttribute(AttributeRegistry.HOLY_SPELL_POWER.getDelegate()).removeModifier(holyModifier.id());
                player.getAttribute(AttributeRegistry.BLOOD_SPELL_POWER.getDelegate()).removeModifier(bloodModifier.id());
            }
        }
        players.clear();
        super.onRemovedFromLevel();
    }

    public void setOwner(@Nullable LivingEntity pOwner) {
        this.owner = pOwner;
        this.ownerUUID = pOwner == null ? null : pOwner.getUUID();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel) this.level()).getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }

        return this.owner;
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.age = pCompound.getInt("Age");
        if (pCompound.hasUUID("Owner")) {
            this.ownerUUID = pCompound.getUUID("Owner");
        }
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Age", this.age);
        if (this.ownerUUID != null) {
            pCompound.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        MagicManager.spawnParticles(level(), ParticleTypes.SMOKE, getX(), getY() + 1, getZ(), 50, .2, 1.25, .2, .08, false);
        this.discard();
    }

    private PlayState animationPredicate(software.bernie.geckolib.animation.AnimationState event) {

        var controller = event.getController();

        if (controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(ANIMATION_IDLE);
        }

        return PlayState.CONTINUE;
    }

    private PlayState risePredicate(software.bernie.geckolib.animation.AnimationState event) {

        var controller = event.getController();

        if (age < 10) {
            controller.setAnimation(ANIMATION_RISE);
            return PlayState.CONTINUE;
        } else
            return PlayState.STOP;


    }


    private final RawAnimation ANIMATION_RISE = RawAnimation.begin().thenPlay("flag_spawn");
    private final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("flag_loop");

    private final AnimationController controller = new AnimationController(this, "flag_controller", 20, this::animationPredicate);
    private final AnimationController riseController = new AnimationController(this, "flag_rise_controller", 0, this::risePredicate);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(riseController);
        controllerRegistrar.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
}
