package io.github.fabricators_of_create.porting_lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.RegistryAccess;

public interface TagsUpdatedCallback {
	Event<TagsUpdatedCallback> EVENT = EventFactory.createArrayBacked(TagsUpdatedCallback.class, callbacks -> registries -> {
		for (TagsUpdatedCallback callback : callbacks)
			callback.onTagsUpdated(registries);
	});

	void onTagsUpdated(RegistryAccess registries);
}
