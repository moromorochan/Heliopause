package com.moromoro.heliopause.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class StarlightCylinderBlock extends Block {
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    public StarlightCylinderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockPos above = pos.above();
        BlockPos below = pos.below();
        BlockState aboveState = level.getBlockState(above);
        BlockState belowState = level.getBlockState(below);

        boolean canPlaceAbove = level.isEmptyBlock(above);
        boolean canPlaceBelow = level.isEmptyBlock(below);

        // **既存のペアを壊さずに処理を適用**
        if (!canPlaceBelow && belowState.getBlock() instanceof StarlightCylinderBlock && belowState.getValue(HALF) == Half.BOTTOM) {
            return; // 既存のペアがある場合は設置せずキャンセル
        }

        if (!canPlaceAbove && aboveState.getBlock() instanceof StarlightCylinderBlock && aboveState.getValue(HALF) == Half.TOP) {
            return; // 既存のペアがある場合は設置せずキャンセル
        }

        // **通常の設置処理**
        if (canPlaceAbove) {
            level.setBlock(above, this.defaultBlockState().setValue(HALF, Half.TOP), 3);
        } else if (canPlaceBelow) {
            level.setBlock(pos, state.setValue(HALF, Half.TOP), 3);
            level.setBlock(below, this.defaultBlockState().setValue(HALF, Half.BOTTOM), 3);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();

        boolean canPlaceAbove = level.isEmptyBlock(above);
        boolean canPlaceBelow = level.isEmptyBlock(below);

        // 上にも下にも設置できない場合はfalseを返して設置キャンセル
        return canPlaceAbove || canPlaceBelow;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);

        if (state.getBlock() != newState.getBlock()) {
            BlockPos otherPos = state.getValue(HALF) == Half.BOTTOM ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);

            // **ペアが適切に存在しているか確認**
            if (otherState.getBlock() instanceof StarlightCylinderBlock && otherState.getValue(HALF) != state.getValue(HALF)) {
                level.destroyBlock(otherPos, false);
            }
        }
    }
}
