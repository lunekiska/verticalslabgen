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

import dev.lambdaurora.aurorasdeco.util.Registrar;
import eu.lunekiska.verticalslabgen.block.VertSlabBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import static eu.lunekiska.verticalslabgen.VerticalSlabGen.VerticalSlabsGroup;

public final class VerticalSlabGenRegistry {

    private VerticalSlabGenRegistry() {
        throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
    }

    public static void init() {
        SlabTypeB.registerSlabTypeModificationCallback(slabTypeB -> Registrar.register("vertical_slab/" + slabTypeB.getPathName(), new VertSlabBlock(slabTypeB))
            .withItem(new FabricItemSettings().group(VerticalSlabsGroup)),
         SlabTypeB.ComponentType.SLABS);
    }
}
