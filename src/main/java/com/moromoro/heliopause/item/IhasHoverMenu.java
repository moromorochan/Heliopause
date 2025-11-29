package com.moromoro.heliopause.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IhasHoverMenu {
    void renderHoverMenu(RenderGuiOverlayEvent event, ClientLevel level, ItemStack itemStack, HitResult hitResult);
    //void setWheelInput(double scrollDelta);
}
