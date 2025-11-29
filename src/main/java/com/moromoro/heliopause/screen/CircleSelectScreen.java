package com.moromoro.heliopause.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class CircleSelectScreen extends Screen {
    private final ItemStack itemStack;
    private final HitResult hitResult;
    private final int guiCenterX;
    private final int guiCenterY;

    public CircleSelectScreen(ItemStack handStack, HitResult hitResult, ItemStack itemStack, HitResult hitResult1, int guiCenterX, int guiCenterY) {
        super(Component.empty());
        this.itemStack = itemStack;
        this.hitResult = hitResult1;
        this.guiCenterX = guiCenterX;
        this.guiCenterY = guiCenterY;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
    }

    @Override
    public boolean keyReleased(int p_94715_, int p_94716_, int p_94717_) {
        // スクリーン消す
        return super.keyReleased(p_94715_, p_94716_, p_94717_);
    }
}
