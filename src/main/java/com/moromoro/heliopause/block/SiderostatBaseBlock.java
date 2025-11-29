package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SiderostatBaseBlock extends Block {
    public SiderostatBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
        return
            Shapes.or(
                Shapes.box(0,0,0,1,0.5,1),
                Shapes.box(0,0.5,4f/16,1,1,12f/16)
            );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return BlockRegistry.SIDEROSTAT_TOP.get().getCloneItemStack(state, target, level, pos, player);//super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @org.checkerframework.checker.nullness.qual.Nullable LivingEntity placer, ItemStack stack) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();
        BlockState aboveState = level.getBlockState(above);
        BlockState belowState = level.getBlockState(below);

        BlockState aboveNewState = BlockRegistry.SIDEROSTAT_TOP.get().defaultBlockState();
        BlockState belowNewState = BlockRegistry.SIDEROSTAT_BASE.get().defaultBlockState();

        boolean canPlaceAbove = level.isEmptyBlock(above);
        boolean canPlaceBelow = level.isEmptyBlock(below);

        // **既存のペアを壊さずに処理を適用**
        if (!canPlaceBelow && belowState.getBlock() instanceof SiderostatBaseBlock) {
            return; // 既存のペアがある場合は設置せずキャンセル
        }

        if (!canPlaceAbove && aboveState.getBlock() instanceof SiderostatTopBlock) {
            return; // 既存のペアがある場合は設置せずキャンセル
        }

        // **通常の設置処理**
        if (canPlaceAbove) {
            level.setBlock(pos, belowNewState, 3);
            level.setBlock(above, aboveNewState, 3);
        } else if (canPlaceBelow) {
            level.setBlock(below, belowNewState, 3);
            level.setBlock(pos, aboveNewState, 3);
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
            BlockPos otherPos = pos.above();
            BlockState otherState = level.getBlockState(otherPos);

            // **ペアが適切に存在しているか確認**
            if (otherState.getBlock() instanceof SiderostatTopBlock) {
                level.destroyBlock(otherPos, true);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()){
            BlockEntity entity = level.getBlockEntity(pos.above());
            if(entity instanceof SiderostatBlockEntity siderostatBlockEntity){
                siderostatBlockEntity.chargeSpring();
                //ネットワークフックでのGUI表示は1.20.1まで
                //NetworkHooks.openScreen(((ServerPlayer)player),(SiderostatBlockEntity)entity,pos);
            }else{
                throw new IllegalStateException("Container provider is missing! BlockPos:"+pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
