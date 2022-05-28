package io.github.fabricators_of_create.porting_lib.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public class EmptyHandler implements IItemHandlerModifiable {
	public static final EmptyHandler INSTANCE = new EmptyHandler();

	private EmptyHandler() {}

	@Override
	public int getSlots() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, TransactionContext t) {
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, TransactionContext t) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {}
}
