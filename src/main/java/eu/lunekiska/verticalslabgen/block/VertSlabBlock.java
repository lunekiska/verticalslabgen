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

package eu.lunekiska.verticalslabgen.block;

import eu.lunekiska.verticalslabgen.registry.SlabTypeB;
import eu.lunekiska.verticalslabgen.registry.VertSlabType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.BlockView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VertSlabBlock extends HorizontalFacingBlock {

    public static EnumProperty<VertSlabType> TYPE;

    public static BooleanProperty WATERLOGGED;

    public static VoxelShape NORTH_SHAPE;
    public static VoxelShape EAST_SHAPE;
    public static VoxelShape SOUTH_SHAPE;
    public static VoxelShape WEST_SHAPE;

    private static final List<VertSlabBlock> VERT_SLAB_BLOCKS = new ArrayList<>();

    private final SlabTypeB slabTypeB;

    public VertSlabBlock(SlabTypeB slabTypeB) {
        super(settings(slabTypeB));

        this.slabTypeB = slabTypeB;

        this.setDefaultState((this.getDefaultState().with(TYPE, VertSlabType.NORTH)).with(WATERLOGGED, false));

        VERT_SLAB_BLOCKS.add(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED);
    }

    public static Stream<VertSlabBlock> stream() {
        return VERT_SLAB_BLOCKS.stream();
    }

    public SlabTypeB getSlabTypeB() {
        return this.slabTypeB;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VertSlabType vertSlabType = state.get(TYPE);
        switch(vertSlabType) {
            case  EAST:
                return EAST_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            default:
                return NORTH_SHAPE;
        }
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext itemPlacementContext) {
        BlockPos blockPos = itemPlacementContext.getBlockPos();
        BlockState blockState = itemPlacementContext.getWorld().getBlockState(blockPos);

        FluidState fluidState = itemPlacementContext.getWorld().getFluidState(blockPos);
        BlockState blockState2 = this.getDefaultState().with(TYPE, VertSlabType.NORTH).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        Direction direction = itemPlacementContext.getSide();
        if (direction == Direction.EAST) {
            return blockState2.with(TYPE, VertSlabType.EAST);
        }

        if (direction == Direction.SOUTH) {
            return blockState2.with(TYPE, VertSlabType.SOUTH);
        }

        if (direction == Direction.WEST) {
            return blockState2.with(TYPE, VertSlabType.WEST);
        }
        return blockState;
    }
    static {

        VoxelShape shape = createCuboidShape(0, 0, 0, 8, 16, 16);

        TYPE = VertProperties.VERT_SLAB_TYPE;
        WATERLOGGED = Properties.WATERLOGGED;

        EAST_SHAPE = shape;
        NORTH_SHAPE = rotate(Direction.EAST, Direction.NORTH, shape);
        SOUTH_SHAPE = rotate(Direction.EAST, Direction.SOUTH, shape);
        WEST_SHAPE = rotate(Direction.EAST, Direction.WEST, shape);
    }

    public static VoxelShape rotate(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{ shape, VoxelShapes.empty() };

        int times = (to.getHorizontal() - from.getHorizontal() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1-maxZ, minY, minX, 1-minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }

    public static class VertProperties {
        public static final EnumProperty<VertSlabType> VERT_SLAB_TYPE;

        static {
            VERT_SLAB_TYPE = EnumProperty.of("type", VertSlabType.class);
        }
    }

    private static Settings settings(SlabTypeB slabTypeB) {
        var slab = slabTypeB.getComponent(SlabTypeB.ComponentType.SLABS);
        return FabricBlockSettings.of(slab.material(), slab.mapColor())
                .sounds(slab.blockSoundGroup());
    }
}
