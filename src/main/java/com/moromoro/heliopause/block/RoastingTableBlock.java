package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.RoastingTableBlockEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoastingTableBlock extends BaseEntityBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public RoastingTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    /*public static VoxelShape SHAPE = Shapes.join(
            Block.box(0,9,0,16,16,16),
            Block.box(2, 0, 2, 14, 9, 14),
            BooleanOp.OR
    );

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }*/

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public void onRemove(BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, BlockState newBlockState, boolean isMoving) {
        if(blockState.getBlock() != newBlockState.getBlock()){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof RoastingTableBlockEntity){
                ((RoastingTableBlockEntity)blockEntity).drops();
            }
        }
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof RoastingTableBlockEntity){
                //ネットワークフックでのGUI表示は1.20.1まで
                NetworkHooks.openScreen(((ServerPlayer)player),(RoastingTableBlockEntity)entity,pos);
            }else{
                throw new IllegalStateException("Container provider is missing! BlockPos:"+pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RoastingTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.ROASTING_TABLE_BE.get(),
                (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
                );
    }

    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
        if (blockState.getValue(LIT)) {
            //位置を用意
            float posX = pos.getX();
            float posY = pos.getY();
            float posZ = pos.getZ();
            //animateTick毎に確率で効果音を鳴らす
            if (randomSource.nextFloat() < 0.1f) {
                level.playLocalSound(posX+ 0.5f, posY, posZ+ 0.5f, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            /*
            //ブロックの方向を取得
            Comparable axis = blockState.getValue(AXIS);

            //パーティクルを発生させる範囲のための座標を用意
            float ventTop = 13.5f/16f, ventBottom = 11.5f/16f, ventSide = 3f/16f;
            //パーティクルの発生座標をランダムで作成
            float randomEmitX = randomSource.nextInt(2);
            float randomEmitZ = randomSource.nextFloat() * (1-2*ventSide) + ventSide;
            float randomEmitY = randomSource.nextFloat() * (ventTop-ventBottom) + ventBottom;

            float axisEmitX = axis == Direction.Axis.Z ? randomEmitX:randomEmitZ;
            float axisEmitZ = axis == Direction.Axis.X ? randomEmitX:randomEmitZ;
            float axisEmitY = randomEmitY;

            //パーティクルを生成
            level.addParticle(ParticleTypes.SMOKE, posX+axisEmitX, posY+axisEmitY, posZ+axisEmitZ, 0, 0, 0);
            */
        }
    }
}
