package me.anomz.blockoutline.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Builds the outline shape for multi-part blocks: when targeting one half of a
 * door, bed, double chest, tall plant or extended piston, the other half is
 * merged in so the whole structure gets a single, seamless outline.
 */
public final class ConnectedBlocks {
    private ConnectedBlocks() {
    }

    /**
     * Returns the shape to outline at {@code pos}, in that block's local
     * coordinates. {@code connect} merges the partner block of multi-part
     * blocks; {@code cube} swaps every part's shape for the full block cube.
     */
    public static VoxelShape shapeFor(BlockGetter level, BlockPos pos, BlockState state,
                                      VoxelShape shape, boolean connect, boolean cube) {
        VoxelShape base = cube ? Shapes.block() : shape;
        if (!connect) {
            return base;
        }
        BlockPos otherPos = partnerPos(level, pos, state);
        if (otherPos == null) {
            return base;
        }
        VoxelShape other = cube
                ? Shapes.block()
                : level.getBlockState(otherPos).getShape(level, otherPos);
        if (other.isEmpty()) {
            return base;
        }
        return Shapes.or(base, other.move(
                otherPos.getX() - pos.getX(),
                otherPos.getY() - pos.getY(),
                otherPos.getZ() - pos.getZ())).optimize();
    }

    /** The position of the block's other half, or null if it has none. */
    private static BlockPos partnerPos(BlockGetter level, BlockPos pos, BlockState state) {
        // Doors, tall plants/flowers, and anything else split into two vertical halves
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState other = level.getBlockState(otherPos);
            if (other.getBlock() == state.getBlock()
                    && other.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    && other.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != half) {
                return otherPos;
            }
            return null;
        }

        // Beds: FACING points from foot to head
        if (state.getBlock() instanceof BedBlock && state.hasProperty(BlockStateProperties.BED_PART)) {
            BedPart part = state.getValue(BlockStateProperties.BED_PART);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos otherPos = pos.relative(part == BedPart.FOOT ? facing : facing.getOpposite());
            BlockState other = level.getBlockState(otherPos);
            if (other.getBlock() == state.getBlock()
                    && other.hasProperty(BlockStateProperties.BED_PART)
                    && other.getValue(BlockStateProperties.BED_PART) != part) {
                return otherPos;
            }
            return null;
        }

        // Double chests
        if (state.getBlock() instanceof ChestBlock
                && state.hasProperty(BlockStateProperties.CHEST_TYPE)
                && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE) {
            Direction toOther = ChestBlock.getConnectedDirection(state);
            BlockPos otherPos = pos.relative(toOther);
            BlockState other = level.getBlockState(otherPos);
            if (other.getBlock() == state.getBlock()
                    && other.hasProperty(BlockStateProperties.CHEST_TYPE)
                    && other.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE
                    && ChestBlock.getConnectedDirection(other) == toOther.getOpposite()) {
                return otherPos;
            }
            return null;
        }

        // Extended piston base -> its head
        if (state.getBlock() instanceof PistonBaseBlock
                && state.hasProperty(BlockStateProperties.EXTENDED)
                && state.getValue(BlockStateProperties.EXTENDED)) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos otherPos = pos.relative(facing);
            if (level.getBlockState(otherPos).getBlock() instanceof PistonHeadBlock) {
                return otherPos;
            }
            return null;
        }

        // Piston head -> its base
        if (state.getBlock() instanceof PistonHeadBlock) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos otherPos = pos.relative(facing.getOpposite());
            BlockState other = level.getBlockState(otherPos);
            if (other.getBlock() instanceof PistonBaseBlock
                    && other.hasProperty(BlockStateProperties.EXTENDED)
                    && other.getValue(BlockStateProperties.EXTENDED)) {
                return otherPos;
            }
            return null;
        }

        return null;
    }
}
