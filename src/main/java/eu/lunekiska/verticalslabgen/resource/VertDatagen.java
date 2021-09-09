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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.lambdaurora.aurorasdeco.resource.datagen.BlockStateBuilder;
import dev.lambdaurora.aurorasdeco.resource.datagen.LangBuilder;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import eu.lunekiska.verticalslabgen.VerticalSlabGen;
import eu.lunekiska.verticalslabgen.block.VertSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

import static dev.lambdaurora.aurorasdeco.util.AuroraUtil.jsonArray;

public final class VertDatagen {
    public static final Logger LOGGER = LogManager.getLogger("verticalslabgen:datagen");

    private static final Pattern SLABS_TO_BASE_ID = Pattern.compile("[_/]slab$");
    private static final Pattern SLABS_SEPARATOR_DETECTOR = Pattern.compile("[/]slab$");

    private VertDatagen() {
        throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
    }

    private static JsonObject generateBlockLootTableSimplePool(Identifier id, boolean copyName) {
        var pool = new JsonObject();
        pool.addProperty("rolls", 1.0);
        pool.addProperty("bonus_rolls", 0.0);

        var entries = new JsonArray();

        var entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", id.toString());

        if (copyName) {
            var function = new JsonObject();
            function.addProperty("function", "minecraft:copy_name");
            function.addProperty("source", "block_entity");
            entry.add("functions", jsonArray(function));
        }

        entries.add(entry);

        pool.add("entries", entries);

        var survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        pool.add("conditions", jsonArray(survivesExplosion));

        return pool;
    }

    private static JsonObject vertSlabBlockLootTable(Identifier id) {
        var root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        var pools = new JsonArray();
        pools.add(generateBlockLootTableSimplePool(id, true));

        {
            var restPool = new JsonObject();
            pools.add(restPool);
            restPool.addProperty("rolls", 1.0);
            var entries = new JsonArray();
            restPool.add("entries", entries);
            var entry = new JsonObject();
            entries.add(entry);
            entry.addProperty("type", "minecraft:dynamic");
            entry.addProperty("name", "verticalslabgen:vertical_slab");
        }

        root.add("pools", pools);

        return root;
    }

    public static void registerVertSlabBlockLootTable(Block block) {
        var id = Registry.BLOCK.getId(block);
        VerticalSlabGen.RESOURCE_PACK.putJson(
                ResourceType.SERVER_DATA,
                new Identifier(id.getNamespace(), "loot_tables/blocks/" + id.getPath()),
                vertSlabBlockLootTable(id)
        );
    }

    public static void generateClientData(ResourceManager resourceManager, LangBuilder langBuilder) {
        generateVertSlabsClientData(resourceManager, langBuilder);
    }

    private static void generateVertSlabsClientData(ResourceManager resourceManager, LangBuilder langBuilder) {
        VertSlabBlock.stream().forEach(vertSlabBlock -> {
            if (AuroraUtil.idEqual(vertSlabBlock.getSlabTypeB().getId(), "minecraft", "stone"))
                return;

            var builder = new BlockStateBuilder(vertSlabBlock);

            builder.register();

            langBuilder.addEntry("block.verticalslabgen.vertical_slab." + vertSlabBlock.getSlabTypeB().getAbsoluteLangPath(),
                    "verticalslabgen.slab_type." + vertSlabBlock.getSlabTypeB().getLangPath());
        });
    }
}
