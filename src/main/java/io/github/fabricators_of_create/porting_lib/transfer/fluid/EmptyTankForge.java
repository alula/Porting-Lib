package io.github.fabricators_of_create.porting_lib.transfer.fluid;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;

public class EmptyTankForge extends FluidTankForge {
	public static final EmptyTankForge INSTANCE = new EmptyTankForge();

	private EmptyTankForge() {
		super(FluidStack.EMPTY, 0);
	}

	@Override
	public FluidTankForge setCapacity(long capacity) {
		return this;
	}

	@Override
	public void setFluid(FluidStack fluid) {
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}
