package com.moromoro.heliopause.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.generic.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ConcentratorScreen extends AbstractContainerScreen<ConcentratorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Heliopause.MODID,"textures/gui/container/concentrator.png");
    private static final int SCROLL_WIDTH = 152;
    private static final int SCROLL_HEIGHT = 68;
    private static final int SCROLL_HORIZON = 66;
    private double compassAngle;
    private boolean mouseClicking;
    private boolean compassInitialized = false;

    List<Vector2i> validCoordinates = new ArrayList<>();
    private Vector2i selectedStarCoordinate = null;    // 選択中赤経

    private int guiX;
    private int guiY;

    public ConcentratorScreen(ConcentratorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 230;
        this.imageHeight = 219;
    }

    @Override
    protected void init() {
        super.init();
        compassAngle = 0;
        //this.titleLabelX = 8;
        //this.titleLabelY = -4;
        this.titleLabelY = 10000;
        this.inventoryLabelY = 10000;

        guiX = (width - imageWidth) /2;
        guiY = (height - imageHeight) /2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if(!compassInitialized){
            // 追従中の星があるならスクロールの初期位置にする
            Vector2i trackingCoordinate = menu.getTrackingStarCoordinate();
            if(trackingCoordinate != null){
                float partialTime = menu.getTime() + 1;
                float partialDate = menu.getDate() + (partialTime % 24000)/24000;
                Vector2d horizontalCoordinate = equatorialToHorizontal(trackingCoordinate, partialDate, partialTime);
                compassAngle = (360 + 270 + horizontalCoordinate.x())%(360);
            }
            compassInitialized = true;
        }
        // テクスチャの用意
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0,TEXTURE);

        // サイズ
        graphics.blit(TEXTURE, guiX, guiY, 0,0,imageWidth,imageHeight, 256, 320);

        // セットアップ

        renderDates(graphics,guiX+35,guiY+127);

        graphics.blit(TEXTURE, guiX + 8, guiY + 54, 0, 237, SCROLL_WIDTH + 2, SCROLL_HEIGHT+2, 256, 320);

        renderButton(graphics, partialTick, mouseX, mouseY, guiX + 165, guiY + 31);

        double screenCompassAngle = (compassAngle + 45)%360;

        float partialTime = (menu.getTime() % 24000 + partialTick) % 24000;
        float partialDate = menu.getDate() + partialTime/24000;

        Vector2i trackingCoordinate = menu.getTrackingStarCoordinate();
        // 選択星の軌道
        renderTrail(selectedStarCoordinate, graphics, partialTime, screenCompassAngle, guiX + 9, guiY + 55, 185, 246, 3);
        renderTrail(trackingCoordinate, graphics, partialTime, screenCompassAngle, guiX + 9, guiY + 55, 194, 246, 3);

        // 方角
        renderCompass(graphics, partialTick, screenCompassAngle, guiX + 9, guiY + 58);

        // 星
        List<Vector2i> starCoordinates = menu.getStarCoordinates();

        validCoordinates.clear();
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, screenCompassAngle, guiX +9, guiY +55, starCoordinates.subList(0, 20), 154, 237, 154, 252,7));
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, screenCompassAngle, guiX +9, guiY +55, starCoordinates.subList(21, 90), 154, 244, 154, 259,5));
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, screenCompassAngle, guiX +9, guiY +55, starCoordinates.subList(91, 150), 154, 249, 154, 264,3));

        // 月・太陽
        renderSun(graphics, partialTick, screenCompassAngle,guiX +9, guiY +55, 161,237,15, 0L);
        renderSun(graphics, partialTick, screenCompassAngle,guiX +9, guiY +55, 176,237,9, 12000L);

        // 装飾
        renderHorizons(graphics, partialTick, screenCompassAngle, guiX + 9, guiY + 112);

        //選択星インジケータ・選択星情報
        if(trackingCoordinate != null){
            renderStars(graphics, partialDate, partialTime, screenCompassAngle, guiX + 9, guiY + 55, List.of(trackingCoordinate), 194, 237, 9);
        }
        if(selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)) {
            renderStars(graphics, partialDate, partialTime, screenCompassAngle, guiX + 9, guiY + 55, List.of(selectedStarCoordinate), 185, 237, 9);
            graphics.drawString(font, "選択中:", guiX+158, guiY+6, 0x404040, false);
            graphics.drawString(font, "α:"+String.format("%3s°", selectedStarCoordinate.x)+" δ:"+String.format("%3s°", selectedStarCoordinate.y),guiX+158, guiY+18, 0x404040, false);
            graphics.drawString(font,"無名の星", guiX+15, guiY+10, 0xFFFFFF, false);

            graphics.drawString(font, "観測可能な時期",guiX+15, guiY+24,0x808080, false);
            // 日時オフセットから、夜の角度範囲に入る部分を取得
            graphics.drawString(font, "10月 7 日 ～ 2月 3 日",guiX+15, guiY+36,0x808080, false);
        }
        // 選択が無く、追従している星があるなら
        else if (trackingCoordinate != null){
            graphics.drawString(font, "追従中:", guiX+158, guiY+6, 0x404040, false);
            graphics.drawString(font, "α:"+String.format("%3s°",trackingCoordinate.x)+" δ:"+String.format("%3s°",trackingCoordinate.y),guiX+158, guiY+18, 0x404040, false);
            graphics.drawString(font,"無名の星", guiX+15, guiY+10, 0xFFFFFF, false);

            graphics.drawString(font, "観測可能な時期",guiX+15, guiY+24,0x808080, false);
            graphics.drawString(font, "10月 7 日 ～ 2月 3 日",guiX+15, guiY+36,0x808080, false);
        }
        // 選択も追従もないなら使い方を表示する
        else{
            graphics.drawString(font, "",guiX+15, guiY+24,0x808080, false);
        }

        //renderSelectPos();

        renderRecipeProgress(graphics, partialTick, guiX, guiY);
    }

    private void renderTrail(Vector2i starCoordinate, @NotNull GuiGraphics graphics, float partialTime, double screenCompassAngle, int x, int y, int texX, int texY, int size) {
        if(starCoordinate != null) {
            List<Vector2i> selectAsList = List.of(starCoordinate);
            // 軌道(15分毎)
            for (int localPos = 0; localPos < 24; localPos++) {
                float localBelowTime = ((int)(partialTime / 500) - localPos)*500;
                float localBelowDate = menu.getDate() + localBelowTime/24000;
                renderStars(graphics, localBelowDate, localBelowTime, screenCompassAngle, x, y, selectAsList, texX, texY, size);

                float localOverTime = ((int)(partialTime / 500) + localPos)*500;
                float localOverDate = menu.getDate() + localOverTime/24000;
                renderStars(graphics, localOverDate, localOverTime, screenCompassAngle, x, y, selectAsList, texX, texY, size);
            }
        }
    }

    private void renderDates(@NotNull GuiGraphics graphics, int x, int y) {
        // 日
        long time = menu.getTime() % 24000L;
        long dayInYear = menu.getDate();
        // 午前午後と日付ズレ
        if (time >= 18000L) {
            dayInYear = Math.floorMod(dayInYear + 1, Season.YEAR_LENGTH);
        }
        String m = (time < 6000L || time >= 18000L)  ? "午前":"午後";
        int month = Season.getMonth(dayInYear);
        int dayInMonth = (int) (dayInYear - month*Season.MONTH_DATES) + 1;
        int hour = (int) ((time + 6000L) % 12000L / 1000);
        int minute = (int)((time % 1000L) * (60f/1000));
        graphics.drawString(font, String.format("%2s",month + 1) + "月 "+ String.format("%2s",dayInMonth) +" 日 "+ m +" "+ String.format("%2s",hour) + ":" + String.format("%02d",minute),x, y, 0x404040,false);//"12月 4 日 午後 11:02"
    }

    private List<Vector2i> renderStars(GuiGraphics graphics, float partialDate, float partialTime, double screenCompassAngle, int x, int y, List<Vector2i> starCoordinates, int texX, int texY, int size) {
        return renderStars(graphics, partialDate, partialTime, screenCompassAngle, x, y, starCoordinates, texX, texY, texX, texY, size);
    }

    private List<Vector2i> renderStars(GuiGraphics graphics, float partialDate, float partialTime, double screenCompassAngle, int x, int y, List<Vector2i> starCoordinates, int texX, int texY, int subTexX, int subTexY, int size) {
        List<Vector2i> validCoordinates = new ArrayList<>();
        for (Vector2i starCoordinate : starCoordinates) {
            // 地平座標に変換
            Vector2d horizontalCoordinate = equatorialToHorizontal(starCoordinate, partialDate, partialTime);
            // スクリーン上の座標に変換
            int scrollOffset = -(int)((screenCompassAngle * SCROLL_WIDTH) / 90);
            double screenX = (SCROLL_WIDTH * 4 + scrollOffset + (horizontalCoordinate.x() * (SCROLL_WIDTH/90d) + size/2d))%(SCROLL_WIDTH * 4) - size;
            double screenY = SCROLL_HORIZON - horizontalCoordinate.y() * (SCROLL_HORIZON/90d) - size/2d;
            // 見えないものは描画スキップ
            if(screenY < -size || screenY >= SCROLL_HEIGHT + size || screenX < -size || screenX >= SCROLL_WIDTH + size){
                continue;
            }
            // トリミング量
            double shrinkLeft = Math.max(0.001f, -screenX);
            double shrinkUp = Math.max(0.001f, -screenY);
            double shrinkRight = Math.max(0.001f, screenX + size - SCROLL_WIDTH);
            double shrinkBottom = Math.max(0.001f, screenY + size - SCROLL_HEIGHT);
            if(shrinkRight >= size || shrinkBottom >= size){
                continue;
            }
            double imageWidth = size - shrinkRight - shrinkLeft;
            double imageHeight = size - shrinkBottom - shrinkUp;

            //float localTime = partialTime % 24000;
            float heightThresh;//Math.min(-10, localTime < 12000 ? -localTime : 12000 -localTime);
            // 日の入
            if(partialTime < 18000){
                heightThresh = Mth.clamp(12090 - partialTime,10,90);
            }
            // 日の出
            else {
                heightThresh = Mth.clamp(partialTime - (24000 - 90),10,90);
            }
            if(horizontalCoordinate.y() > heightThresh && partialTime >= 12000){
                blitFloat(TEXTURE, graphics, (float) (x + screenX + shrinkLeft), (float) (y + screenY + shrinkUp), (float) (texX + shrinkLeft), (float) (texY + shrinkUp), (float) imageWidth, (float) imageHeight, 256, 320);
            }
            // 地平線に近いか、昼のときはsubTexを使う
            else{
                blitFloat(TEXTURE, graphics, (float) (x + screenX + shrinkLeft), (float) (y + screenY + shrinkUp), (float) (subTexX + shrinkLeft), (float) (subTexY + shrinkUp), (float) imageWidth, (float) imageHeight, 256, 320);
            }
            validCoordinates.add(starCoordinate);
        }
        return validCoordinates;
    }

    private void renderSun(GuiGraphics graphics, float partialTick, double screenCompassAngle, int x, int y, int texX, int texY, int size, long timeOffset) {
        // 時刻から座標作成
        double time = (24000L + menu.getTime() - timeOffset + 400 + partialTick) % 24000L -400;
        int direction = (int) (screenCompassAngle / 90);

        int scrollOffset = -(int)((screenCompassAngle * SCROLL_WIDTH) / 90);
        double screenX;
        double screenY;
        // 東の空 & 東が見えているなら
        if(time < 6400L && direction == 1){
            screenX = (SCROLL_WIDTH * 2 + scrollOffset)%(SCROLL_WIDTH * 4) - size/2d;
            screenY = SCROLL_HORIZON - (time/6000d)*SCROLL_HORIZON - size/2d;
        }
        // 西の空 & 西が見えているなら
        else if(time > 5600L && direction == 3){
            screenX = (SCROLL_WIDTH * 4 + scrollOffset)%(SCROLL_WIDTH * 4) - size/2d;
            screenY = SCROLL_HORIZON - ((12000L-time)/6000d)*SCROLL_HORIZON - size/2d;
        }
        else{
            return;
        }
        // 見えないものは描画スキップ
        if(screenY < -size || screenY >= SCROLL_HORIZON + size || screenX < -size || screenX >= SCROLL_WIDTH + size){
            return;
        }
        // トリミング量
        double shrinkLeft = Math.max(0.001f, -screenX);
        double shrinkUp = Math.max(0.001f, -screenY);
        double shrinkRight = Math.max(0.001f, screenX + size - SCROLL_WIDTH);
        double shrinkBottom = Math.max(0.001f, screenY + size - SCROLL_HORIZON);
        if(shrinkRight >= size || shrinkBottom >= size){
            return;
        }
        double imageWidth = size - shrinkRight - shrinkLeft;
        double imageHeight = size - shrinkBottom - shrinkUp;

        blitFloat(TEXTURE, graphics, (float) (x + screenX + shrinkLeft), (float) (y + screenY + shrinkUp), (float) (texX + shrinkLeft), (float) (texY + shrinkUp), (float) imageWidth, (float) imageHeight, 256, 320);
    }

    // ドットからズレた位置に描画
    void blitFloat(ResourceLocation texture, GuiGraphics graphics, float posX, float posY, float posU, float posV, float width, float height, int texWidth, int texHeight) {
        float posEndX = posX + width;
        float posEndY = posY + height;

        float u0 = posU / texWidth;
        float v0 = posV / texHeight;
        float u1 = (posU + width) / texWidth;
        float v1 = (posV + height) / texHeight;


        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, posX, posY, 0).uv(u0, v0).endVertex();
        bufferbuilder.vertex(matrix4f, posX, posEndY, 0).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix4f, posEndX, posEndY, 0).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, posEndX, posY, 0).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    //赤道座標系から地平座標系へ
    public static Vector2d equatorialToHorizontal(Vector2i equatorialCoordinate, float partialDate, float partialTime){
        double eqX = equatorialCoordinate.x();
        double eqY = equatorialCoordinate.y();

        // 経度方向季節オフセット
        eqX += partialDate * (360d / Season.YEAR_LENGTH);
        // 緯度方向季節オフセット
        eqY += - 10 * Math.cos(Math.toRadians(partialDate * (360d / Season.YEAR_LENGTH))); //1月1日が冬至(暫定)
        // 経度方向時刻オフセット
        eqX += (partialTime) * (360d / 24000L);

        // ラジアン変換
        double H = Math.toRadians(eqX % 360d);
        double delta = Math.toRadians(eqY);

        // 高度(altitude)
        double sinAlt = Mth.clamp(Math.cos(delta) * Math.cos(H), -1, 1);
        double altDeg = -Math.toDegrees(Math.asin(sinAlt));

        // 方位角(azimuth)
        double y = Math.sin(H);
        double x = -Math.tan(delta);
        double azDeg = (Math.toDegrees(Math.atan2(y, x)) + 90) % 360;

        return new Vector2d(azDeg, altDeg);
    }

    private void getTrackPos(double screenCompassAngle, int x, int y, double mouseX, double mouseY) {
        int size = 9;
        double lastDistance = 10000;
        float partialTime = menu.getTime() % 24000;
        float partialDate = menu.getDate() + partialTime/24000;
        //boolean selected = false;
        selectedStarCoordinate = null;
        Vector2i selectedCoordinate = null;
        for (Vector2i starCoordinate : validCoordinates) {
            // 地平座標に変換
            Vector2d horizontalCoordinate = equatorialToHorizontal(starCoordinate, partialDate, partialTime);
            // スクリーン上の座標に変換
            int scrollOffset = -(int)((screenCompassAngle * SCROLL_WIDTH) / 90);
            double screenX = (SCROLL_WIDTH * 4 + scrollOffset + (horizontalCoordinate.x() * (SCROLL_WIDTH/90d) + size/2d))%(SCROLL_WIDTH * 4) - size;
            double screenY = SCROLL_HORIZON - horizontalCoordinate.y() * (SCROLL_HORIZON/90d) - size/2d;

            double distanceX = Math.abs(x + screenX + size/2d - mouseX);
            double distanceY = Math.abs(y + screenY + size/2d - mouseY);
            if( distanceX < size/2d && distanceY < size/2d){
                double distance = Vector2d.length(distanceX, distanceY);
                if(distance < lastDistance){
                    lastDistance = distance;
                    selectedCoordinate = new Vector2i(starCoordinate);
                }
            }
        }
        if(selectedCoordinate != null) {
            //blitFloat(TEXTURE, graphics, (float) selectX, (float) selectY, 185, 237, 9, 9, 256, 320);
            selectedStarCoordinate = selectedCoordinate;
        }
    }

    private void renderButton(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int x, int y) {
        Component buttonString;
        boolean isValid = true;
        Vector2i trackingCoordinate = menu.getTrackingStarCoordinate();
        if(selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)){
            buttonString = Component.literal("追従");
        } else if (trackingCoordinate != null) {
            buttonString = Component.literal("追従を解除");
        }else{
            isValid = false;
            graphics.blit(TEXTURE, x, y, 100,219, 50,18, 256, 320);
            buttonString = Component.literal("追従");
        }
        // 有効状態か確認
        if(isValid){
            // ボタン上か確認
            if(mouseX > x && mouseX < x+50 && mouseY > y && mouseY < y+18){
                //押されているか確認
                if(mouseClicking){
                    graphics.blit(TEXTURE, x, y, 50,219, 50,18, 256, 320);
                }else{
                    graphics.blit(TEXTURE, x, y, 150,219, 50,18, 256, 320);
                }
            }else{
                graphics.blit(TEXTURE, x, y, 0,219, 50,18, 256, 320);
            }
        }

        graphics.drawString(font, buttonString, x+25 - font.width(buttonString)/2, y+5, 0xFFFFFF, false);
    }

    private void renderRecipeProgress(GuiGraphics graphics, float partialTick, int x, int y) {
        graphics.blit(TEXTURE, x + 165, y + 53, 200, 219, 18, 13, 256, 320);
        graphics.blit(TEXTURE, x + 185, y + 53, 200, 219, 18, 5, 256, 320);
        graphics.blit(TEXTURE, x + 205, y + 53, 218, 219, 18, 13, 256, 320);
    }

    private void renderHorizons(GuiGraphics graphics, float partialTick, double screenCompassAngle, int x, int y) {
        // 角度をオフセットに変換
        int scrollOffset = (int)((screenCompassAngle * SCROLL_WIDTH) / 90) % SCROLL_WIDTH;
        int afterOffset = SCROLL_WIDTH - scrollOffset;

        // スクロール手前部分
        graphics.blit(TEXTURE, x, y, 1 + scrollOffset, 307, afterOffset, 11, 256, 320);
        // スクロール後ろ部分
        graphics.blit(TEXTURE, x + afterOffset, y, 1, 307, scrollOffset, 11, 256, 320);
    }

    private void renderCompass(@NotNull GuiGraphics graphics, float partialTick, double screenCompassAngle, int x, int y){
        // 角度をオフセットに変換
        int scrollOffset = (int)((screenCompassAngle * SCROLL_WIDTH) / 90) % SCROLL_WIDTH;
        int direction = (int) (screenCompassAngle / 90);
        int afterOffset = SCROLL_WIDTH - scrollOffset;

        // スクロール手前部分
        graphics.blit(TEXTURE, x, y, 1 + scrollOffset, 318, afterOffset, 1, 256, 320);
        // スクロール後ろ部分
        graphics.blit(TEXTURE, x + afterOffset, y, 1, 318, scrollOffset, 1, 256, 320);

        //graphics.drawString(font, String.valueOf(compassAngle), x, y+8, 0x544C3B, false);
        if(scrollOffset > 4 && scrollOffset < SCROLL_WIDTH - 4){
            MutableComponent dirString = switch (direction){
                case 0 -> Component.translatable("gui.heliopause.concentrator.compass.north");
                case 1 -> Component.translatable("gui.heliopause.concentrator.compass.east");
                case 2 -> Component.translatable("gui.heliopause.concentrator.compass.south");
                case 3 -> Component.translatable("gui.heliopause.concentrator.compass.west");
                default -> Component.literal("");
            };
            graphics.drawString(font, dirString, x + afterOffset - font.width(dirString)/2, y - 3, 0x544C3B, false);
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        // スクロール範囲内か確認
        if(mouseX > guiX+9 && mouseX < guiX+161 && mouseY > guiY+55 && mouseY < guiY+123 ){
            compassAngle = Math.floorMod((int) (compassAngle - scrollDelta * 5), 360);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // スクロール範囲内か確認
        if(mouseX > guiX+9 && mouseX < guiX+161 && mouseY > guiY+55 && mouseY < guiY+123 ){
            compassAngle = Math.floorMod((int) (compassAngle - dragX), 360);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0 && !mouseClicking){
            mouseClicking = true;
            ClientLevel level = minecraft != null ? minecraft.level : null;
            // ボタン確認
            if(mouseX > guiX + 165 && mouseX < guiX + 215 && mouseY > guiY + 31 && mouseY < guiY + 49){
                // 効果音
                if (level != null) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            // スクロール上の星確認
            if(mouseX > guiX + 9 && mouseX < guiX + 161 && mouseY > guiY + 55 && mouseY < guiY + 123) {
                getTrackPos((compassAngle + 45)%360, guiX + 9, guiY + 55, mouseX, mouseY);
                //効果音
                if(level != null && selectedStarCoordinate != null){
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PUT, 2.0f));
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) {
            mouseClicking = false;
            // ボタン確認
            if(mouseX > guiX + 165 && mouseX < guiX + 215 && mouseY > guiY + 31 && mouseY < guiY + 49){
                // ボタン適用
                Vector2i trackingCoordinate = menu.getTrackingStarCoordinate();
                int encodedCoordinate;
                // 選択を追従に適用
                if(selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)){
                    encodedCoordinate = ConcentratorMenu.encodeStarCoordinate(selectedStarCoordinate);
                }
                // 追従を解除
                else if (menu.getTrackingStarCoordinate() != null) {
                    selectedStarCoordinate = menu.getTrackingStarCoordinate();
                    encodedCoordinate = ConcentratorMenu.encodeStarCoordinate(null);
                }else{
                    encodedCoordinate = -1;
                }
                if(encodedCoordinate != -1){
                    sendCoordinate(encodedCoordinate);
                        //menu.clicked(,encodedCoordinate, , Minecraft.getInstance().player);
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendCoordinate(int encodedCoordinate){
        Minecraft instance = Minecraft.getInstance();
        if (instance.player != null && instance.gameMode != null && instance.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            instance.gameMode.handleInventoryMouseClick(menu.containerId,
                ConcentratorMenu.COORDINATE_BUTTON_INDEX, encodedCoordinate, ClickType.PICKUP, instance.player);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
