package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WrittenBoardBlockEntity extends AbstractWrittenBoardBlockEntity{
    public WrittenBoardBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.WRITTEN_BOARD_BE.get(), pos, blockState);
    }

    @Override
    public ResourceLocation getLineType(){
        return CustomModelRegistry.LINE_DEFAULT;
    }
    @Override
    public ResourceLocation getCircleType(){
        return CustomModelRegistry.CIRCLE_DEFAULT;
    }

    @Override
    public boolean operateFromArea(Level level, BlockPos blockPos, Vec3 location, Player player, InteractionHand hand) {
        return false;
    }
}
