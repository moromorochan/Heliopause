package com.moromoro.heliopause.block;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.enumProperty.LensBarrelCoverageIconValue;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.implementable.IHasHoverDrawBlock;
import com.moromoro.heliopause.recipe.LensBarrelCoverageListener;
import com.moromoro.heliopause.recipe.LensBarrelCoverageListener.BarrelCoverageData;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.MAX_BARREL_LENGTH;

public class LensBarrelBlock extends Block implements IHasHoverDrawBlock {

    private static final ForgeConfigSpec.BooleanValue INFO_ALWAYS = ConfigHolder.BARREL_BLOCK_INFO_ALWAYS;

    // 背景
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Heliopause.MODID, "textures/gui/coverage_check.png");
    private static final ResourceLocation COVERAGE_ICON = new ResourceLocation(Heliopause.MODID, "textures/gui/coverage_icon.png");
    private static final int TEX_WIDTH = 76;
    
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
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();

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
    
    private @Nullable LensBarrelBlock.BarrelStateData getBarrelStateData(@NotNull Level level, @NotNull BlockPos blockPos) {
        if(!INFO_ALWAYS.get() && !Minecraft.getInstance().options.keyShift.isDown())
        {
            //localTooltipData = null;
            return null;
        }
        
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
    public boolean renderHoverGraphic(RenderGuiOverlayEvent event, ClientLevel level, BlockPos pos) {
        //Heliopause.LOGGER.debug("test barrel");
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        // プレイヤー確認
        if(player == null){
            return false;
        }
        // キー状態確認
        if(!INFO_ALWAYS.get() && !instance.options.keyShift.isDown())
        {
            return false;
        }
        
        // ツールチップデータ作成
        final BarrelStateData barrelStateData = getBarrelStateData(level, pos);
        if(barrelStateData == null){
            return false;
        }
        final CoverageWindowData coverageWindowData = getCoverageWindowData(barrelStateData.barrelTypeList());
        
        renderAccuracyGui(event, instance, barrelStateData, coverageWindowData, false);
        return true;
    }

    public static void renderAccuracyGui(RenderGuiOverlayEvent event, Minecraft instance, final BarrelStateData barrelStateData, final CoverageWindowData coverageWindowData, boolean fromEntity) {
        // 鏡筒の最大長さに応じたGUIを作成
        Window window = event.getWindow();
        GuiGraphics graphics = event.getGuiGraphics();
        
        List<BlockState> barrelTypeList = barrelStateData.barrelTypeList();
        final boolean hasConcentrator = barrelStateData.hasConcentrator();
        
        List<Set<BarrelCoverageData>> coverageList = coverageWindowData.coverageList();
        Set<BarrelCoverageData> totalCoverage = coverageWindowData.totalCoverage();
        
        final int texHeight = 19 + 16 * MAX_BARREL_LENGTH + 48;
        final int x = (window.getGuiScaledWidth()) / 2 + 8;
        final int y = (window.getGuiScaledHeight()) - texHeight - 70;
        
        final int SLOT_START_Y = y + 5;
        final int TOTAL_COVERAGE_START_Y = y + coverageWindowData.barrelSlotHeight() + 1;
        final int MESSAGE_START_Y = y + coverageWindowData.windowHeight() + 5;
        
        final int centerX = x + TEX_WIDTH / 2;
        final int barrelX = x + 42;
        final int coverageX = x + 22;
        final boolean isTooLong = barrelTypeList.size() > MAX_BARREL_LENGTH;

        graphics.drawCenteredString(instance.font, Component.translatable("gui.heliopause.lens_barrel_coverage_check.title"), centerX, y- instance.font.lineHeight, 0xFFFFFF);
        // 鏡筒の長さ分の背景を描画
        graphics.blit(BACKGROUND, x, y,0,0, TEX_WIDTH, coverageWindowData.barrelSlotHeight() + (isTooLong? -1:-17));
        if(!isTooLong) {
            graphics.blit(BACKGROUND, x, TOTAL_COVERAGE_START_Y - 18,TEX_WIDTH,0, TEX_WIDTH,  16 + totalCoverage.size() * 10);
        }
        // 収斂帯域幅分の背景を描画
        /*for (int i = TOTAL_COVERAGE_START_Y; i < totalCoverage.size() * 10 + TOTAL_COVERAGE_START_Y; i+=16) {
            graphics.blit(BACKGROUND, x, i,0,15, TEX_WIDTH,16, 128, 128);
        }*/
        //graphics.blit(BACKGROUND, x, MESSAGE_START_Y -7,0,35, TEX_WIDTH, 32, 128, 128);
        graphics.blit(BACKGROUND, x, MESSAGE_START_Y -8,TEX_WIDTH,66, TEX_WIDTH, 42);
        
        // 鏡筒の描画
        boolean canAssemble = barrelTypeList.size() >= 2;
        int iterateLength = Math.min(barrelTypeList.size(), MAX_BARREL_LENGTH + 1);
        
        // 足りない分を空スロットで追加
        int emptyLength = MAX_BARREL_LENGTH - barrelTypeList.size();
        for (int slot = 0; slot < emptyLength; slot++) {
            graphics.blit(BACKGROUND, barrelX, SLOT_START_Y + slot * 16, TEX_WIDTH + 26, 0, 26, 16, 128, 128);
        }
        for (int i = 0; i < iterateLength; i++) {
            BlockState blockState = barrelTypeList.get(i);
            Set<BarrelCoverageData> coverage = coverageList.get(i);
            
            final int slotHeight = SLOT_START_Y + i * 16 + Math.max(MAX_BARREL_LENGTH - barrelTypeList.size(),  0) * 16;
            // 種類を確認
            boolean isMirrorPart = false;
            if (blockState.is(TagRegistry.Blocks.SECOND_MIRROR)) {
                graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH*2, 0, 26, 16);
                isMirrorPart = true;
                if(i != 0){
                    canAssemble = false;
                }
            }
            if (blockState.is(TagRegistry.Blocks.MAIN_MIRROR)) {
                graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH*2, 32, 26, 16);
                isMirrorPart = true;
                if(i != barrelTypeList.size() - 1){
                    canAssemble = false;
                }
            }
            if (!isMirrorPart) {
                if(i == 0 || i == barrelTypeList.size()-1){
                    canAssemble = false;
                }
                graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH*2, 16, 26, 16);
            }
            // 観測波長の描画
            int coverageOffsetX = -(coverage.size() * 8) / 2;
            for (BarrelCoverageData data : coverage) {
                LensBarrelCoverageIconValue icon = LensBarrelCoverageIconValue.fromString(data.icon());
                if(icon == null){
                    continue;
                }
                int[] color = data.icon_color();
                RenderSystem.setShaderColor(color[0]/255f, color[1]/255f, color[2]/255f, 1f);
                graphics.blit(COVERAGE_ICON, coverageX + coverageOffsetX, slotHeight + 4,icon.ordinal() * 7,0,7,7, 32, 16);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                coverageOffsetX += 8;
            }
            if(i < iterateLength - 1){
                graphics.drawString(instance.font, "×", coverageX - instance.font.width("×") / 2, slotHeight + 12, 0x808080, false);
            }
        }
        if(!isTooLong){
            // 経緯台
            if(hasConcentrator){
                graphics.blit(BACKGROUND, barrelX, TOTAL_COVERAGE_START_Y-18, TEX_WIDTH * 2, 48, 26, 16);
            }/*else{
                graphics.blit(BACKGROUND, barrelX, TOTAL_COVERAGE_START_Y-18, TEX_WIDTH + 26, 16, 26, 16, 128, 128);
            }*/
            //graphics.drawCenteredString(instance.font, String.valueOf(barrelTypeList.size()), coverageX, slotHeight + 4, 0xFFFFFF);
            //graphics.blit(BACKGROUND, coverageX-5, TOTAL_COVERAGE_START_Y -17, 102, 32, 9,11, 128, 128);

            // 収斂帯域幅の合計を描画
            List<BarrelCoverageData> totalCoverageList = totalCoverage.stream().toList();
            for (int i = 0; i < totalCoverage.size(); i++) {
                BarrelCoverageData data = totalCoverageList.get(i);
                int slotHeight = TOTAL_COVERAGE_START_Y + i * 10;
                    LensBarrelCoverageIconValue icon = LensBarrelCoverageIconValue.fromString(data.icon());
                if (icon == null) {
                    continue;
                }
                int[] color = data.icon_color();
                graphics.setColor(color[0] / 255f, color[1] / 255f, color[2] / 255f, 1f);
                graphics.blit(COVERAGE_ICON, x + 6, slotHeight, icon.ordinal() * 7, 0, 7, 7, 32, 16);
                graphics.setColor(1f, 1f, 1f, 1f);
                // 名前を切り出して描画
                String coverageName = Component.translatable("gui.heliopause.lens_barrel_coverage." + data.name()).getString();
                if (instance.font.width(coverageName) >= 55) {
                    while (instance.font.width(coverageName) > 50) {
                        coverageName = coverageName.substring(0, coverageName.length() - 1);
                    }
                    coverageName = coverageName + "...";
                }
                graphics.drawString(instance.font, coverageName, x + 16, slotHeight, 0xFFFFFF);
            }
        }
        
        if(!isTooLong && hasConcentrator && canAssemble){
            
            graphics.drawString(instance.font, Component.literal("[").append(instance.options.keyUse.getTranslatedKeyMessage()).append("]"), x + 4, MESSAGE_START_Y, 0xFFFFFF);
            if(fromEntity) {
                graphics.drawCenteredString(instance.font, Component.translatable("gui.heliopause.lens_barrel_coverage_check.disassemble"), centerX, MESSAGE_START_Y +15, 0xFFFFFF);
            }
            else{
                graphics.drawCenteredString(instance.font, Component.translatable("gui.heliopause.lens_barrel_coverage_check.assemble"), centerX, MESSAGE_START_Y +15, 0xFFFFFF);
            }
        }else{
            //int textWidth = TEX_WIDTH - 10;
            Component component;

            if (isTooLong) {
                component = Component.translatable("gui.heliopause.lens_barrel_coverage_check.invalid_length");
            } else if (!canAssemble) {
                component = Component.translatable("gui.heliopause.lens_barrel_coverage_check.invalid_mirror");
            } else {
                component = Component.translatable("gui.heliopause.lens_barrel_coverage_check.invalid_concentrator");
            }
            
            String[] segments = component.getString().split("\n", -1);
            int messageHeight = MESSAGE_START_Y + 1 + instance.font.lineHeight * (3 - segments.length)/2;
            for (int i = 0; i < segments.length; i++) {
                String seg = segments[i];
                graphics.drawCenteredString(instance.font, seg, centerX,messageHeight + i * instance.font.lineHeight, 0x808080);
            }
            
            /*String remaining = component.getString();
            // 1行目を切り出す
            String firstLine = instance.font.plainSubstrByWidth(remaining, textWidth);
            graphics.drawString(instance.font, firstLine, x + 4, MESSAGE_START_Y, 0x808080);
            if (firstLine.length() < remaining.length()) {
                remaining = remaining.substring(firstLine.length());
                
                // 2行目を切り出す
                String secondLine = instance.font.plainSubstrByWidth(remaining, textWidth);
                graphics.drawString(instance.font, secondLine, x + 4, MESSAGE_START_Y + 12, 0x808080);
                
                // 残りを3行目に描画
                if(secondLine.length() < remaining.length()){
                    String thirdLine = remaining.substring(secondLine.length());
                    graphics.drawString(instance.font, thirdLine, x + 4, MESSAGE_START_Y + 22, 0x808080);
                }
            }*/
        }
    }
}
