package io.github.fabricators_of_create.porting_lib.mixin.common;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import io.github.fabricators_of_create.porting_lib.block.CustomFrictionBlock;
import io.github.fabricators_of_create.porting_lib.block.CustomLandingEffectsBlock;
import io.github.fabricators_of_create.porting_lib.event.LivingEntityEvents;
import io.github.fabricators_of_create.porting_lib.extensions.EntityExtensions;
import io.github.fabricators_of_create.porting_lib.item.EntitySwingListenerItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	@Shadow
	protected Player lastHurtByPlayer;

	@Shadow
	public abstract ItemStack getItemInHand(InteractionHand interactionHand);

	@Unique
	private InteractionResultHolder<Float> port_lib$hurtResult = null;

	public LivingEntityMixin(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void port_lib$spawnDropsHEAD(DamageSource source, CallbackInfo ci) {
		((EntityExtensions) this).port_lib$captureDrops(new ArrayList<>());
	}

	@ModifyArgs(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropCustomDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;IZ)V"))
	private void port_lib$modifyLootingLevel(Args args, DamageSource source) {
		int currentLevel = args.get(1);
		int modifiedLevel = LivingEntityEvents.LOOTING_LEVEL.invoker().modifyLootingLevel(source, (LivingEntity) (Object) this, currentLevel);
		if (modifiedLevel != currentLevel) {
			args.set(1, modifiedLevel);
		}
	}

	@Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
	private void port_lib$spawnDrops(DamageSource source, CallbackInfo ci) {
		Collection<ItemEntity> drops = ((EntityExtensions) this).port_lib$captureDrops(null);
		if (!LivingEntityEvents.DROPS.invoker().onLivingEntityDrops(source, drops))
			drops.forEach(e -> level.addFreshEntity(e));
	}

	@Environment(EnvType.CLIENT)
	@Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
	private void port_lib$swingHand(InteractionHand hand, boolean bl, CallbackInfo ci) {
		ItemStack stack = getItemInHand(hand);
		if (!stack.isEmpty() && stack.getItem() instanceof EntitySwingListenerItem listener &&
				listener.onEntitySwing(stack, (LivingEntity) (Object) this)) {
			ci.cancel();
		}
	}

	@ModifyArgs(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
	private void port_lib$dropExperience(Args args) {
		int amount = args.get(2);
		int newAmount = LivingEntityEvents.EXPERIENCE_DROP.invoker().onLivingEntityExperienceDrop(amount, lastHurtByPlayer);
		if (amount != newAmount) args.set(2, newAmount);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void port_lib$tick(CallbackInfo ci) {
		LivingEntityEvents.TICK.invoker().onLivingEntityTick((LivingEntity) (Object) this);
	}

	@ModifyVariable(method = "knockback", at = @At("STORE"), ordinal = 0, argsOnly = true)
	private double port_lib$takeKnockback(double f) {
		if (lastHurtByPlayer != null)
			return LivingEntityEvents.KNOCKBACK_STRENGTH.invoker().onLivingEntityTakeKnockback(f, lastHurtByPlayer);

		return f;
	}

	@ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
	private float port_lib$onHurt(float amount, DamageSource source, float amountAgainBecauseWeNeedItTwiceSomeReason) {
		port_lib$hurtResult = LivingEntityEvents.ATTACK.invoker().onAttack(source, (LivingEntity) (Object) this, amount);
		if (port_lib$hurtResult == null)
			return amount;
		return port_lib$hurtResult.getObject();
	}

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void port_lib$cancelOnHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (port_lib$hurtResult != null) {
			if (port_lib$hurtResult.getResult() == InteractionResult.FAIL) {
				cir.setReturnValue(false);
			}
		}
	}

	@ModifyArgs(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
	private void port_lib$onHurt(Args args) {
		DamageSource source = args.get(0);
		float currentAmount = args.get(1);
		float newAmount = LivingEntityEvents.ACTUALLY_HURT.invoker().onHurt(source, (LivingEntity) (Object) this, currentAmount);
		if (newAmount != currentAmount)
			args.set(1, newAmount);
	}

	@ModifyArgs(
			method = "checkFallDamage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
			)
	)
	protected void port_lib$updateFallState(Args args, double y, boolean onGround, BlockState state, BlockPos pos) {
		int count = args.get(4);
		if (state.getBlock() instanceof CustomLandingEffectsBlock custom) {
			if (custom.addLandingEffects(state, (ServerLevel) level, pos, state, (LivingEntity) (Object) this, count)) {
				super.checkFallDamage(y, onGround, state, pos);
				args.set(4, 0); // spawn 0 particles
			}
		}
	}

	@ModifyVariable(
			method = "travel",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getBlockPosBelowThatAffectsMyMovement()Lnet/minecraft/core/BlockPos;")),
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"), ordinal = 0
	)
	@SuppressWarnings("InvalidInjectorMethodSignature")
	private float port_lib$setFriction(float original) { // shut, MCDev
		BlockPos pos = new BlockPos(getX(), getY() - 1, getZ());
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof CustomFrictionBlock custom) {
			return custom.getFriction(state, level, pos, this);
		}
		return original;
	}

	@Inject(method = "getVisibilityPercent", at = @At(value = "TAIL"), cancellable = true)
	private void port_lib$modifyVisibility(@Nullable Entity lookingEntity, CallbackInfoReturnable<Double> cir) {
		double current = cir.getReturnValue();
		double newVis = LivingEntityEvents.VISIBILITY.invoker().modifyVisibility((LivingEntity) (Object) this, lookingEntity, current);
		if (current != newVis) {
			cir.setReturnValue(newVis);
		}
	}
}
