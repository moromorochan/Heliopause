package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IHasLevelDraw {
    boolean renderLevelGraphic(RenderLevelStageEvent event, ClientLevel level, ItemStack itemStack);
}
