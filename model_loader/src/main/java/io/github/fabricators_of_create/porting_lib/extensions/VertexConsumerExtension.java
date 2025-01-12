package io.github.fabricators_of_create.porting_lib.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;

import io.github.fabricators_of_create.porting_lib.model.IQuadTransformer;
import io.github.fabricators_of_create.porting_lib.util.client.VertexUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.nio.ByteBuffer;

public interface VertexConsumerExtension {
	private VertexConsumer self() {
		return (VertexConsumer) this;
	}

	/**
	 * Consumes an unknown {@link VertexFormatElement} as a raw int data array.
	 * <p>
	 * If the consumer needs to store the data for later use, it must copy it. There are no guarantees on immutability.
	 */
	default VertexConsumer misc(VertexFormatElement element, int... rawData) {
		return self();
	}

	/**
	 * Variant with no per-vertex shading.
	 */
	default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
		VertexUtils.putBulkData(self(), pose, bakedQuad, new float[] { 1.0F, 1.0F, 1.0F, 1.0F }, red, green, blue, alpha, new int[] { packedLight, packedLight, packedLight, packedLight }, packedOverlay, readExistingColor);
	}

	default int applyBakedLighting(int packedLight, ByteBuffer data)
	{
		int bl = packedLight & 0xFFFF;
		int sl = (packedLight >> 16) & 0xFFFF;
		int offset = IQuadTransformer.UV2 * 4; // int offset for vertex 0 * 4 bytes per int
		int blBaked = Short.toUnsignedInt(data.getShort(offset));
		int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
		bl = Math.max(bl, blBaked);
		sl = Math.max(sl, slBaked);
		return bl | (sl << 16);
	}

	default void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
		byte nx = data.get(28);
		byte ny = data.get(29);
		byte nz = data.get(30);
		if (nx != 0 || ny != 0 || nz != 0)
		{
			generated.set(nx / 127f, ny / 127f, nz / 127f);
			generated.transform(normalTransform);
		}
	}
}
