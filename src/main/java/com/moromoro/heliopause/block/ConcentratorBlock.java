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

        if(!newBlockState.is(this)) {
            disassembleBarrel(state, level, blockPos, newBlockState);
        }

        super.onRemove(state, level, blockPos, newBlockState, isMoving);
    }

    private void disassembleBarrel(@NotNull BlockState state, Level level, @NotNull BlockPos blockPos, @NotNull BlockState newBlockState) {
        List<LensBarrelEntity> entity =
            level.getEntitiesOfClass(LensBarrelEntity.class, new AABB(blockPos.above(2)), e -> true);
        for (LensBarrelEntity barrelEntity : entity) {
            barrelEntity.disassemble();
        }
        // enabled切り替え
        /*BlockState newState =  level.getBlockState(blockPos).setValue(ConcentratorBlock.ENABLED, false);
        level.setBlock(blockPos,newState, 3);
        level.sendBlockUpdated(blockPos, newState, newState, 3);*/
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new ConcentratorBlockEntity(blockPos, blockState);
    }

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
