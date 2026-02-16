package com.moromoro.heliopause.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IhasHoverDraw {
    boolean renderHoverGraphic(RenderGuiOverlayEvent event, ClientLevel level, ItemStack itemStack, HitResult hitResult);
    //void setWheelInput(double scrollDelta);
}
