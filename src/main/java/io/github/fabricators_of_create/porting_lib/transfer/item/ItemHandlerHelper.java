package io.github.fabricators_of_create.porting_lib.transfer.item;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemHandlerHelper {

	@Nonnull
	public static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate) {
		if (dest == null || stack.isEmpty())
			return stack;

		for (int i = 0; i < dest.getSlots(); i++) {
			try (Transaction t = TransferUtil.getTransaction()) {
				stack = dest.insertItem(i, stack, t);
				if (!simulate)
					t.commit();
			}
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}

		return stack;
	}

	public static boolean canItemStacksStack(ItemStack first, ItemStack second) {
		if (first.isEmpty() || !first.sameItem(second) || first.hasTag() != second.hasTag()) return false;

		return !first.hasTag() || first.getTag().equals(second.getTag());
	}

	public static ItemStack copyStackWithSize(ItemStack stack, int size) {
		if (size == 0) return ItemStack.EMPTY;
		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}
}
