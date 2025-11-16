package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import com.moromoro.heliopause.blockStateProperty.WrittenBoardType;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrittenBoardBlock extends Block implements EntityBlock {
    public static final EnumProperty<WrittenBoardType> CIRCLE_TYPE = WrittenBoardType.create("type", WrittenBoardType.class);
    public WrittenBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CIRCLE_TYPE,WrittenBoardType.CROSS_CIRCLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CIRCLE_TYPE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new WrittenBoardBlockEntity(blockPos, blockState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return BlockRegistry.BLACKBOARD.get().getCloneItemStack(state, target, level, pos, player);//super.getCloneItemStack(state, target, level, pos, player);
    }
}
