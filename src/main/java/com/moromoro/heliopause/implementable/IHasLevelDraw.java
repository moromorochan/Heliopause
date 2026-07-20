package com.moromoro.heliopause.implementable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IHasLevelDraw {
    @OnlyIn(Dist.CLIENT)
    boolean renderLevelGraphic(RenderLevelStageEvent event, ClientLevel level, ItemStack itemStack);
}
