package com.moromoro.heliopause.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IhasLevelDraw {
    boolean renderLevelGraphic(RenderLevelStageEvent event, ClientLevel level, ItemStack itemStack);
}
