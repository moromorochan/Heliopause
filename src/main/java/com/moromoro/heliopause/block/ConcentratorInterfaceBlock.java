package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class ConcentratorInterfaceBlock extends Block {
    public ConcentratorInterfaceBlock(Properties properties) {
        super(properties);
    }

    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, blockPos, newState, isMoving);

        if(state.getBlock() != newState.getBlock()){
            BlockPos otherPos = blockPos.below();
            BlockState otherState = level.getBlockState(otherPos);

            // **ペアが適切に存在しているか確認**
            if (otherState.getBlock() instanceof ConcentratorBlock baseBlock) {
                baseBlock.pairRemoved(level, otherState, otherPos);
            }
        }
    }*/
}
