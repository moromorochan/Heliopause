package com.moromoro.heliopause.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IhasBlockHoverTexts {
    @NotNull
    List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, BlockPos pos);
}
