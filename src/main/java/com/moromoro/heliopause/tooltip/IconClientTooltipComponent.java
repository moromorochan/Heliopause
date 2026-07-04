package com.moromoro.heliopause.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class IconClientTooltipComponent implements ClientTooltipComponent {
    private final static int ICON_TEXT_SPACING = 2;
    private final IconTooltipComponent component;
    
    public IconClientTooltipComponent(IconTooltipComponent component) {
        this.component = component;
    }
    
    @Override
    public int getHeight() {
        return Math.max(10, component.data().height());
    }
    
    @Override
    public int getWidth(Font font) {
        return font.width(component.text()) + component.data().width() + ICON_TEXT_SPACING;
    }
    
    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        
        IconTooltipComponent.IconData data = component.data();
        
        int[] color = data.color();
        graphics.setColor(color[0]/255f, color[1]/255f, color[2]/255f, 1f);
        graphics.blit(data.texture(), x, y, data.u(), data.v(), data.width(), data.height(), data.textureWidth(), data.textureHeight());
        
        graphics.setColor(1f, 1f, 1f, 1f);
        
        graphics.drawString(font, component.text(), x + data.width() + ICON_TEXT_SPACING, y, 0xFFFFFF);
    }
}
