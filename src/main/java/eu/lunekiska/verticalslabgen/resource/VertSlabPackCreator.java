package eu.lunekiska.verticalslabgen.resource;

import eu.lunekiska.verticalslabgen.VerticalSlabGen;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;

import java.util.function.Consumer;

public class VertSlabPackCreator implements ResourcePackProvider {
    @Override
    public void register(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) {
        consumer.accept(ResourcePackProfile.of("verticalslabgen:pack/runtime",
                true,
                () -> VerticalSlabGen.RESOURCE_PACK.rebuild(ResourceType.SERVER_DATA, null),
                factory, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.onlyName()));
    }
}