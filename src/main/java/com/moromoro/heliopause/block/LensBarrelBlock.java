package com.moromoro.heliopause.block;

import com.moromoro.heliopause.implementable.AbstractBlockTooltipRenderer;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.implementable.IHasHoverDrawBlock;
import com.moromoro.heliopause.network.LensBarrelCoverageListener;
import com.moromoro.heliopause.network.LensBarrelCoverageListener.BarrelCoverageData;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.render.LensBarrelTooltipRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.MAX_BARREL_LENGTH;

public class LensBarrelBlock extends Block implements IHasHoverDrawBlock {
    
    // ツールチップ用データ
    //private BlockPos localTooltipPos = null;
    //private BarrelStateData localTooltipData = null;
    //private final long lastDataUpdated = 0L;
    //private final boolean requestData = false;

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
        /*TagKey<Block> thisKey = getBarrelType(state);
        if(thisKey == null){
            return;
        }*/
        BlockState newState = state;
        if(pos.above().equals(fromPos)){
            // 上側が鏡筒
            //if(level.getBlockState(fromPos).getBlock() instanceof LensBarrelBlock neighborBlock){
                //neighborBarrelChanged();
                // 上側が同種タグの鏡筒なら
                //TagKey<Block> aboveKey = getBarrelType(level.getBlockState(fromPos));
                //newState=newState.setValue(TOP, !thisKey.equals(aboveKey));
                newState=newState.setValue(TOP, !level.getBlockState(fromPos).getBlock().equals(this));
            //}
        }

        if(pos.below().equals(fromPos)){
            // 下側が鏡筒
            //if(level.getBlockState(fromPos).getBlock() instanceof LensBarrelBlock neighborBlock) {
                //neighborBarrelChanged();
                // 下側が同種タグの鏡筒なら
                //TagKey<Block> belowKey = getBarrelType(level.getBlockState(fromPos));
                //newState = newState.setValue(BOTTOM, !thisKey.equals(belowKey));
                newState=newState.setValue(BOTTOM, !level.getBlockState(fromPos).getBlock().equals(this));
            //}
        }
        level.setBlock(pos, newState, 3);
    }

    /*public static @Nullable TagKey<Block> getBarrelType(BlockState blockState){
        List<TagKey<Block>> tagList = blockState.getTags().toList();
        for (TagKey<Block> tagKey : tagList) {
            String blockTagId = tagKey.location().toString();
            // 収斂帯域幅の登録から確認
            for (BarrelCoverageData data : LensBarrelCoverageListener.DATA.values()) {
                // タグ一致を確認
                for (String registeredTag : data.block()) {
                    if (blockTagId.equals(registeredTag)) {
                        return tagKey;
                    }
                }
            }
        }
        return null;
    }*/

    public static Set<BarrelCoverageData> getBarrelConcentrationCoverage(Block block){
        Set<BarrelCoverageData> coverageData = new HashSet<>();
        String blockId = ForgeRegistries.BLOCKS.getKey(block).toString();

        // 収斂帯域幅の登録から確認
        for (BarrelCoverageData data : LensBarrelCoverageListener.DATA.values()) {
            // id一致を確認
            for (String registeredTag : data.block()) {
                if (blockId.equals(registeredTag)) {
                    coverageData.add(data);
                    break;
                }
            }
        }

        return coverageData;
    }

    /*public static double getBarrelAccuracy(TagKey<Block> barrelType){
        if(barrelType != null){
            return LENS_BARREL_ACCURACIES.get(barrelType).get();
        }
        return 0;
    }*/
    
    public record BarrelStateData(
        List<BlockState> barrelTypeList,
        boolean hasConcentrator
    ){}
    public record CoverageWindowData(
        List<Set<BarrelCoverageData>> coverageList,
        Set<BarrelCoverageData> totalCoverage,
        int barrelSlotHeight,
        int windowHeight
    ){}
    
    public static LensBarrelBlock.BarrelStateData getBarrelStateData(@NotNull Level level, @NotNull BlockPos blockPos) {
        /*if(!INFO_ALWAYS.get() && !Minecraft.getInstance().options.keyShift.isDown())
        {
            //localTooltipData = null;
            return null;
        }*/
        
        boolean hasConcentrator = false;
        
        // 鏡筒の配列を作成
        
        // 上下の接続状況確認
        List<BlockState> barrelTypeList = new ArrayList<>();
        // 自身を追加
        barrelTypeList.add(level.getBlockState(blockPos));
        
        // 頂上まで探索
        int topPosY = 0;
        while (topPosY <= MAX_BARREL_LENGTH){
            ++topPosY;
            BlockState aboveState = level.getBlockState(blockPos.above(topPosY));
            //TagKey<Block> barrelType = getBarrelType(aboveState);
            if(!(aboveState.getBlock() instanceof LensBarrelBlock)/*barrelType == null*/){
                break;
            }
            barrelTypeList.add(aboveState);
        }
        
        // 下→上順から上→下順へ
        Collections.reverse(barrelTypeList);
        
        int bottomPosY = 0;
        while (bottomPosY <= MAX_BARREL_LENGTH){
            ++bottomPosY;
            BlockState belowState = level.getBlockState(blockPos.below(bottomPosY));
            //TagKey<Block> barrelType = getBarrelType(belowState);
            if(!(belowState.getBlock() instanceof LensBarrelBlock)/*barrelType == null*/){
                if(belowState.is(BlockRegistry.CONCENTRATOR.get())){
                    hasConcentrator = true;
                }
                break;
            }
            barrelTypeList.add(belowState);
        }
        
        return new BarrelStateData(barrelTypeList, hasConcentrator);
    }
    
    public static @NotNull LensBarrelBlock.CoverageWindowData getCoverageWindowData(List<BlockState> barrelTypeList) {
        // 鏡筒の観測波長を作成
        List<Set<BarrelCoverageData>> coverageList = new ArrayList<>();
        
        for (BlockState state : barrelTypeList) {
            Set<BarrelCoverageData> coverage = new HashSet<>(getBarrelConcentrationCoverage(state.getBlock()));
            coverageList.add(coverage);
        }
        int barrelSlotHeight = 5 + (MAX_BARREL_LENGTH + 1) * 16;
        
        // 合計波長表示 長すぎるときは表示しない
        Set<BarrelCoverageData> totalCoverage = new HashSet<>();
        int windowHeight = barrelSlotHeight + 2;
        if (!coverageList.isEmpty() && barrelTypeList.size() <= MAX_BARREL_LENGTH) {
            totalCoverage.addAll(coverageList.get(0));
            for (int i = 1; i < coverageList.size(); i++) {
                totalCoverage.retainAll(coverageList.get(i));
            }
            windowHeight += totalCoverage.size() * 10;
        }
        
        return new CoverageWindowData(coverageList, totalCoverage, barrelSlotHeight, windowHeight);
    }
    
    @Override
    public AbstractBlockTooltipRenderer getRendererHoverGraphic() {
        return new LensBarrelTooltipRenderer();
    }
}
