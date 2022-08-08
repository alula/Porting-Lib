package io.github.fabricators_of_create.porting_lib.mixin.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import io.github.fabricators_of_create.porting_lib.block.CullingBlockEntityIterator;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(LevelRenderer.class)
public class Opti_LevelRendererMixin {
	@Shadow
	private @Nullable Frustum capturedFrustum;

	@Shadow
	private Frustum cullingFrustum;

	@ModifyVariable(
			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;getRenderableBlockEntities()Ljava/util/List;"
					),
					to = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"
					)
			),
			at = @At("STORE")
	)
	private Iterator<BlockEntity> port_lib$wrapBlockEntityIterator(Iterator<BlockEntity> iterator) {
		return new CullingBlockEntityIterator(iterator, capturedFrustum != null ? capturedFrustum : cullingFrustum);
	}

	@Inject(
			method = "renderLevel",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/FogRenderer;setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZ)V",
					shift = At.Shift.AFTER
			)
	)
	private void port_lib$fogRenderAfter(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		FogEvents.RENDER_FOG.invoker().onFogRender(FogRenderer.FogMode.FOG_TERRAIN, camera, partialTick, Math.max(gameRenderer.getRenderDistance(), 32.0F));
	}
}
