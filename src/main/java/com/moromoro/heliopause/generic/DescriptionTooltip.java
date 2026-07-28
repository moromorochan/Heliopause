package com.moromoro.heliopause.generic;

import com.moromoro.heliopause.compat.jei.StarlightConcentrationCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class DescriptionTooltip {
    public static void drawDescriptionTooltip(GuiGraphics guiGraphics, int windowWidth, MutableComponent descriptionComponent, int maxWidth, double mouseX, int mouseY) {
        final Font font = Minecraft.getInstance().font;
        
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenMouseX = (int) (mouseX + (double) (screenWidth - windowWidth) /2);
        TooltipTransform tooltipTransform = getTooltipTransform(screenWidth, screenMouseX, 10, maxWidth);
        List<FormattedCharSequence> coverageTooltip = new ArrayList<>();
        String[] lines = descriptionComponent.getString().split("\n", -1);
        int tooltipWidth = 0;
        for (String line : lines) {
            coverageTooltip.addAll(font.split(Component.literal(line), tooltipTransform.width()));
            for (FormattedCharSequence component : coverageTooltip) {
                tooltipWidth = Math.max(tooltipWidth, font.width(component));
            }
        }
        
        if( tooltipTransform.isLeft()) {
            guiGraphics.renderTooltip(font, coverageTooltip, (int) mouseX - tooltipWidth - 20, mouseY);
        }else{
            guiGraphics.renderTooltip(font, coverageTooltip, (int) mouseX, mouseY);
        }
    }
    
    public record TooltipTransform(int width, boolean isLeft) {}
    private static TooltipTransform getTooltipTransform(int screenWidth, int screenMouseX, int padding, int maxWidth) {
        // 右側に確保できる幅
        int availableRight = Math.max(0, screenWidth - screenMouseX - padding);
        // 左側に確保できる幅
        int availableLeft = Math.max(0, screenMouseX - padding);
        
        // 右に収まるか
        if (availableRight >= maxWidth) {
            return new TooltipTransform(maxWidth, false);
        }
        // 左に収まるか
        if (availableLeft >= maxWidth) {
            return new TooltipTransform(maxWidth, true);
        }
        
        // どちらにも収まらない場合
        if (availableRight >= availableLeft) {
            return new TooltipTransform(availableRight, false);
        } else {
            return new TooltipTransform(availableLeft, true);
        }
    }
}
