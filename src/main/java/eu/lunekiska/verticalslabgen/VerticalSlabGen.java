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

package eu.lunekiska.verticalslabgen;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import eu.lunekiska.verticalslabgen.registry.VerticalSlabGenRegistry;
import eu.lunekiska.verticalslabgen.resource.VertSlabGenPack;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * Represents VerticalSlabGen mod.
 *
 * @author Lunekiska
 * @version 1.0.0
 * @since 1.0.0
 */
public class VerticalSlabGen implements ModInitializer {

    public static final String NAMESPACE = "verticalslabgen";

    public static final VertSlabGenPack RESOURCE_PACK;

    public static final ItemGroup VerticalSlabsGroup = FabricItemGroupBuilder.build(new Identifier(NAMESPACE, "vertical_slabs"), () -> new ItemStack(AurorasDecoRegistry.TUFF_SLAB));

    public void onInitialize() {
        VerticalSlabGenRegistry.init();
    }

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }

    static {
        RESOURCE_PACK = new VertSlabGenPack(ResourceType.SERVER_DATA);
    }
}