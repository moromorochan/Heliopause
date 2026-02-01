package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LensBarrelBlock extends Block {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    //public static final VoxelShape outlineShape = Block.box(-4f/16f,0f,-4f/16f,20f/16f,1f,20f/16f);

    public LensBarrelBlock(Properties properties) {
        super(properties);
    }

    /*@Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return outlineShape;
    }*/

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(TOP,BOTTOM);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(player.getItemInHand(hand).isEmpty()) {
            if(!level.isClientSide()) {
                sendAssemble(level, pos);
            }
            return InteractionResult.sidedSuccess(!level.isClientSide());
        }
        return super.use(blockState, level, pos, player, hand, hitResult);
    }

    private void sendAssemble(Level level, BlockPos pos){
        BlockPos belowPos = pos.below();
        while(level.getBlockState(belowPos).getBlock() instanceof LensBarrelBlock){
            belowPos = belowPos.below();
        }
        if(level.getBlockEntity(belowPos) instanceof ConcentratorBlockEntity concentratorBlockEntity){
            concentratorBlockEntity.assemble();
        }
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);

        // 自身の種類を取得
        TagKey<Block> thisKey = getBarrelType(level, pos);
        if(thisKey == null){
            return;
        }
        BlockState newState = state;
        if(pos.above().equals(fromPos)){
            // 上側が同種タグの鏡筒なら
            TagKey<Block> aboveKey = getBarrelType(level, fromPos);
            newState=newState.setValue(TOP, !thisKey.equals(aboveKey));
        }

        if(pos.below().equals(fromPos)){
            // 下側が同種タグの鏡筒なら
            TagKey<Block> belowKey = getBarrelType(level, fromPos);
            newState=newState.setValue(BOTTOM, !thisKey.equals(belowKey));
        }
        level.setBlock(pos, newState, 3);
    }

    private@Nullable TagKey<Block> getBarrelType(Level level, BlockPos pos){
        List<TagKey<Block>> tagList = level.getBlockState(pos).getTags().toList();
        if(tagList.contains(TagRegistry.Blocks.WOODEN_LENS_BARREL)){
            return TagRegistry.Blocks.WOODEN_LENS_BARREL;
        }
        return null;
    }
}
