package com.moromoro.heliopause.tooltip;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record IconTooltipComponent(String text, IconData data) implements TooltipComponent {
    public record IconData(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int[] color){
    }
}