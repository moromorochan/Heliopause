package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IHasHoverDraw {
    boolean renderHoverGraphic(RenderGuiOverlayEvent event, ClientLevel level, ItemStack itemStack, HitResult hitResult);
    //void setWheelInput(double scrollDelta);
}
