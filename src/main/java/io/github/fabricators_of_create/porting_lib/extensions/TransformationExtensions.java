package io.github.fabricators_of_create.porting_lib.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.core.Direction;

public interface TransformationExtensions {
	/**
	 * Apply this transformation to a different origin.
	 * Can be used for switching between coordinate systems.
	 * Parameter is relative to the current origin.
	 */
	Transformation applyOrigin(Vector3f origin);

	Matrix3f getNormalMatrix();

	void push(PoseStack stack);

	void transformPosition(Vector4f position);

	Direction rotateTransform(Direction facing);

	default boolean isIdentity() {
		return this.equals(Transformation.identity());
	}

	default void transformNormal(Vector3f normal) {
		normal.transform(getNormalMatrix());
		normal.normalize();
	}

	/**
	 * convert transformation from assuming center-block system to opposing-corner-block system
	 */
	default Transformation blockCenterToCorner() {
		return applyOrigin(new Vector3f(.5f, .5f, .5f));
	}

	/**
	 * convert transformation from assuming opposing-corner-block system to center-block system
	 */
	default Transformation blockCornerToCenter() {
		return applyOrigin(new Vector3f(-.5f, -.5f, -.5f));
	}
}
