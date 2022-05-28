package io.github.fabricators_of_create.porting_lib.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public interface IItemHandler extends Storage<ItemVariant> {
	int getSlots();
	ItemStack getStackInSlot(int slot);
	ItemStack insertItem(int slot, ItemStack stack, TransactionContext t); // remainder
	ItemStack extractItem(int slot, int amount, TransactionContext t); // extracted
	int getSlotLimit(int slot);
	boolean isItemValid(int slot, ItemStack stack);

	@Override
	default long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return insertItem(0, resource.toStack((int) maxAmount), transaction).getCount();
	};

	@Override
	default long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return extractItem(0, (int) maxAmount, transaction).getCount();
	}

	@Override
	default Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return null; // TODO: Implement
	}
}
