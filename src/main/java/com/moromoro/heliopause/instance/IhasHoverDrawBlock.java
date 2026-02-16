package com.moromoro.heliopause.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IhasHoverDrawBlock {
    boolean renderHoverGraphic(RenderGuiOverlayEvent event, ClientLevel level, BlockPos pos);

}
