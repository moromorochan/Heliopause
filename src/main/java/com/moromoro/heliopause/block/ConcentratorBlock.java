package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.entity.LensBarrelEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcentratorBlock extends BaseEntityBlock {
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public ConcentratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ENABLED,false)
        );
    }

    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    @Override
    public void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos blockPos, @NotNull BlockState newBlockState, boolean isMoving) {

        if(!level.isClientSide()) {
            if(!(newBlockState.getBlock() instanceof ConcentratorBlock)) {
                if(level.getBlockEntity(blockPos) instanceof ConcentratorBlockEntity blockEntity){
                    List<LensBarrelEntity> entities = level.getEntitiesOfClass(LensBarrelEntity.class, new AABB(blockPos.above()));
                    for (LensBarrelEntity entity : entities) {
                        blockEntity.disAssemble(entity.getBarrels());
                        entity.discard();
                    }
                }
            }
        }

        super.onRemove(state, level, blockPos, newBlockState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new ConcentratorBlockEntity(blockPos, blockState);
    }

    /*@Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        // 上面は無視
        if (!hitResult.getDirection().equals(Direction.UP)) {
            if (level.getBlockEntity(blockPos) instanceof ConcentratorBlockEntity entity) {
                if (blockState.getValue(ConcentratorBlock.ENABLED).equals(false)) {
                    if (entity.assemble()) {
                        return InteractionResult.sidedSuccess(!level.isClientSide());
                    }
                } else {
                    entity.disAssemble();
                    return InteractionResult.sidedSuccess(!level.isClientSide());
                }
            }
        }
        return super.use(blockState, level, blockPos, player, hand, hitResult);
    }

    public void pairRemoved(Level level, BlockState blockState, BlockPos blockPos) {
        if(blockState.getValue(ConcentratorBlock.ENABLED).equals(true)){
            this.onRemove(blockState,level, blockPos, Blocks.AIR.defaultBlockState(), false);
        }
    }*/

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()){
            //BlockPos basePos = pos.below();
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ConcentratorBlockEntity menuEntity){
                //ネットワークフックでのGUI表示は1.20.1まで
                NetworkHooks.openScreen((ServerPlayer)player, menuEntity, pos);
            }else{
                throw new IllegalStateException("Container provider is missing! BlockPos:"+pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.CONCENTRATOR_BE.get(),
            (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
        );
    }
}
