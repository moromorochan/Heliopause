package com.moromoro.heliopause.block;

import com.moromoro.heliopause.EnumProperty.SiderostatTopState;
import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.particle.StarRippleParticles;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SiderostatTopBlock extends Block {

    public static final EnumProperty<SiderostatTopState> FACING_SIDEROSTAT = SiderostatTopState.create("charge_state", SiderostatTopState.class);//DirectionProperty.create("facing", Direction.EAST, Direction.UP, Direction.WEST);
    public SiderostatTopBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING_SIDEROSTAT, SiderostatTopState.EMPTY));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
        return Shapes.box(0,0,4f/16,1,1,12f/16);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        if(state.getValue(FACING_SIDEROSTAT) == SiderostatTopState.MOVING){
            return RenderShape.INVISIBLE;
        }
        return RenderShape.MODEL;
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

        if(state.getBlock() != newState.getBlock()){
            BlockPos otherPos = pos.below();
            BlockState otherState = level.getBlockState(otherPos);

            // **ペアが適切に存在しているか確認**
            if (otherState.getBlock() instanceof SiderostatBaseBlock) {
                level.destroyBlock(otherPos, false);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()){
            BlockPos basePos = pos.below();
            BlockEntity entity = level.getBlockEntity(basePos);
            if(entity instanceof SiderostatBlockEntity menuEntity){
                //ネットワークフックでのGUI表示は1.20.1まで
                NetworkHooks.openScreen((ServerPlayer)player, menuEntity, basePos);
            }else{
                throw new IllegalStateException("Container provider is missing! BlockPos:"+basePos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(FACING_SIDEROSTAT, SiderostatTopState.EMPTY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING_SIDEROSTAT);
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        // 現在のフレーム時間を取得
        //float currentFrameTime = (System.nanoTime() / 1_000_000_000.0F) + (RandomSource.create(blockPos.asLong()).nextFloat()*8);

        if (/*currentFrameTime % 8 > 0.5 || */randomSource.nextInt(1000) > 30) {
            return;
        }
        Vec3 centerPos = blockPos.getCenter().add(0,-0.4f + randomSource.nextGaussian() * 0.02f,0);
        float scale = randomSource.nextInt(4,6)*0.1f;
        BlockEntity blockEntity = level.getBlockEntity(blockPos.below());
        if(blockEntity instanceof SiderostatBlockEntity entity){
            if(entity.getSynced()&&entity.isAngleVisible(entity.getSpringAmount(),entity.getCanSeeSkies())){
                // パーティクル生成
                for (int i = 0; i < 2; i++) {
                    StarRippleParticles particles = (StarRippleParticles)Minecraft.getInstance().particleEngine.createParticle(
                        ParticleRegistry.STAR_RIPPLE_PARTICLES.get(),
                        centerPos.x,centerPos.y,centerPos.z, 0,0,0
                    );
                    if(particles!=null) {
                        particles.setScale(scale);
                    }
                }
                level.playSound(Minecraft.getInstance().getCameraEntity(), blockPos, SoundRegistry.RIPPLE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
}
