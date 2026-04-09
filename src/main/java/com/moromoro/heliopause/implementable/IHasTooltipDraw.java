package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;

public interface IHasTooltipDraw {
    boolean renderTooltip(RenderTooltipEvent.GatherComponents event, ClientLevel level, ItemStack itemStack);
}