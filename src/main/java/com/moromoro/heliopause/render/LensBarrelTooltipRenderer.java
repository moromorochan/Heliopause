package com.moromoro.heliopause.render;

import com.mojang.blaze3d.platform.Window;
import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.implementable.AbstractBlockTooltipRenderer;
import com.moromoro.heliopause.network.LensBarrelCoverageListener;
import com.moromoro.heliopause.registry.TagRegistry;
import com.moromoro.heliopause.registry.enumProperty.LensBarrelCoverageIconValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;

import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.MAX_BARREL_LENGTH;

public class LensBarrelTooltipRenderer implements AbstractBlockTooltipRenderer {
    
    private static final ForgeConfigSpec.BooleanValue INFO_ALWAYS = ConfigHolder.BARREL_BLOCK_INFO_ALWAYS;
    
    // 背景
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Heliopause.MODID, "textures/gui/coverage_check.png");
    private static final ResourceLocation COVERAGE_ICON = new ResourceLocation(Heliopause.MODID, "textures/gui/coverage_icon.png");
    private static final int TEX_WIDTH = 76;
    
    public LensBarrelTooltipRenderer(){
    
    }
    
    @Override //@OnlyIn(Dist.CLIENT)
    public boolean renderHoverGraphic(RenderGuiEvent event, BlockPos pos) {
        //Heliopause.LOGGER.debug("test barrel");
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        Level level = instance.level;
        // プレイヤー・レベル確認
        if(player == null || level == null){
            return false;
        }
        // キー状態確認
        if(!INFO_ALWAYS.get() && !instance.options.keyShift.isDown())
        {
            return false;
        }
        
        // ツールチップデータ作成
        final LensBarrelBlock.BarrelStateData barrelStateData = LensBarrelBlock.getBarrelStateData(level, pos);
        final LensBarrelBlock.CoverageWindowData coverageWindowData = LensBarrelBlock.getCoverageWindowData(barrelStateData.barrelTypeList());
        
        LensBarrelTooltipRenderer.renderAccuracyGui(event, instance, barrelStateData, coverageWindowData, false);
        return true;
    }
    
    public static void renderAccuracyGui(RenderGuiEvent event, Minecraft instance, final LensBarrelBlock.BarrelStateData barrelStateData, final LensBarrelBlock.CoverageWindowData coverageWindowData, boolean fromEntity) {
        // 鏡筒の最大長さに応じたGUIを作成
        Window window = event.getWindow();
        GuiGraphics graphics = event.getGuiGraphics();
        
        List<BlockState> barrelTypeList = barrelStateData.barrelTypeList();
        final boolean hasConcentrator = barrelStateData.hasConcentrator();
        
        List<Set<LensBarrelCoverageListener.BarrelCoverageData>> coverageList = coverageWindowData.coverageList();
        Set<LensBarrelCoverageListener.BarrelCoverageData> totalCoverage = coverageWindowData.totalCoverage();
        
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
            Set<LensBarrelCoverageListener.BarrelCoverageData> coverage = coverageList.get(i);
            
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
            for (LensBarrelCoverageListener.BarrelCoverageData data : coverage) {
                LensBarrelCoverageIconValue icon = LensBarrelCoverageIconValue.fromString(data.icon());
                if(icon == null){
                    continue;
                }
                int[] color = data.icon_color();
                graphics.setColor(color[0]/255f, color[1]/255f, color[2]/255f, 1f);
                graphics.blit(COVERAGE_ICON, coverageX + coverageOffsetX, slotHeight + 4,icon.ordinal() * 7,0,7,7, 32, 16);
                graphics.setColor(1f, 1f, 1f, 1f);
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
            List<LensBarrelCoverageListener.BarrelCoverageData> totalCoverageList = totalCoverage.stream().toList();
            for (int i = 0; i < totalCoverage.size(); i++) {
                LensBarrelCoverageListener.BarrelCoverageData data = totalCoverageList.get(i);
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
