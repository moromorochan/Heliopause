package com.moromoro.heliopause.block;

import com.mojang.blaze3d.platform.Window;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.instance.IhasHoverDrawBlock;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.moromoro.ConfigHolder.LENS_BARREL_ACCURACIES;
import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.MAX_BARREL_LENGTH;

public class LensBarrelBlock extends Block implements IhasHoverDrawBlock {

    // 背景
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Heliopause.MODID, "textures/gui/lens_barrel_accuracy_check.png");
    private static final int TEX_WIDTH = 76;

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
        TagKey<Block> thisKey = getBarrelType(state);
        if(thisKey == null){
            return;
        }
        BlockState newState = state;
        if(pos.above().equals(fromPos)){
            // 上側が同種タグの鏡筒なら
            TagKey<Block> aboveKey = getBarrelType(level.getBlockState(fromPos));
            newState=newState.setValue(TOP, !thisKey.equals(aboveKey));
        }

        if(pos.below().equals(fromPos)){
            // 下側が同種タグの鏡筒なら
            TagKey<Block> belowKey = getBarrelType(level.getBlockState(fromPos));
            newState=newState.setValue(BOTTOM, !thisKey.equals(belowKey));
        }
        level.setBlock(pos, newState, 3);
    }

    public static @Nullable TagKey<Block> getBarrelType(BlockState blockState){
        List<TagKey<Block>> tagList = blockState.getTags().toList();
        for (TagKey<Block> tagKey : tagList) {
            if(LENS_BARREL_ACCURACIES.containsKey(tagKey)){
                return tagKey;
            }
        }
        return null;
    }

    public static double getBarrelAccuracy(TagKey<Block> barrelType){
        if(barrelType != null){
            return LENS_BARREL_ACCURACIES.get(barrelType).get();
        }
        return 0;
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

        boolean hasConcentrator = false;

        // 上下の接続状況確認
        List<BlockState> barrelTypeList = new ArrayList<>();
        // 自身を追加
        barrelTypeList.add(level.getBlockState(pos));

        // 頂上まで探索
        int topPosY = 0;
        while (topPosY <= MAX_BARREL_LENGTH){
            ++topPosY;
            BlockState aboveState = level.getBlockState(pos.above(topPosY));
            TagKey<Block> barrelType = getBarrelType(aboveState);
            if(barrelType == null){
                break;
            }
            barrelTypeList.add(aboveState);
        }

        // 下→上順から上→下順へ
        Collections.reverse(barrelTypeList);

        int bottomPosY = 0;
        while (bottomPosY <= MAX_BARREL_LENGTH){
            ++bottomPosY;
            BlockState belowState = level.getBlockState(pos.below(bottomPosY));
            TagKey<Block> barrelType = getBarrelType(belowState);
            if(barrelType == null){
                if(belowState.is(BlockRegistry.CONCENTRATOR.get())){
                    hasConcentrator = true;
                }
                break;
            }
            barrelTypeList.add(belowState);
        }

        renderAccuracyGUI(event, instance, barrelTypeList, hasConcentrator, false);
        return true;
    }

    public static void renderAccuracyGUI(RenderGuiOverlayEvent event, Minecraft instance, List<BlockState> barrelTypeList, boolean hasConcentrator, boolean fromEntity) {
        // 鏡筒の最大長さに応じたGUIを作成
        Window window = event.getWindow();
        GuiGraphics graphics = event.getGuiGraphics();

        final int texHeight = 19 + 16 * MAX_BARREL_LENGTH + 48;
        final int x = (window.getGuiScaledWidth())/2 + 8;
        final int y = (window.getGuiScaledHeight()) - texHeight - 50;

        final int centerX = x + TEX_WIDTH/2;
        final int barrelX = x + 11;
        final int accuracyX = x + 55;
        final int bottomY = y + texHeight;

        graphics.drawCenteredString(instance.font, "鏡筒の情報", centerX, y- instance.font.lineHeight, 0xFFFFFF);

        graphics.blit(BACKGROUND, x,y,0,0, TEX_WIDTH,19, 128, 128);
        for (int i = 0; i < MAX_BARREL_LENGTH + 1; i++) {
            graphics.blit(BACKGROUND, x,y + 16 * (i+1),0,23, TEX_WIDTH,16, 128, 128);
        }
        graphics.blit(BACKGROUND, x,y + 16 * (MAX_BARREL_LENGTH + 1),0,43, TEX_WIDTH, 48, 128, 128);

        int slotHeight = y + 5;
        // 足りない分を空スロットで追加
        int emptyLength = MAX_BARREL_LENGTH - barrelTypeList.size();
        boolean isTooLong = emptyLength < 0;
        for (int slot = 0; slot < emptyLength; slot++) {
            graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH + 26, 0, 26, 16, 128, 128);
            slotHeight += 16;
        }
        double resultAccuracy = 1.0;

        boolean canAssemble = barrelTypeList.size() >= 2;

        // 鏡筒を追加
        int iterateLength = Math.min(barrelTypeList.size(), MAX_BARREL_LENGTH + 1);
        for (int i = 0; i < iterateLength; i++) {
            BlockState blockState = barrelTypeList.get(i);
            // 種類を確認
            boolean isMirrorPart = false;
            if (blockState.is(TagRegistry.Blocks.SECOND_MIRROR)) {
                graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH, 0, 26, 16, 128, 128);
                isMirrorPart = true;
                if(i != 0){
                    canAssemble = false;
                }
            }
            if (blockState.is(TagRegistry.Blocks.MAIN_MIRROR)) {
                graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH, 32, 26, 16, 128, 128);
                isMirrorPart = true;
                if(i != barrelTypeList.size() - 1){
                    canAssemble = false;
                }
            }
            if (!isMirrorPart) {
                if(i == 0 || i == barrelTypeList.size()-1){
                    canAssemble = false;
                }
                graphics.blit(BACKGROUND, barrelX, slotHeight, TEX_WIDTH, 16, 26, 16, 128, 128);
            }
            // 精度を記載
            double accuracy = getBarrelAccuracy(getBarrelType(blockState));
            resultAccuracy *= accuracy;
            if(i < MAX_BARREL_LENGTH){
                graphics.drawCenteredString(instance.font, String.format("%.2f", accuracy), accuracyX, slotHeight + 4, 0xFFFFFF);
                graphics.drawString(instance.font, "×", accuracyX - instance.font.width("×") / 2, slotHeight + 12, 0x808080, false);
                slotHeight += 16;
            }
        }
        if(!isTooLong){
            // 経緯台
            if(hasConcentrator){
                graphics.blit(BACKGROUND, barrelX, slotHeight+1, TEX_WIDTH, 48, 26, 16, 128, 128);
            }else{
                graphics.blit(BACKGROUND, barrelX, slotHeight+1, TEX_WIDTH + 26, 16, 26, 16, 128, 128);
            }
            graphics.drawCenteredString(instance.font, String.valueOf(barrelTypeList.size()), accuracyX, slotHeight + 4, 0xFFFFFF);
        }else{
            // 長さと結果
            graphics.drawCenteredString(instance.font, String.valueOf(barrelTypeList.size()), accuracyX, slotHeight + 4, 0xFF8080);
        }

        resultAccuracy *= barrelTypeList.size();

        if(!isTooLong && hasConcentrator && canAssemble){
            graphics.drawCenteredString(instance.font, "合計: "+String.format("%.2f", resultAccuracy), centerX, bottomY-43, 0x8080FF);

            graphics.drawString(instance.font, Component.literal("[").append(instance.options.keyUse.getTranslatedKeyMessage()).append("] :"), x + 5, bottomY-28, 0xFFFFFF);
            if(fromEntity) {
                graphics.drawString(instance.font, "組み立て解除", x + 5, bottomY-16, 0xFFFFFF);
            }
            else{
                graphics.drawString(instance.font, "組み立て可能!", x + 5, bottomY -16, 0xFFFFFF);
            }
        }else{
            graphics.drawCenteredString(instance.font, "合計: "+String.format("%.2f", resultAccuracy), centerX, bottomY -43, 0x808080);
            int textWidth = TEX_WIDTH - 10;
            Component component;

            if (isTooLong) {
                component = Component.literal("鏡筒が長すぎます");
            } else if (!canAssemble) {
                component = Component.literal("鏡の配置が無効です");
            } else {
                component = Component.literal("最下部に経緯台が必要です");
            }
            String fullText = component.getString();
            // 1行目を切り出す
            String firstLine = instance.font.plainSubstrByWidth(fullText, textWidth);
            graphics.drawString(instance.font, firstLine, x + 5, bottomY -28, 0xFF8080);
            String secondLine;
            // 残りを2行目に描画
            if (firstLine.length() < fullText.length()) {
                secondLine = fullText.substring(firstLine.length());
                graphics.drawString(instance.font, secondLine, x + 5, bottomY -16, 0xFF8080);
            }

        }
    }
}
