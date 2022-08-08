package io.github.fabricators_of_create.porting_lib.mixin.client.compat;

import io.github.fabricators_of_create.porting_lib.event.client.FOVModifierCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class Opti_GameRendererMixin {
	@Inject(method = "getFov", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void port_lib$modifyFOV(Camera activeRenderInfo, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir, double oldFov) {
		double newFov = FOVModifierCallback.PARTIAL_FOV.invoker().getNewFOV((GameRenderer) (Object) this, activeRenderInfo, partialTicks, oldFov);
		if (newFov != oldFov)
			cir.setReturnValue(newFov);
	}
}
