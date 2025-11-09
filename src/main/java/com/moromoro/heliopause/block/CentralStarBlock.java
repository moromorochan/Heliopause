package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.CentralStarBlockEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CentralStarBlock extends BaseEntityBlock {

    public CentralStarBlock(Properties properties) {
        super(properties
            .noOcclusion()
            .noParticlesOnBreak()
            .pushReaction(PushReaction.IGNORE)
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CentralStarBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.CENTRAL_STAR_BE.get(),
            (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
        );
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
        super.animateTick(blockState, level, pos, randomSource);

        //パーティクル生成
        if(level.isClientSide()){
            if(level.getBlockEntity(pos) instanceof CentralStarBlockEntity entity){
                for (CircumstellarIngredient ingredient : entity.getCircumstellars()) {
                    if(randomSource.nextInt(0,2)<1){
                        float orbitalRadius = ingredient.getOrbitalRadius();
                        Vec3 orbitalCartPos = pos.getCenter().add(CentralStarBlockEntity.getCartesianCoordinates(orbitalRadius, ingredient.getRevolutionOffset()));
                        EndRodParticle particle = (EndRodParticle) Minecraft.getInstance().particleEngine.createParticle(
                            ParticleTypes.END_ROD,
                            orbitalCartPos.x(),orbitalCartPos.y(), orbitalCartPos.z(), 0,0,0
                        );
                        if(particle!=null) {
                            particle.setLifetime(100);
                            particle.setPower(0.90f);
                        }
                    }
                }
            }
        }
    }

    //破壊時に中身のルートテーブルを参照

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        //ブロックが破壊されるとき
        if(!blockState.is(newBlockState.getBlock()) && !level.isClientSide()){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof CentralStarBlockEntity starBlockEntity){
                BlockState centerState = starBlockEntity.getCenterBlockState();
                //ルートテーブルを取り出す
                LootParams.Builder builder = (new LootParams.Builder((ServerLevel) level))
                    .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                    .withParameter(LootContextParams.BLOCK_STATE, centerState)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
                //ドロップ
                List<ItemStack> drops = centerState.getDrops(builder);
                for (ItemStack drop : drops) {
                    popResource(level, pos, drop);
                }
            }
        }
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        super.onPlace(blockState, level, pos, newBlockState, isMoving);
        BlockState spaceBlockState = BlockRegistry.ORRERY_SPACE.get().defaultBlockState();

        //四方にインタラクト用ブロックを生成
        for (Direction direction : Direction.values()) {
            if(direction.getAxis().isVertical()){
                continue;
            }
            spaceBlockState = spaceBlockState
                .setValue(OrrerySpaceBlock.CoordinateX,16 + direction.getStepX())
                .setValue(OrrerySpaceBlock.CoordinateZ,16 + direction.getStepZ());
            if(level.getBlockState(pos.relative(direction)).canBeReplaced()) {
                level.setBlock(pos.relative(direction), spaceBlockState,0b11);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if(level.isClientSide()){
            return;
        }
        if(neighborPos.getY()!=pos.getY()){
            return;
        }
        if(level.getBlockState(neighborPos).canBeReplaced()){
            BlockState spaceBlockState = BlockRegistry.ORRERY_SPACE.get().defaultBlockState();
            spaceBlockState = spaceBlockState
                .setValue(OrrerySpaceBlock.CoordinateX,16 - (pos.getX() - neighborPos.getX()))
                .setValue(OrrerySpaceBlock.CoordinateZ,16 - (pos.getZ() - neighborPos.getZ()));

            level.setBlock(neighborPos, spaceBlockState,0b11);
        }
    }
}
