package io.github.fabricators_of_create.porting_lib;

import io.github.fabricators_of_create.porting_lib.event.client.ModelsBakedCallback;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.block.BlockModelShaper;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fabricators_of_create.porting_lib.entity.MultiPartEntity;
import io.github.fabricators_of_create.porting_lib.entity.PartEntity;
import io.github.fabricators_of_create.porting_lib.util.FluidTextUtil;
import io.github.fabricators_of_create.porting_lib.util.LogicalSidedProvider;
import io.github.fabricators_of_create.porting_lib.util.NetworkUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class PortingLibClient implements ClientModInitializer {
	private final Logger LOGGER = LoggerFactory.getLogger("porting_lib_client");

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(FluidTextUtil.NUMBER_FORMAT);

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof MultiPartEntity partEntity && partEntity.isMultipartEntity()) {
				for (PartEntity<?> part : partEntity.getParts()) {
					world.getPartEntityMap().put(part.getId(), part);
				}
			}
		});
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof MultiPartEntity partEntity && partEntity.isMultipartEntity()) {
				for (PartEntity<?> part : partEntity.getParts()) {
					world.getPartEntityMap().remove(part.getId());
				}
			}
		});
		InjectedInterfacesGen.run();
		ClientPlayNetworking.registerGlobalReceiver(NetworkUtil.OPEN_ID, (client, handler, buf, responseSender) -> {
			int typeId = buf.readVarInt();
			int syncId = buf.readVarInt();
			Component title = buf.readComponent();
			FriendlyByteBuf extraData = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(32600)));
			// Retain the buf since we must open the screen handler with it's extra modded data on the client thread
			// The buf will be released after the screen is opened
			buf.retain();

			client.execute(() -> openScreen(typeId, syncId, title, extraData));
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> LogicalSidedProvider.setClient(() -> client));


	}

	private void openScreen(int typeId, int syncId, Component title, FriendlyByteBuf buf) {
		try {
			MenuType<?> type = Registry.MENU.byId(typeId);

			if (type == null) {
				LOGGER.warn("Unknown screen handler ID: {}", typeId);
				return;
			}

			if (!(type instanceof ExtendedScreenHandlerType<?>)) {
				LOGGER.warn("Received extended opening packet for non-extended screen handler {}", typeId);
				return;
			}

			AbstractContainerMenu c = ((ExtendedScreenHandlerType)type).create(syncId, Minecraft.getInstance().player.getInventory(), buf);
			@SuppressWarnings("unchecked")
			Screen s = ((MenuScreens.ScreenConstructor<AbstractContainerMenu, ?>)MenuScreens.getConstructor(type)).create(c, Minecraft.getInstance().player.getInventory(), title);
			Minecraft.getInstance().player.containerMenu = ((MenuAccess<?>)s).getMenu();
			Minecraft.getInstance().setScreen(s);
		} finally {
			buf.release(); // Release the buf
		}
	}
}
