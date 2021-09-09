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

package eu.lunekiska.verticalslabgen.resource;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import dev.lambdaurora.aurorasdeco.resource.Datagen;
import dev.lambdaurora.aurorasdeco.resource.datagen.LangBuilder;
import eu.lunekiska.verticalslabgen.block.VertSlabBlock;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VertSlabGenPack extends AurorasDecoPack implements ModResourcePack {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<String> namespaces = new HashSet();
    private final Map<String, byte[]> resources = new Object2ObjectOpenHashMap();
    private final ResourceType type;

    public VertSlabGenPack(ResourceType type) {
        super(type);
        this.type = type;
    }

    public VertSlabGenPack rebuild(ResourceType type, @Nullable ResourceManager resourceManager) {
        return type == ResourceType.CLIENT_RESOURCES ? this.rebuildClient(resourceManager) : this.rebuildData();
    }

    public VertSlabGenPack rebuildClient(ResourceManager resourceManager) {
        LangBuilder langBuilder = new LangBuilder();
        langBuilder.load();
        this.namespaces.add("verticalslabgen");
        VertDatagen.generateClientData(resourceManager, langBuilder);
        langBuilder.write(this);
        return this;
    }

    public VertSlabGenPack rebuildData() {
        this.resources.clear();
        this.namespaces.clear();

        VertSlabBlock.stream().forEach(VertDatagen::registerVertSlabBlockLootTable);
        return this;
    }

    public void putResource(String resource, byte[] data) {
        this.resources.put(resource, data);
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            try {
                Path path = Paths.get("debug", "aurorasdeco").resolve(resource);
                Files.createDirectories(path.getParent());
                Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

    }

    public void putJsonText(ResourceType type, Identifier id, String json) {
        this.namespaces.add(id.getNamespace());
        String var10000 = Datagen.toPath(id, type);
        String path = var10000 + ".json";
        this.putText(path, json);
    }

    public void putText(String resource, String text) {
        this.putResource(resource, text.getBytes(StandardCharsets.UTF_8));
    }

    public void putJson(ResourceType type, Identifier id, JsonObject json) {
        this.namespaces.add(id.getNamespace());
        String var10000 = Datagen.toPath(id, type);
        String path = var10000 + ".json";
        this.putJson(path, json);
    }

    public void putJson(String resource, JsonObject json) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(true);
        jsonWriter.setIndent("  ");

        try {
            Streams.write(json, jsonWriter);
        } catch (IOException var6) {
            LOGGER.error("Failed to write JSON at {}.", resource, var6);
        }

        this.putText(resource, stringWriter.toString());
    }

    public void putImage(Identifier id, NativeImage image) {
        this.namespaces.add(id.getNamespace());
        String path = Datagen.toPath(id, ResourceType.CLIENT_RESOURCES, "textures/") + ".png";
        this.putImage(path, image);
    }

    public void putImage(String location, NativeImage image) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        WritableByteChannel out = Channels.newChannel(byteOut);
        this.putResource(location, byteOut.toByteArray());

        try {
            out.close();
        } catch (IOException var6) {
            LOGGER.warn("Could not close output channel for texture " + location + ".", var6);
        }

    }

    public ModMetadata getFabricModMetadata() {
        return FabricLoader.getInstance().getModContainer("verticalslabgen").get().getMetadata();
    }
}
