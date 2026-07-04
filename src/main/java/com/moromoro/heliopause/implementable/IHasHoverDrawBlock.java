package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IHasHoverDrawBlock {
    boolean renderHoverGraphic(RenderGuiOverlayEvent event, ClientLevel level, BlockPos pos);

}
