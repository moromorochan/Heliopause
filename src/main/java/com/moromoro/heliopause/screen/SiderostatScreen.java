package com.moromoro.heliopause.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector2d;

public class SiderostatScreen extends AbstractContainerScreen<SiderostatMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Heliopause.MODID, "textures/gui/container/siderostat.png");
    public static final int GUI_HEIGHT = 184;
    public static final int ARROW_WIDTH = 14;
    public static final int ARROW_HEIGHT =16;
    public static final int CENTER_X = 88;
    public static final int CENTER_Y = 85;
    public static final int RADIUS = 63;
    public static final int STAR_WIDTH = 6;
    public static final int STAR_HEIGHT = 7;

    // 視界インジケータ
    public static final int[] SIGHT_POS_X = new int[]{
        /*16,22,39,62,94,116,138,152*/
        16,17,22,29,38,49,62,75, 89,102,115,127,137,146,152,155
    };
    public static final int[] SIGHT_POS_Y = new int[]{
        /*231,208,191,185,185,191,208,231*/
        244,231,218,207,198,191,186,185, 185,186,191,198,207,218,231,244
    };
    public static final int[] SIGHT_WIDTH_X = new int[]{
        /*8,16,21,20,20,21,16,8*/
        5,7,8,10,11,12,12,12, 12,12,12,11,10,8,7,5
    };
    public static final int[] SIGHT_WIDTH_Y = new int[]{
        /*24,21,16,8,8,16,21,24*/
        12,12,12,11,10,8,7,5, 5,7,8,10,11,12,12,12
    };

    public SiderostatScreen(SiderostatMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @Override
    protected void init(){
        //this.imageHeight = GUI_HEIGHT;
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = -4;
        this.inventoryLabelY = GUI_HEIGHT - 103;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // テクスチャの用意
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0,TEXTURE);

        // 表示位置の用意
        int x = (width - imageWidth) /2;//this.leftPos;
        int y = (height - GUI_HEIGHT) /2;//this.topPos;

        // サイズ
        graphics.blit(TEXTURE, x, y, 0,0,imageWidth,GUI_HEIGHT);

        // 視野インジケータの表示
        renderSightIndicator(graphics,x,y);

        // ゼンマイ位置インジケータの表示
        renderStarIndicator(graphics,x,y);

        // クラフト進捗インジケータの表示
        renderCraftIndicator(graphics,x,y);
    }

    private void renderCraftIndicator(GuiGraphics graphics, int x, int y) {
        int craftingProgress = menu.getCraftingProgress();//data.get(1);
        int craftingTotalTime = menu.getCraftingTotalTime();//data.get(2);
        if(craftingTotalTime == 0){return;}
        float craftingPercentage = (float) craftingProgress / craftingTotalTime;
        int perSpriteHeight = (int) Math.floor(ARROW_HEIGHT * craftingPercentage);

        graphics.blit(TEXTURE, x+81, y+48, imageWidth, 0, ARROW_WIDTH, perSpriteHeight);
    }

    private void renderStarIndicator(GuiGraphics graphics, int x, int y) {
        int springAmount = menu.getSpringAmount();
        //double springPercentage = 1.0 - (springAmount / 180.0);
        // 円弧を描画
        //Vector2d firstPos;// = new Vector2i(0,0);
        Vector2d pos = new Vector2d(x + CENTER_X - RADIUS, y + CENTER_Y);
        for (int i = 1; i < springAmount; i++) {
            //firstPos = secondPos;
            pos = new Vector2d(x+ CENTER_X - (int)(Math.cos(Math.toRadians(i)) * RADIUS), y + CENTER_Y - (int)(Math.sin(Math.toRadians(i)) * RADIUS));
            //graphics.hLine(RenderType.LINES,firstPos.x,firstPos.y, secondPos.x,secondPos.y);
            //drawLinePixels(graphics, (int) firstPos.x, (int) firstPos.y, (int) secondPos.x, (int) secondPos.y, 0xFFFFFF);
            graphics.blit(TEXTURE, (int)pos.x - 1, (int)pos.y - 1, imageWidth + STAR_WIDTH + 1, ARROW_HEIGHT + 1, 2, 2);
        }
        if(springAmount < 2){
            pos = new Vector2d(x+ CENTER_X - (int)(Math.cos(Math.toRadians(1)) * RADIUS), y + CENTER_Y - (int)(Math.sin(Math.toRadians(1)) * RADIUS));
        }
        graphics.blit(TEXTURE, (int)(pos.x - STAR_WIDTH/2.0), (int)(pos.y - STAR_HEIGHT/2.0), imageWidth,ARROW_HEIGHT+1, STAR_WIDTH, STAR_HEIGHT);
    }

    private void renderSightIndicator(GuiGraphics graphics, int x, int y) {
        /*int springAmount = menu.getSpringAmount();
        if(springAmount == 0 || springAmount == 180){
            return;
        }*/
        short sight = menu.getCanSeeSkies();
        for (int i = 0; i < SiderostatBlockEntity.SKY_SLICES; i++) {
            boolean visible = (sight & (1 << i)) != 0;
            if(!visible){
                continue;
            }
            graphics.blit(TEXTURE, x+ SIGHT_POS_X[i], y + SIGHT_POS_Y[i] - 173,SIGHT_POS_X[i], SIGHT_POS_Y[i], SIGHT_WIDTH_X[i],SIGHT_WIDTH_Y[i]);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

}
