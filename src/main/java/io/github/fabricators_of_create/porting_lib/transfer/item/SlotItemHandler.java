package io.github.fabricators_of_create.porting_lib.transfer.item;

import javax.annotation.Nonnull;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemHandler extends Slot {
	private static final Container emptyInventory = new SimpleContainer(0);
	private final ItemStackHandler itemStackHandler;
	private final IItemHandler itemHandler;
	private final int index;

	public SlotItemHandler(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
		super(emptyInventory, index, xPosition, yPosition);
		this.itemStackHandler = itemHandler;
		this.itemHandler = null;
		this.index = index;
	}

	public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(emptyInventory, index, xPosition, yPosition);
		this.itemHandler = itemHandler;
		this.itemStackHandler = null;
		this.index = index;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if (stack.isEmpty())
			return false;
		if (itemStackHandler != null)
			return itemStackHandler.isItemValid(index, ItemVariant.of(stack));
		return itemHandler.isItemValid(index, stack);
	}

	@Override
	@Nonnull
	public ItemStack getItem() {
		return this.getItemHandler().getStackInSlot(index);
	}

	// Override if your IItemHandler does not implement IItemHandlerModifiable
	@Override
	public void set(@Nonnull ItemStack stack) {
		if (itemStackHandler != null)
			((ItemStackHandler)this.getItemHandler()).setStackInSlot(index, stack);
		else
			((IItemHandlerModifiable)this.getItemHandler()).setStackInSlot(index, stack);
		this.setChanged();
	}

	@Override
	public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {

	}

	@Override
	public int getMaxStackSize() {
		return this.getItemHandler().getSlotLimit(this.index);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return getItemHandler().getSlotLimit(index);
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return !getItemHandler().getStackInSlot(index).isEmpty();
	}

	@Override
	@Nonnull
	public ItemStack remove(int amount) {
		if (itemHandler != null) {
			try (Transaction t = TransferUtil.getTransaction()) {
				ItemStack remainer = itemHandler.extractItem(index, amount, t);
				t.commit();
				return remainer;
			}
		}
		ItemStack held = itemHandler.getStackInSlot(index).copy();
		ItemStack removed = held.split(amount);
		itemStackHandler.setStackInSlot(index, held);
		return removed;
	}

	public SlotExposedStorage getItemHandler() {
		return itemHandler;
	}
}
