package com.moromoro.heliopause.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.moromoro.Heliopause;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RoastingTableScreen extends AbstractContainerScreen<RoastingTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Heliopause.MODID,"textures/gui/container/alchemy_roasting_table.png");
    public RoastingTableScreen(RoastingTableMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @Override
    protected void init() {
        super.init();
        //干渉するインベントリ側タイトルを画面外へ
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0,TEXTURE);
        //画面の中心に画像を置くための座標を取得
        int x = (width - imageWidth) /2;
        int y = (height - imageHeight) /2;
        //テクスチャアトラスの切り出しと位置設定
        graphics.blit(TEXTURE, x, y, 0,0,imageWidth,imageHeight);

        renderBurnIndicator(graphics,x,y);
    }

    private void renderBurnIndicator(GuiGraphics graphics, int x, int y){
        if(menu.isBurning()){//燃えていなければ描画しない
            //燃える時間を取得
            float litDurationPercentage = 1-(float) menu.getLitTime()/menu.getLitDuration();
            //スプライトの切り出し座標用に変換
            int perSpriteHeight = (int) Math.floor(8*litDurationPercentage);
            //x176 = メニューの右端 y0 =上端 x33=スプライトの幅 y8=スプライトの高さ
            //テクスチャアトラスの切り出しと位置設定
            graphics.blit(TEXTURE, x+48,y+63+perSpriteHeight,176, perSpriteHeight,33,8);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
