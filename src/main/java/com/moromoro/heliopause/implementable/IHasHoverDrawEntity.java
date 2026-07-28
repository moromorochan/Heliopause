package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.event.RenderGuiEvent;

public interface IHasHoverDrawEntity {
    boolean renderHoverGraphicWithEntity(RenderGuiEvent event, ClientLevel level);
}
