package io.github.fabricators_of_create.porting_lib.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidHandlerStorage;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStorageHandler;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStorageHandlerItem;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTransferableForge;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.IFluidHandler;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.IFluidHandlerItem;
import io.github.fabricators_of_create.porting_lib.transfer.item.CustomStorageHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerStorage;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStorageHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemTransferableForge;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("UnstableApiUsage")
public class TransferUtilForge {
	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be) {
		return getItemHandler(be, null);
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos) {
		return getItemHandler(level, pos, null);
	}

	public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos, @Nullable Direction direction) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) return LazyOptional.empty();
		return getItemHandler(be, direction);
	}

	public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, @Nullable Direction side) {
		// lib handling
		if (be instanceof ItemTransferableForge transferable) return transferable.getItemHandler(side);
		// client handling
		if (Objects.requireNonNull(be.getLevel()).isClientSide()) {
			return LazyOptional.empty();
		}
		// external handling
		List<Storage<ItemVariant>> itemStorages = new ArrayList<>();
		Level l = be.getLevel();
		BlockPos pos = be.getBlockPos();
		BlockState state = be.getBlockState();

		for (Direction direction : getDirections(side)) {
			Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(l, pos, state, be, direction);

			if (itemStorage != null) {
				if (itemStorages.size() == 0) {
					itemStorages.add(itemStorage);
					continue;
				}

				for (Storage<ItemVariant> storage : itemStorages) {
					if (!Objects.equals(itemStorage, storage)) {
						itemStorages.add(itemStorage);
						break;
					}
				}
			}
		}


		if (itemStorages.isEmpty()) return LazyOptional.empty();
		if (itemStorages.size() == 1) return simplifyItem(itemStorages.get(0));
		return simplifyItem(new CombinedStorage<>(itemStorages));
	}

	// Fluids

	public static LazyOptional<IFluidHandler> getFluidHandler(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) return LazyOptional.empty();
		return getFluidHandler(be);
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be) {
		return getFluidHandler(be, null);
	}

	public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, @Nullable Direction side) {
		boolean client = Objects.requireNonNull(be.getLevel()).isClientSide();
		// lib handling
		if (be instanceof FluidTransferableForge transferable) {
			if (client && !transferable.shouldRunClientSide()) {
				return LazyOptional.empty();
			}
			return transferable.getFluidHandler(side);
		}
		// external handling
		List<Storage<FluidVariant>> fluidStorages = new ArrayList<>();
		Level l = be.getLevel();
		BlockPos pos = be.getBlockPos();
		BlockState state = be.getBlockState();

		for (Direction direction : getDirections(side)) {
			Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(l, pos, state, be, direction);

			if (fluidStorage != null) {
				if (fluidStorages.size() == 0) {
					fluidStorages.add(fluidStorage);
					continue;
				}

				for (Storage<FluidVariant> storage : fluidStorages) {
					if (!Objects.equals(fluidStorage, storage)) {
						fluidStorages.add(fluidStorage);
						break;
					}
				}
			}
		}

		if (fluidStorages.isEmpty()) return LazyOptional.empty();
		if (fluidStorages.size() == 1) return simplifyFluid(fluidStorages.get(0));
		return simplifyFluid(new CombinedStorage<>(fluidStorages));
	}

	// Fluid-containing items

	public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return LazyOptional.empty();
		ContainerItemContext ctx = ContainerItemContext.withInitial(stack);
		Storage<FluidVariant> fluidStorage = FluidStorage.ITEM.find(stack, ctx);
		return fluidStorage == null ? LazyOptional.empty() : LazyOptional.ofObject(new FluidStorageHandlerItem(ctx, fluidStorage));
	}

	// Helpers

	public static LazyOptional<?> getHandler(BlockEntity be, @Nullable Direction direction, Class<?> handler) {
		if (handler == IItemHandler.class) {
			return getItemHandler(be, direction);
		} else if (handler == IFluidHandler.class) {
			return getFluidHandler(be, direction);
		} else throw new RuntimeException("Handler class must be IItemHandler or IFluidHandler");
	}

	public static LazyOptional<IItemHandler> simplifyItem(Storage<ItemVariant> storage) {
		if (storage == null) return LazyOptional.empty();
		if (storage instanceof ItemHandlerStorage handler) return LazyOptional.ofObject(handler.getHandler());
		return LazyOptional.ofObject(new ItemStorageHandler(storage));
	}

	public static LazyOptional<IFluidHandler> simplifyFluid(Storage<FluidVariant> storage) {
		if (storage == null) return LazyOptional.empty();
		if (storage instanceof FluidHandlerStorage handler) return LazyOptional.ofObject(handler.getHandler());
		return LazyOptional.ofObject(new FluidStorageHandler(storage));
	}

	@Nullable
	public static Storage<FluidVariant> getFluidStorageForBE(BlockEntity be, Direction side) {
		if (be instanceof FluidTransferableForge transferable) {
			LazyOptional<IFluidHandler> handler = transferable.getFluidHandler(side);
			return handler == null || !handler.isPresent() ? null : new FluidHandlerStorage(handler.getValueUnsafer());
		}
		return null;
	}

	@Nullable
	public static Storage<ItemVariant> getItemStorageForBE(BlockEntity be, Direction side) {
		if (be instanceof ItemTransferableForge transferable) {
			LazyOptional<IItemHandler> handler = transferable.getItemHandler(side);
			if (handler instanceof CustomStorageHandler custom) return custom.getStorage();
			return handler == null || !handler.isPresent() ? null : new ItemHandlerStorage(handler.getValueUnsafer());
		}
		return null;
	}

	/**
	 * Helper method to get the fluid contained in an itemStack
	 */
	public static Optional<FluidStack> getFluidContained(@Nonnull ItemStack container) {
		if (!container.isEmpty()) {
			container = ItemHandlerHelper.copyStackWithSize(container, 1);
			Optional<FluidStack> fluidContained = getFluidHandlerItem(container)
					.map(handler -> handler.drain(Integer.MAX_VALUE, true));
			if (fluidContained.isPresent() && !fluidContained.get().isEmpty())
			{
				return fluidContained;
			}
		}
		return Optional.empty();
	}

	private static Direction[] getDirections(@Nullable Direction direction) {
		if (direction == null) return Direction.values();
		return new Direction[] {direction};
	}

	public static void registerFluidStorage(BlockEntityType<?> type) {
		FluidStorage.SIDED.registerForBlockEntities(TransferUtilForge::getFluidStorageForBE, type);
	}

	public static void registerItemStorage(BlockEntityType<?> type) {
		ItemStorage.SIDED.registerForBlockEntities(TransferUtilForge::getItemStorageForBE, type);
	}
}
