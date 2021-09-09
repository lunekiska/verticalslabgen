/*
 * Copyright (c) 2021 Lunekiska <kiscaatwork@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.lunekiska.verticalslabgen.registry;

import dev.lambdaurora.aurorasdeco.mixin.block.AbstractBlockAccessor;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SlabTypeB {
    public static final SlabTypeB STONE;

    private static final List<ModificationCallbackEntry> CALLBACKS = new ArrayList<>();
    private static final List<SlabTypeB> TYPES;
    private final Map<ComponentType, Component> components = new Object2ObjectOpenHashMap<>();
    private final List<ModificationCallbackEntry> toTrigger = new ArrayList<>();
    private final Identifier id;
    private final String pathName;
    private final String absoluteLangPath;
    private final String langPath;

    public SlabTypeB(Identifier id) {
        this.id = id;
        this.pathName = getPathName(this.id);
        this.absoluteLangPath = this.pathName.replaceAll("/", ".");
        this.langPath = getLangPath(this.id);

        this.toTrigger.addAll(CALLBACKS);
    }

    static {
        STONE = new SlabTypeB(new Identifier("stone"));
        TYPES = new ArrayList<>(List.of(STONE));
    }

    /**
     * Gets the identifier of slab type.
     *
     * @return the identifier of slab type
     */
    public Identifier getId() {
        return this.id;
    }

    public String getPathName() {
        return this.pathName;
    }

    public String getAbsoluteLangPath() {
        return this.absoluteLangPath;
    }

    public String getLangPath() {
        return this.langPath;
    }

    /**
     * Returns the component associated to the given component type.
     *
     * @param type the component type
     * @return the component if associated to the given component type, otherwise {@code null}
     */
    public Component getComponent(ComponentType type) {
        return this.components.get(type);
    }

    private void addComponent(ComponentType type, Component component) {
        this.components.put(type, component);

        this.onSlabTypeModified();
    }

    private void onSlabTypeModified() {
        var it = this.toTrigger.iterator();
        while (it.hasNext()) {
            var entry = it.next();

            if (AuroraUtil.contains(this.components.keySet(), entry.requiredComponents())) {
                entry.callback().accept(this);
                it.remove();
            }
        }
    }

    private void tryTriggerCallback(ModificationCallbackEntry callbackEntry) {
        if (AuroraUtil.contains(this.components.keySet(), callbackEntry.requiredComponents())) {
            callbackEntry.callback().accept(this);
            this.toTrigger.remove(callbackEntry);
        }
    }

    @Override
    public String toString() {
        return "SlabType{" +
                "id=" + this.id +
                ", pathName='" + this.pathName + '\'' +
                ", remaining_registry_callbacks=" + this.toTrigger.size() +
                ", components=" + this.components.keySet() +
                '}';
    }
    public static void registerSlabTypeModificationCallback(Consumer<SlabTypeB> callback, ComponentType... requiredComponents) {
        var entry = new ModificationCallbackEntry(callback, Arrays.asList(requiredComponents));
        CALLBACKS.add(entry);

        for (var woodType : TYPES) {
            woodType.toTrigger.add(entry);
            woodType.tryTriggerCallback(entry);
        }
    }

    public static void onBlockRegister(Identifier id, Block block) {
        for (var componentType : ComponentType.types()) {
            var slabName = componentType.filter(id, block);
            if (slabName == null) continue;

            var slabId = new Identifier(id.getNamespace(), slabName);
            var slabType = TYPES.stream().filter(type -> type.getId().equals(slabId)).findFirst()
                    .orElseGet(() -> {
                        var newSlabType = new SlabTypeB(slabId);
                        TYPES.add(newSlabType);
                        return newSlabType;
                    });
            slabType.addComponent(componentType, new Component(block));
            break;
        }
    }

    /**
     * Returns slab type of the specified identifier.
     *
     * @param id the identifier of slab type
     * @return slab type if it exists, otherwise {@code null}
     */
    public static @Nullable SlabTypeB fromId(Identifier id) {
        for (var type : TYPES) {
            if (type.getId().equals(id))
                return type;
        }

        return null;
    }

    public static void forEach(Consumer<SlabTypeB> consumer) {
        TYPES.forEach(consumer);
    }

    private static String getPathName(Identifier id) {
        var path = id.getPath();
        var namespace = id.getNamespace();
        if (!namespace.equals("minecraft"))
            path = namespace + '/' + path;
        return path;
    }

    private static String getLangPath(Identifier id) {
        return switch (id.getPath()) {
            default -> getPathName(id).replaceAll("/", ".");
        };
    }

    public record Component(Block block) {
        public Identifier id() {
            return Registry.BLOCK.getId(this.block());
        }

        public Material material() {
            return ((AbstractBlockAccessor) this.block()).getMaterial();
        }

        public MapColor mapColor() {
            return this.block().getDefaultMapColor();
        }

        public BlockSoundGroup blockSoundGroup() {
            return this.block().getSoundGroup(this.block().getDefaultState());
        }

        public Item item() {
            return this.block().asItem();
        }

        public boolean hasItem() {
            return this.item() != Items.AIR;
        }

        public Identifier getItemId() {
            return Registry.ITEM.getId(this.item());
        }

        public Identifier texture() {
            var id = this.id();
            return new Identifier(id.getNamespace(), "block/" + id.getPath());
        }

        @Environment(EnvType.CLIENT)
        public BlockColorProvider getBlockColorProvider() {
            return ColorProviderRegistry.BLOCK.get(this.block());
        }

        @Environment(EnvType.CLIENT)
        public ItemColorProvider getItemColorProvider() {
            return ColorProviderRegistry.ITEM.get(this.block());
        }
    }

    public enum ComponentType {
        SLABS((id, block) -> {
            if (!id.getPath().endsWith("_slab")) return null;
            return id.getPath().substring(0, id.getPath().length() - "_slab".length());
        });

        private static final List<ComponentType> COMPONENT_TYPES = List.of(values());
        private final Filter filter;
        private final TextureProvider textureProvider;

        ComponentType(Filter filter, TextureProvider textureProvider) {
            this.filter = filter;
            this.textureProvider = textureProvider;
        }

        ComponentType(Filter filter) {
            this(filter, BASIC_TEXTURE_PROVIDER);
        }

        public @Nullable String filter(Identifier id, Block block) {
            return this.filter.filter(id, block);
        }

        public Identifier getTexture(ResourceManager resourceManager, Component component) {
            return this.textureProvider.searchTexture(resourceManager, component);
        }

        public static List<ComponentType> types() {
            return COMPONENT_TYPES;
        }
    }

    public interface Filter {
        @Nullable String filter(Identifier id, Block block);
    }

    public static final TextureProvider BASIC_TEXTURE_PROVIDER = (resourceManager, component) -> component.texture();

    public interface TextureProvider {
        Identifier searchTexture(ResourceManager resourceManager, Component component);
    }

    private record ModificationCallbackEntry(Consumer<SlabTypeB> callback, List<ComponentType> requiredComponents) {
    }

}
