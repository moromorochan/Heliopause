package com.moromoro.heliopause.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IhasHoverTexts {
    @NotNull
    List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, HitResult hitResult);
}
