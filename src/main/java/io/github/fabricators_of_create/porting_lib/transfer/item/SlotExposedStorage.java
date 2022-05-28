package io.github.fabricators_of_create.porting_lib.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

public interface SlotExposedStorage extends Storage<ItemVariant> {
	int getSlots();
	ItemStack getStackInSlot(int slot);
	int getSlotLimit(int slot);
}
