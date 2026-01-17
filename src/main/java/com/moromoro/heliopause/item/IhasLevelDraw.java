package com.moromoro.heliopause.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IhasLevelDraw {
    void renderLevelGraphic(RenderLevelStageEvent event, ClientLevel level, ItemStack itemStack);
}
