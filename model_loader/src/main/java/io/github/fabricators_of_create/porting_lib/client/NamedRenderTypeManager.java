package io.github.fabricators_of_create.porting_lib.client;

import java.util.HashMap;
import java.util.Map;

import io.github.fabricators_of_create.porting_lib.client.RenderTypeGroup;
import io.github.fabricators_of_create.porting_lib.model.PortingLibRenderTypes;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Manager for named {@link RenderType render types}.
 * <p>
 * Provides a lookup.
 */
public final class NamedRenderTypeManager {
	private static ImmutableMap<ResourceLocation, RenderTypeGroup> RENDER_TYPES;

	/**
	 * Finds the {@link RenderTypeGroup} for a given name, or the {@link RenderTypeGroup#EMPTY empty group} if not found.
	 */
	public static RenderTypeGroup get(ResourceLocation name) {
		return RENDER_TYPES.getOrDefault(name, RenderTypeGroup.EMPTY);
	}

	@ApiStatus.Internal
	public static void init() {
		var renderTypes = new HashMap<ResourceLocation, RenderTypeGroup>();
		preRegisterVanillaRenderTypes(renderTypes);
		RENDER_TYPES = ImmutableMap.copyOf(renderTypes);
	}

	/**
	 * Pre-registers vanilla render types.
	 */
	private static void preRegisterVanillaRenderTypes(Map<ResourceLocation, RenderTypeGroup> blockRenderTypes) {
		blockRenderTypes.put(new ResourceLocation("solid"), new RenderTypeGroup(RenderType.solid(), PortingLibRenderTypes.ITEM_LAYERED_SOLID.get()));
		blockRenderTypes.put(new ResourceLocation("cutout"), new RenderTypeGroup(RenderType.cutout(), PortingLibRenderTypes.ITEM_LAYERED_CUTOUT.get()));
		// Generally entity/item rendering shouldn't use mipmaps, so cutout_mipped has them off by default. To enforce them, use cutout_mipped_all.
		blockRenderTypes.put(new ResourceLocation("cutout_mipped"), new RenderTypeGroup(RenderType.cutoutMipped(), PortingLibRenderTypes.ITEM_LAYERED_CUTOUT.get()));
		blockRenderTypes.put(new ResourceLocation("cutout_mipped_all"), new RenderTypeGroup(RenderType.cutoutMipped(), PortingLibRenderTypes.ITEM_LAYERED_CUTOUT_MIPPED.get()));
		blockRenderTypes.put(new ResourceLocation("translucent"), new RenderTypeGroup(RenderType.translucent(), PortingLibRenderTypes.ITEM_LAYERED_TRANSLUCENT.get()));
		blockRenderTypes.put(new ResourceLocation("tripwire"), new RenderTypeGroup(RenderType.tripwire(), PortingLibRenderTypes.ITEM_LAYERED_TRANSLUCENT.get()));
	}

	private NamedRenderTypeManager()
	{
	}
}
