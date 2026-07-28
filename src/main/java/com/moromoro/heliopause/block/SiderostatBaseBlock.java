package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SiderostatBaseBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public SiderostatBaseBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(POWERED,false)
        );
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
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    // レッドストーン用
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);

        boolean hasSignal = level.hasNeighborSignal(pos);
        if (hasSignal != state.getValue(POWERED)) {
            // 状態を更新
            level.setBlock(pos, state.setValue(POWERED, hasSignal), 3);
        }
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
        
        if (!canPlaceBelow && belowState.getBlock() instanceof SiderostatBaseBlock) {
            return; // 既存のペアがある場合は設置せずキャンセル
        }

        if (!canPlaceAbove && aboveState.getBlock() instanceof SiderostatTopBlock) {
            return; // 既存のペアがある場合は設置せずキャンセル
        }

        // 通常の設置処理
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
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof SiderostatBlockEntity siderostatBlockEntity){
                siderostatBlockEntity.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
            
            // ペア確認
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            
            if (aboveState.getBlock() instanceof SiderostatTopBlock) {
                level.destroyBlock(above, false);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()){
            if(player.isSpectator()){
                return InteractionResult.PASS;
            }
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof SiderostatBlockEntity siderostatBlockEntity){
                siderostatBlockEntity.chargeSpring();
            }else{
                throw new IllegalStateException("Container provider is missing! BlockPos:"+pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SiderostatBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.SIDEROSTAT_BE.get(),
            (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
        );
    }
}
