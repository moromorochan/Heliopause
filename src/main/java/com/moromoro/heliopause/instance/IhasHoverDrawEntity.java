package com.moromoro.heliopause.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public interface IhasHoverDrawEntity {
    boolean renderHoverGraphicWithEntity(RenderGuiOverlayEvent event, ClientLevel level);
    //void setWheelInput(double scrollDelta);
}
