package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IHasHoverDrawEntity {
    boolean renderHoverGraphicWithEntity(RenderGuiOverlayEvent event, ClientLevel level);
    //void setWheelInput(double scrollDelta);
}
