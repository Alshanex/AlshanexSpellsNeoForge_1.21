package net.alshanex.alshanexspells.event;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.alshanex.alshanexspells.AlshanexSpellsMod;
import net.alshanex.alshanexspells.block.ModBlocks;
import net.alshanex.alshanexspells.effect.ModEffects;
import net.alshanex.alshanexspells.item.ModItems;
import net.alshanex.alshanexspells.util.AUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ModEvents {

    @EventBusSubscriber
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
                if (entityHitResult.getEntity() instanceof ServerPlayer player) {
                    MagicData magicData = MagicData.getPlayerMagicData(player);
                    if (AUtils.hasItemInSpellbookSlot(player, ModItems.MERAMERA.get()) && magicData.getMana() >= 10) {
                        magicData.setMana(magicData.getMana() - 10);
                        PacketDistributor.sendToPlayer(player, new SyncManaPacket(magicData));
                        if (player.level() instanceof ServerLevel serverLevel) {

                            Vec3 projectileDirection = event.getProjectile().getDeltaMovement().normalize();

                            double newPosX = event.getProjectile().getX() + projectileDirection.x;
                            double newPosY = event.getProjectile().getY() + projectileDirection.y;
                            double newPosZ = event.getProjectile().getZ() + projectileDirection.z;

                            for (int i = 0; i < 20; i++) {
                                double offsetX = (Math.random() - 0.5) * 0.5;
                                double offsetY = (Math.random() - 0.5) * 0.5;
                                double offsetZ = (Math.random() - 0.5) * 0.5;

                                serverLevel.sendParticles(ParticleTypes.FLAME,
                                        newPosX + offsetX,
                                        newPosY + offsetY,
                                        newPosZ + offsetZ,
                                        1, 0, 0, 0, 0);
                            }
                        }
                        event.getProjectile().setRemainingFireTicks(100);
                        event.setCanceled(true);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEntityAttacked(LivingDamageEvent.Pre event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                MagicData magicData = MagicData.getPlayerMagicData(player);
                if(AUtils.hasItemInSpellbookSlot(player, ModItems.HIEHIE.get()) && magicData.getMana() >= 20 && !(AUtils.isFireDamage(event.getSource().type()))){
                    BlockPos currentBlockPos = player.blockPosition();
                    BlockPos blockBelowPos = player.blockPosition().below();
                    Block blockBelow = player.level().getBlockState(blockBelowPos).getBlock();
                    Block currentBlock = player.level().getBlockState(currentBlockPos).getBlock();
                    if (AUtils.isIceOrSnow(blockBelow) || currentBlock == ModBlocks.ICE_SURFACE_BLOCK.get()) {
                        Vec3 attackDirection = event.getSource().getSourcePosition();
                        if (attackDirection != null) {
                            FrozenHumanoid shadow = new FrozenHumanoid(player.level(), player);
                            shadow.setShatterDamage(Math.max(event.getOriginalDamage() * 0.1f, 5f));
                            shadow.setDeathTimer(2);
                            player.level().addFreshEntity(shadow);
                            MagicManager.spawnParticles(player.level(), ParticleHelper.ICY_FOG, player.getX(), player.getY(), player.getZ(), 4, 0, 0, 0, .3, true);

                            Vec3 knockbackDirection = player.position().subtract(attackDirection).normalize();
                            double knockbackStrength = 1.5;

                            player.setDeltaMovement(player.getDeltaMovement().add(knockbackDirection.scale(knockbackStrength)));
                            player.hurtMarked = true;

                            magicData.setMana(magicData.getMana() - 20);
                            PacketDistributor.sendToPlayer(player, new SyncManaPacket(magicData));
                            event.setNewDamage(0);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Pre event) {
            if(event.getEntity() instanceof ServerPlayer player){
                if(AUtils.hasItemInSpellbookSlot(player, ModItems.MERAMERA.get())){
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2, 0, false, false));
                }

                if (AUtils.hasItemInSpellbookSlot(player, ModItems.HIEHIE.get())) {
                    Level world = event.getEntity().level();

                    BlockPos playerPos = event.getEntity().blockPosition();

                    if (event.getEntity().onGround()) {
                        int radius = 2;
                        for (BlockPos blockPos : BlockPos.betweenClosed(playerPos.offset(-radius, -1, -radius), playerPos.offset(radius, -1, radius))) {
                            BlockState blockState = world.getBlockState(blockPos);
                            if (blockState.getBlock() == Blocks.WATER) {
                                world.setBlockAndUpdate(blockPos, Blocks.FROSTED_ICE.defaultBlockState());
                                world.scheduleTick(blockPos, Blocks.FROSTED_ICE, 120);
                            }
                        }
                    }
                }


                if(player.hasEffect(ModEffects.ENCORE.getDelegate())){
                    Vec3 freezePosition = player.position();
                    player.teleportTo(freezePosition.x, freezePosition.y, freezePosition.z);

                    player.getAbilities().mayfly = false;
                    if (player.onGround()) {
                        player.setDeltaMovement(new Vec3(0, 0, 0));
                    }
                }
            }
        }
    }
}
