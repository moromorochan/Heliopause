package com.moromoro.heliopause.implementable;

import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RenderGuiEvent;

public interface AbstractBlockTooltipRenderer {
    boolean renderHoverGraphic(RenderGuiEvent event, BlockPos pos);
}
