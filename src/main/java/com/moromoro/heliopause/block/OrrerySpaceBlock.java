package com.moromoro.heliopause.block;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.CentralStarBlockEntity;
import com.moromoro.heliopause.particle.WhirlRingParticles;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class OrrerySpaceBlock extends Block {
    public static final IntegerProperty CoordinateX = IntegerProperty.create("coordinate_x",0,32);
    public static final IntegerProperty CoordinateZ = IntegerProperty.create("coordinate_z",0,32);
    public OrrerySpaceBlock(Properties properties) {
        super(properties
            .instabreak()
            .noCollission()
            .sound(SoundType.EMPTY)
            .noLootTable()
            .replaceable()
            .noParticlesOnBreak()
        );
        registerDefaultState(this.defaultBlockState().setValue(CoordinateX,16).setValue(CoordinateZ,16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(CoordinateX,CoordinateZ);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            //右クリックされたとき
            Heliopause.LOGGER.debug("right clicked!");
            if(level.getBlockEntity(blockPos.offset(16 - blockState.getValue(CoordinateX), 0, 16 - blockState.getValue(CoordinateZ)))
                instanceof CentralStarBlockEntity entity){
                entity.setCircumstellars(player, hand, 16 - blockState.getValue(CoordinateX),16 - blockState.getValue(CoordinateZ));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_48758_) {
        return RenderShape.INVISIBLE;
    }

    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(0,4,0,16,12,16);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
        super.animateTick(blockState, level, pos, randomSource);
        if(randomSource.nextInt(0,200)<1){
            Vec3 centerPos = pos.getCenter().add(new Vec3(0,0,0).offsetRandom(randomSource,1f).multiply(1,0.5,1));
            EndRodParticle particle =(EndRodParticle) Minecraft.getInstance().particleEngine.createParticle(
                ParticleTypes.END_ROD,
                centerPos.x(),centerPos.y(), centerPos.z(), 0,0,0
            );
            if(particle!=null) {
                particle.scale(0.5f);
                particle.setLifetime(50);
            }
        }
        /*if(randomSource.nextInt(0,500)<10){
            Vec3 centerPos = pos.offset(16 - blockState.getValue(CoordinateX), 0, 16 - blockState.getValue(CoordinateZ)).getCenter();
            WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                centerPos.x(),centerPos.y(),centerPos.z(), 0, 0, 0
            );
            ringParticle.setOrbitalElements(new Vec3(centerPos.x(),centerPos.y(),centerPos.z()),(float) Math.toRadians(-90),0,0,5,3);
            ringParticle.setPixelBasedSize(0.1f);
        }*/
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        super.onPlace(blockState, level, pos, newBlockState, isMoving);
        updateNeighbor(blockState, level, pos);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        //level.destroyBlock(pos,false);
        super.neighborChanged(blockState, level, pos, neighborBlock, neighborPos, isMoving);
        if(level.isClientSide()){
            return;
        }
        // 中心を確認
        BlockState centerState = level.getBlockState(
            pos.offset(16 - blockState.getValue(CoordinateX), 0, 16 - blockState.getValue(CoordinateZ))
        );
        if (!(centerState.getBlock() instanceof CentralStarBlock)) {
            level.destroyBlock(pos, false);
            return;
        }
        updateNeighbor(blockState, level, pos);
    }

    private void updateNeighbor(BlockState blockState, Level level, BlockPos pos) {
        // すべての隣を確認
        BlockState spaceBlockState = BlockRegistry.ORRERY_SPACE.get().defaultBlockState();
        for (Direction direction : Direction.values()) {
            BlockPos relativePos = pos.relative(direction);
            if(direction.getAxis().isVertical()){
                continue;
            }
            if(!(level.getBlockState(relativePos).getBlock() instanceof OrrerySpaceBlock) && level.getBlockState(relativePos).canBeReplaced()){
                // 中心からの距離を確認
                int neighborCoordinateX = blockState.getValue(CoordinateX) + direction.getStepX();
                int neighborCoordinateZ = blockState.getValue(CoordinateZ) + direction.getStepZ();
                if(neighborCoordinateX < 0 || neighborCoordinateZ < 0 || neighborCoordinateX > 32 || neighborCoordinateZ > 32){
                    continue;
                }
                if(Math.sqrt(Math.pow(neighborCoordinateX -16, 2) + Math.pow(neighborCoordinateZ -16, 2)) < 5 + 0.5f){
                    spaceBlockState = spaceBlockState
                        .setValue(OrrerySpaceBlock.CoordinateX,neighborCoordinateX)
                        .setValue(OrrerySpaceBlock.CoordinateZ,neighborCoordinateZ);

                    level.setBlock(relativePos, spaceBlockState,0b11);
                }
            }
        }
    }

}
