package com.moromoro.heliopause.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.recipe.StarlightConcentrationRecipe;
import com.moromoro.heliopause.recipe.StellarInstantiationRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.getRecipeStellar;
import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.initStellarInstances;

public class ConcentratorScreen extends AbstractContainerScreen<ConcentratorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Heliopause.MODID,"textures/gui/container/concentrator.png");
    private static final int TEXTURE_HEIGHT = 512;
    private static final int TEXTURE_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 180;

    public static final int ACTUAL_NIGHT_RANGE = 70; // 実際に観測できる夜の時刻角度の幅(地平付近除く) 夜側の座標を引き伸ばす
    public static final int TWILIGHT_RANGE = 20; // 地平線付近の観測できない角度

    public static final double RANGE_OFFSET = ACTUAL_NIGHT_RANGE + TWILIGHT_RANGE * 2;

    public static final int TAB_COUNT = 3;
    private int tabId = 0;
    private boolean mouseClicking;
    private boolean buttonSelecting = false;
    private int scrollOffset = 0;

    private enum recipeCondition{
        INVALID_NO_RECIPE,
        VALID,
        INVALID_SIGHT,
        INVALID_ACCURACY,
        TANK_FULL
    }

    private final recipeCondition[] isRecipeValid = new recipeCondition[]{recipeCondition.INVALID_NO_RECIPE, recipeCondition.INVALID_NO_RECIPE, recipeCondition.INVALID_NO_RECIPE};
    private final int PROGRESS_DIVIDE = 180;
    private final double[] recipeProgress = new double[]{PROGRESS_DIVIDE, PROGRESS_DIVIDE, PROGRESS_DIVIDE};

    public List<StellarInstantiationRecipe.StellarInstance> stellarInstances = new ArrayList<>();
    long levelSeed;

    List<Vector2i> validCoordinates = new ArrayList<>();
    private Vector2i selectedStarCoordinate = null;    // 選択中赤経

    private final NonNullList<Slot> wholeSlot = NonNullList.create();
    static class InActiveSlot extends Slot {
        public InActiveSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }
        @Override
        public boolean isActive() {
            return false;
        }
    }

    public ConcentratorScreen(ConcentratorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 230;
        this.imageHeight = WINDOW_HEIGHT + 28;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 10000;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if(wholeSlot.isEmpty()){
            wholeSlot.addAll(this.menu.slots);
            for (int slot = 0; slot <= 3; slot++) {
                Slot old = this.menu.slots.get(slot);
                this.menu.slots.set(slot, new InActiveSlot(old.container, old.getContainerSlot(), old.x, old.y));
            }
            for (int slot = 4; slot <= 30; slot++) {
                Slot old = this.menu.slots.get(slot);
                this.menu.slots.set(slot, new InActiveSlot(old.container, old.getContainerSlot(), old.x, old.y));
            }
            for (int slot = 31; slot <= 39; slot++) {
                Slot old = this.menu.slots.get(slot);
                this.menu.slots.set(slot, wholeSlot.get(slot));
            }
        }
        Level level = Minecraft.getInstance().level;
        if (level != null && stellarInstances.isEmpty()) {
            levelSeed = menu.getLevelSeed();
            //stellarInstances = StellarInstantiationRecipe.getStars(level, levelSeed);
            stellarInstances = initStellarInstances(level, levelSeed);
        }

        // テクスチャの用意
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0,TEXTURE);

        Vector2i trackingCoordinate = menu.getTrackingStarCoordinate();

        // ウィンドウ
        switch (tabId){
            case 0 ->{
                renderObservatoryTab(graphics, partialTick, mouseX, mouseY, trackingCoordinate);
            }
            case 1 -> {
                renderTankTab(graphics, partialTick, mouseX, mouseY, trackingCoordinate);
            }
            case 2 -> {
                renderGlobeTab(graphics, partialTick, mouseX, mouseY, trackingCoordinate);
            }
        }
        // タブアイテム
        graphics.renderFakeItem(Items.BUCKET.getDefaultInstance(), leftPos+32, topPos+9);

        if(mouseX > leftPos + 1 && mouseX < leftPos + 24 && mouseY > topPos + 1 && mouseY < topPos + 24){
            graphics.renderTooltip(font,Component.literal("観測状況"), mouseX, mouseY);
        }
        if(mouseX > leftPos + 28 && mouseX < leftPos + 51 && mouseY > topPos +1 && mouseY < topPos + 24){
            graphics.renderTooltip(font,Component.literal("タンク"), mouseX, mouseY);
        }
        if(mouseX > leftPos + 55 && mouseX < leftPos + 79 && mouseY > topPos + 1 && mouseY < topPos + 24){
            graphics.renderTooltip(font,Component.literal("天球儀"), mouseX, mouseY);
        }
    }

    private void renderFixedCompass(GuiGraphics graphics, int x, int y) {
        Component north = Component.translatable("gui.heliopause.concentrator.compass.north");
        Component east = Component.translatable("gui.heliopause.concentrator.compass.east");
        Component south = Component.translatable("gui.heliopause.concentrator.compass.south");
        Component west = Component.translatable("gui.heliopause.concentrator.compass.west");

        graphics.drawString(font, north, x+10 - font.width(north)/2, y+10 - font.lineHeight/2, 0xFFFFFF, false);
        graphics.drawString(font, east, x+10 - font.width(east)/2, y+(114-10) - font.lineHeight/2, 0xFFFFFF, false);
        graphics.drawString(font, south, x+(114-10) - font.width(south)/2, y+(114-10) - font.lineHeight/2, 0xFFFFFF, false);
        graphics.drawString(font, west, x+(114-10) - font.width(west)/2, y+10 - font.lineHeight/2, 0xFFFFFF, false);
    }

    private void renderScrollCompass(GuiGraphics graphics, int x, int y){
        Minecraft instance = Minecraft.getInstance();
        for (int month = 0; month < 12; month+=2) {
            String monthString = Component.translatable("season.heliopause.month."+ (month+1)).getString();
            int size = 0;
            int scroll = (scrollOffset + month * 30)%360;
            if(scroll > 180){
                continue;
            }
            double radX = Math.toRadians(scroll);
            double screenX = (1+Math.cos(radX))*45 - size/2d;
            double screenY = y + (Math.sin(radX))*6;
            graphics.drawString(instance.font, monthString, (int) (x + screenX - instance.font.width(monthString)/2d), (int)screenY, 0xFFFFFF, false);
        }
    }

    private void renderSelectedStellarInfo(@NotNull GuiGraphics graphics, Vector2i trackingCoordinate, int x, int y, int mouseX, int mouseY) {
        if(selectedStarCoordinate != null || trackingCoordinate != null){

            Vector2i targetCoordinate;
            if (selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)) {
                targetCoordinate = selectedStarCoordinate;
            } else {
                targetCoordinate = trackingCoordinate;
            }

            // ターゲット星取得
            Optional<StellarInstantiationRecipe.StellarInstance> targetOptional =
                stellarInstances.stream().filter(stellarInstance -> stellarInstance.coordinate().equals(targetCoordinate)).findFirst();

            if(targetOptional.isPresent()){
                StellarInstantiationRecipe.StellarInstance target = targetOptional.get();
                if(targetCoordinate.equals(selectedStarCoordinate)){
                    graphics.drawString(font, "選択中:", x, y, 0x808080, false);
                }else{
                    graphics.drawString(font, "追従中:", x, y, 0x808080, false);
                }
                graphics.drawString(font, Component.translatable("gui.heliopause.concentrator."+target.stellarType()), x, y + 12, 0xFFFFFF, false);
                graphics.drawString(font, "α:"+String.format("%3s°", targetCoordinate.x)+" δ:"+String.format("%3s°", targetCoordinate.y),x, y+ 23, 0xFFFFFF, false);

                // 角度から日付
                graphics.drawString(font, "観測可能な時期", x, y + 37,0x808080, false);

                double range = ACTUAL_NIGHT_RANGE + 1;//180 - TWILIGHT_RANGE;

                double starVisibleStart = Season.YEAR_LENGTH * ((targetCoordinate.x() - range)%360/360d);
                String visibleStartDate = Season.getDateTranslatable((long)starVisibleStart, 23000L);
                double starVisibleEnd = Season.YEAR_LENGTH * ((targetCoordinate.x() + range)%360/360d);
                String visibleEndDate = Season.getDateTranslatable((long)starVisibleEnd, 13000L);
                graphics.drawString(font, visibleStartDate+" ~ ", x, y + 48,0xFFFFFF, false);
                graphics.drawString(font, visibleEndDate, x + 81 - font.width(visibleEndDate), y + 59,0xFFFFFF, false);

                renderRecipeInfo(graphics, trackingCoordinate, x, y +72, mouseX, mouseY);
            }

        }
        // 選択も追従もないなら使い方を表示する
        else{
            graphics.drawString(font, "使い方", x, y,0x808080, false);
        }
    }

    private void renderRecipeInfo(@NotNull GuiGraphics graphics, Vector2i trackingCoordinate, int x, int y, int mouseX, int mouseY) {
        if (selectedStarCoordinate != null || trackingCoordinate != null) {
            Vector2i target;
            if (selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)) {
                target = selectedStarCoordinate;
            } else {
                target = trackingCoordinate;
            }

            Level level = Minecraft.getInstance().level;
            graphics.drawString(font, "エーテル特性:", x, y, 0x808080, false);
            double barrelAccuracy = menu.getBarrelAccuracy();
            boolean isSightValid = menu.isSyncedToStar();

            List<StarlightConcentrationRecipe.FluidStackChance> targetRecipeFluidStack = getRecipeStellar(level, stellarInstances, target);
            for (int featureId = 0; featureId < targetRecipeFluidStack.size(); featureId++) {
                StarlightConcentrationRecipe.FluidStackChance fluidStackChance = targetRecipeFluidStack.get(featureId);
                FluidStack fluidStack = fluidStackChance.fluidStack();
                if (fluidStack.isEmpty()) {
                    recipeProgress[featureId] = 0;
                    continue;
                }

                // 描画位置
                int drawY = y + 11 + 11 * featureId;

                // テクスチャ取得
                ResourceLocation atlasLocation = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);

                // 切り出して描画
                int pixelAmount = 10 - (int)Math.floor(((double)fluidStack.getAmount()/fluidStackChance.maxAmount())*10);
                /*if(pixelAmount == 0){
                    graphics.blit(TEXTURE,x-2, drawY-2, 48, 476, 12, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                }
                else{

                }*/
                graphics.blit(TEXTURE,x-2, drawY-2, 36, 476, 12, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                blitSprite(atlasLocation, graphics, x-1, drawY -1 + pixelAmount, 3, pixelAmount + 3, 13, 13);
                String fluidName = fluidStack.getDisplayName().getString();
                if(font.width(fluidName) >= 70){
                    while(font.width(fluidName) > 65){
                        fluidName = fluidName.substring(0, fluidName.length() - 1);
                    }
                    fluidName = fluidName + "...";
                }
                graphics.drawString(font, fluidName, x + 12, drawY, pixelAmount == 0 ? 0x80FFFF: 0xFFFFFF, false);

                // ツールチップ表示・状況判定
                final boolean hover = (mouseX > x- 1 && mouseX < x + 70 && mouseY > drawY - 2 && mouseY < drawY + 10);
                final double recipeAccuracy = fluidStackChance.accuracy();
                final boolean isValidAccuracy = barrelAccuracy >= recipeAccuracy;
                if(hover) {
                    List<Component> fluidTooltip = new ArrayList<>();
                    fluidTooltip.add(fluidStack.getDisplayName());
                    fluidTooltip.add(Component.literal(fluidStack.getAmount() + "mb/秒"));
                    String accuracyTooltip = Component.literal("必要な精度: ").getString();
                    if(isValidAccuracy){
                        fluidTooltip.add(Component.literal(accuracyTooltip + String.format("%.2f", recipeAccuracy)));
                    }else{
                        fluidTooltip.add(Component.literal(accuracyTooltip +String.format("%.2f",recipeAccuracy)+" < "+String.format("%.2f",barrelAccuracy)).withStyle(ChatFormatting.RED));
                    }

                    graphics.renderComponentTooltip(font, fluidTooltip, mouseX, mouseY);
                }
                if(isValidAccuracy){
                    if(isSightValid){
                        isRecipeValid[featureId] = recipeCondition.VALID;
                        if(recipeProgress[featureId] <= PROGRESS_DIVIDE){
                            recipeProgress[featureId] += (double) fluidStack.getAmount() / Minecraft.getInstance().getFps();
                        }
                    }else{
                        recipeProgress[featureId] = 0;
                        isRecipeValid[featureId] = recipeCondition.INVALID_SIGHT;
                    }
                }else{
                    recipeProgress[featureId] = 0;
                    isRecipeValid[featureId] = recipeCondition.INVALID_ACCURACY;
                }
            }
        }

    }

    private void blitSprite(@Nullable ResourceLocation texture, GuiGraphics graphics, int x, int y, int localU0, int localV0, int localU1, int localV1) {
        if(texture == null){
            // missingNoテクスチャを使用
            texture = MissingTextureAtlasSprite.getLocation();
        }
        // スプライト取得
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        if (sprite == null) return;
        //スプライトのサイズ取得
        int spriteW = sprite.contents().width();
        int spriteH = sprite.contents().height();

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float uRange = sprite.getU1() - u0;
        float vRange = sprite.getV1() - v0;

        float subU0 = u0 + (localU0 / (float) spriteW) * uRange;
        float subV0 = v0 + (localV0 / (float) spriteH) * vRange;
        float subU1 = u0 + (localU1 / (float) spriteW) * uRange;
        float subV1 = v0 + (localV1 / (float) spriteH) * vRange;

        int posEndX = x + Math.max(0, localU1 - localU0);
        int posEndY = y + Math.max(0, localV1 - localV0);

        // 描画
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = graphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, (float) x, (float) y, 0).uv(subU0, subV0).endVertex();
        bufferbuilder.vertex(matrix4f, (float) x, (float) posEndY, 0).uv(subU0, subV1).endVertex();
        bufferbuilder.vertex(matrix4f, (float) posEndX, (float) posEndY, 0).uv(subU1, subV1).endVertex();
        bufferbuilder.vertex(matrix4f, (float) posEndX, (float) y, 0).uv(subU1, subV0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    private void renderObservatoryTab(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY, Vector2i trackingCoordinate) {
        // ウィンドウ
        graphics.blit(TEXTURE, leftPos, topPos + 28, 0, 0, imageWidth, WINDOW_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos, topPos, 0,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos+27, topPos, 107,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos+54, topPos, 134,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // セットアップ
        renderDates(graphics,leftPos+21,topPos+158);

        renderButton(graphics, mouseX, mouseY, leftPos + 129, topPos + 159);

        renderSelectedStellarInfo(graphics, trackingCoordinate, leftPos+134, topPos + 38, mouseX, mouseY);

        float partialTime = (menu.getTime() % 24000 + partialTick) % 24000;
        float partialDate = menu.getDate();

        // 選択星の軌道
        renderTrail(selectedStarCoordinate, graphics, partialTime, leftPos + 13, topPos + 38, 31, 455, 3);
        renderTrail(trackingCoordinate, graphics, partialTime, leftPos + 13, topPos + 38, 40, 455, 3);

        // 星
        List<Vector2i> mag1Stars = stellarInstances.stream().filter(stellarInstance -> stellarInstance.magnitude() == 1).map(StellarInstantiationRecipe.StellarInstance::coordinate).toList();
        List<Vector2i> mag2Stars = stellarInstances.stream().filter(stellarInstance -> stellarInstance.magnitude() == 2).map(StellarInstantiationRecipe.StellarInstance::coordinate).toList();
        List<Vector2i> mag3Stars = stellarInstances.stream().filter(stellarInstance -> stellarInstance.magnitude() == 3).map(StellarInstantiationRecipe.StellarInstance::coordinate).toList();

        validCoordinates.clear();
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, true, leftPos +13, topPos +38, mag3Stars, 0, 458, 0, 473,3));
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, true, leftPos +13, topPos +38, mag2Stars, 0, 453, 0, 468,5));
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, true, leftPos +13, topPos +38, mag1Stars, 0, 446, 0, 461,7));

        // 月・太陽
        renderSun(graphics, partialTick, leftPos +13, topPos +38, 7,446,15, 0L);
        renderSun(graphics, partialTick, leftPos +13, topPos +38, 22,446,9, 12000L);

        //選択星インジケータ・選択星情報
        if(trackingCoordinate != null){
            renderStars(graphics, partialDate, partialTime, true, leftPos + 13, topPos + 38, List.of(trackingCoordinate), 40, 446, 9);
        }
        if(selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)){
            renderStars(graphics, partialDate, partialTime, true, leftPos + 13, topPos + 38, List.of(selectedStarCoordinate), 31, 446, 9);
        }

        // 範囲外の星を隠す
        graphics.blit(TEXTURE, leftPos+12, topPos+37, 81, 392, 114, 114, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        renderFixedCompass(graphics, leftPos+12, topPos+37);
    }

    private void renderTankTab(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY, Vector2i trackingCoordinate) {
        // ウィンドウ
        graphics.blit(TEXTURE, leftPos, topPos + 28, 0, WINDOW_HEIGHT, imageWidth, WINDOW_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos, topPos, 80,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos+27, topPos, 27,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos+54, topPos, 134,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // セットアップ
        renderButton(graphics, mouseX, mouseY, leftPos + 129, topPos + 101);

        Arrays.fill(isRecipeValid, recipeCondition.INVALID_NO_RECIPE);

        renderRecipeInfo(graphics, trackingCoordinate, leftPos+134, topPos + 38, mouseX, mouseY);

        renderRecipeProgress(graphics, partialTick, mouseX, mouseY, leftPos, topPos);

        renderTank(graphics, mouseX, mouseY, leftPos + 20, topPos + 97);
    }

    private void renderGlobeTab(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY, Vector2i trackingCoordinate) {
        // ウィンドウ
        graphics.blit(TEXTURE, leftPos, topPos + 28, 0, 0, imageWidth, WINDOW_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos, topPos, 80,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos+27, topPos, 107,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(TEXTURE, leftPos+54, topPos, 54,360, 26, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // セットアップ
        renderDates(graphics,leftPos+21,topPos+158);

        renderButton(graphics, mouseX, mouseY, leftPos + 129, topPos + 159);

        renderSelectedStellarInfo(graphics, trackingCoordinate, leftPos+134, topPos + 38, mouseX, mouseY);


        float partialTime = (menu.getTime() % 24000 + partialTick) % 24000;
        float partialDate = menu.getDate();

        // 選択星の軌道
        //renderTrail(selectedStarCoordinate, graphics, partialTime, leftPos + 13, topPos + 38, 31, 455, 3);
        //renderTrail(trackingCoordinate, graphics, partialTime, leftPos + 13, topPos + 38, 40, 455, 3);

        // 月・太陽
        renderSun(graphics, partialTick, leftPos +13, topPos +38, 7,446,15, 0);
        blitFloat(TEXTURE, graphics, leftPos + 13 + 56 - 4.5f, topPos + 38 + 56 - 4.5f, 0.5f, 22, 446, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //renderSun(graphics, partialTick, leftPos +13, topPos +38, 22,446,9, 12000L);

        // 星
        List<Vector2i> mag1Stars = stellarInstances.stream().filter(stellarInstance -> stellarInstance.magnitude() == 1).map(StellarInstantiationRecipe.StellarInstance::coordinate).toList();
        List<Vector2i> mag2Stars = stellarInstances.stream().filter(stellarInstance -> stellarInstance.magnitude() == 2).map(StellarInstantiationRecipe.StellarInstance::coordinate).toList();
        List<Vector2i> mag3Stars = stellarInstances.stream().filter(stellarInstance -> stellarInstance.magnitude() == 3).map(StellarInstantiationRecipe.StellarInstance::coordinate).toList();

        validCoordinates.clear();
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, false, leftPos +13, topPos +38, mag3Stars, 0, 458, 0, 473,3));
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, false, leftPos +13, topPos +38, mag2Stars, 0, 453, 0, 468,5));
        validCoordinates.addAll(renderStars(graphics, partialDate, partialTime, false, leftPos +13, topPos +38, mag1Stars, 0, 446, 0, 461,7));

        //選択星インジケータ・選択星情報
        if(trackingCoordinate != null){
            renderStars(graphics, partialDate, partialTime, false, leftPos + 13, topPos + 38, List.of(trackingCoordinate), 40, 446, 9);
        }
        if(selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)){
            renderStars(graphics, partialDate, partialTime, false, leftPos + 13, topPos + 38, List.of(selectedStarCoordinate), 31, 446, 9);
        }

        // 範囲外の星を隠す
        graphics.blit(TEXTURE, leftPos+12, topPos+37, 81, 392, 114, 114, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        renderScrollCompass(graphics, leftPos + 22, topPos + 138);
    }

    private void renderTrail(Vector2i starCoordinate, @NotNull GuiGraphics graphics, float partialTime, int x, int y, int texX, int texY, int size) {
        if(starCoordinate != null) {
            List<Vector2i> selectAsList = List.of(new Vector2i(180,starCoordinate.y()));
            // 軌道(15分毎)
            for (int localPos = 0; localPos < 12; localPos++) {
                float localBelowTime = -localPos * 500;//((int)(partialTime / 500) - localPos)*500;
                //float localBelowDate = /*menu.getDate() +*/ localBelowTime/24000;
                renderStars(graphics, 0L, localBelowTime, false, x, y, selectAsList, texX, texY, size);

                float localOverTime = localPos * 500;//((int)(partialTime / 500) + localPos)*500;
                //float localOverDate = /*menu.getDate() +*/ localOverTime/24000;
                renderStars(graphics, 0L, localOverTime, false, x, y, selectAsList, texX, texY, size);
            }
        }
    }

    private void renderDates(@NotNull GuiGraphics graphics, int x, int y) {
        graphics.drawString(font,
            Season.getDateTranslatable(menu.getDate(), menu.getTime())
                + " " + Season.getTimeTranslatable(menu.getTime()),
            x, y, 0x404040,false);//"12月 4 日 午後 11:02"
    }

    private List<Vector2i> renderStars(GuiGraphics graphics, float partialDate, float partialTime, boolean realign, int x, int y, List<Vector2i> starCoordinates, int texX, int texY, int size) {
        return renderStars(graphics, partialDate, partialTime, realign, x, y, starCoordinates, texX, texY, texX, texY, size);
    }

    private List<Vector2i> renderStars(GuiGraphics graphics, float partialDate, float partialTime, boolean realign, int x, int y, List<Vector2i> starCoordinates, int texX, int texY, int subTexX, int subTexY, int size) {
        List<Vector2i> validCoordinates = new ArrayList<>();
        for (Vector2i starCoordinate : starCoordinates) {
            // 地平座標に変換
            Vector2d horizontalCoordinate;
            double radX;
            double radY;
            double screenX;
            double screenY;
            boolean useSubTex;
            if(tabId == 0){
                horizontalCoordinate = equatorialToHorizontal(starCoordinate, partialDate, partialTime, realign);
                // スクリーン上の座標に変換
                radX = Math.toRadians(horizontalCoordinate.x() - 45);
                if(horizontalCoordinate.y() < 0){
                    continue;
                }
                radY = Math.toRadians(horizontalCoordinate.y());
                screenX = (1+Math.cos(radX)*Math.cos(radY))*56 - size/2d;
                screenY = (1+Math.sin(radX)*Math.cos(radY))*56 - size/2d;

                useSubTex = !isStellarValidOnTime(partialTime, horizontalCoordinate);

            }else{
                horizontalCoordinate = new Vector2d((starCoordinate.x() + scrollOffset)%360, starCoordinate.y());
                // スクリーン上の座標に変換
                radX = Math.toRadians(horizontalCoordinate.x());
                radY = Math.toRadians(horizontalCoordinate.y());
                screenX = (1+Math.cos(radX) * Math.cos(radY))*56 - size/2d;
                screenY = (1+Math.sin(radY))*56 + (Math.sin(radX))*6 - size/2d;
                useSubTex = horizontalCoordinate.x() > 180;
            }

            // 見えないものは描画スキップ
            if(screenY < -size || screenY >= 112 + size || screenX < -size || screenX >= 112 + size){
                continue;
            }
            // トリミング量
            double shrinkLeft = Math.max(0.001f, -screenX);
            double shrinkUp = Math.max(0.001f, -screenY);
            double shrinkRight = Math.max(0.001f, screenX + size - 112);
            double shrinkBottom = Math.max(0.001f, screenY + size - 112);
            if(shrinkRight >= size || shrinkBottom >= size){
                continue;
            }
            double imageWidth = size - shrinkRight - shrinkLeft;
            double imageHeight = size - shrinkBottom - shrinkUp;

            if(!useSubTex){
                blitFloat(TEXTURE, graphics, (float) (x + screenX + shrinkLeft), (float) (y + screenY + shrinkUp), 1, (float) (texX + shrinkLeft), (float) (texY + shrinkUp), (float) imageWidth, (float) imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
            // 地平線に近いか、昼のときはsubTexを使う
            else{
                blitFloat(TEXTURE, graphics, (float) (x + screenX + shrinkLeft), (float) (y + screenY + shrinkUp), 0, (float) (subTexX + shrinkLeft), (float) (subTexY + shrinkUp), (float) imageWidth, (float) imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
            validCoordinates.add(starCoordinate);
        }
        return validCoordinates;
    }

    public static boolean isStellarValidOnTime(float partialTime, Vector2d horizontalCoordinate) {
        float heightThresh;
        // 日の入
        if(partialTime < 18000){
            heightThresh = Mth.clamp(90+(12000 - partialTime)/35, TWILIGHT_RANGE,90);
        }
        // 日の出
        else {
            heightThresh = Mth.clamp(90+(partialTime - 24000)/35, TWILIGHT_RANGE,90);
        }
        return (horizontalCoordinate.y() >= heightThresh) && (partialTime > 12000);
    }

    private void renderSun(GuiGraphics graphics, float partialTick, int x, int y, int texX, int texY, int size, long timeOffset) {
        if(tabId == 0){
            // 時刻から座標作成
            float time = (24000L + menu.getTime() - timeOffset + 400 + partialTick) % 24000L -400;
            renderStars(graphics, 0, time, false, x,y,List.of(new Vector2i(90,0)),texX, texY, size);
            return;
        }
        if(tabId == 2){
            // 季節から座標作成
            int season = (int)(180 + menu.getDate() * 360f/Season.YEAR_LENGTH) % 360;
            renderStars(graphics, 0, 0, false, x,y,List.of(new Vector2i(season,0)),texX, texY, size);
        }

        /*int direction = (int) (screenCompassAngle / 90);

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

        blitFloat(TEXTURE, graphics, (float) (x + screenX + shrinkLeft), (float) (y + screenY + shrinkUp), (float) (texX + shrinkLeft), (float) (texY + shrinkUp), (float) imageWidth, (float) imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);*/
    }

    void blitFloat(ResourceLocation texture, GuiGraphics graphics, float posX, float posY, float posU, float posV, float width, float height, int texWidth, int texHeight) {
        blitFloat(texture, graphics, posX, posY, 0, posU, posV, width, height, texWidth, texHeight);
    }
    // ドットからズレた位置に描画
    void blitFloat(ResourceLocation texture, GuiGraphics graphics, float posX, float posY, float z, float posU, float posV, float width, float height, int texWidth, int texHeight) {
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
        bufferbuilder.vertex(matrix4f, posX, posY, z).uv(u0, v0).endVertex();
        bufferbuilder.vertex(matrix4f, posX, posEndY, z).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix4f, posEndX, posEndY, z).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix4f, posEndX, posY, z).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    //赤道座標系から地平座標系へ
    public static Vector2d equatorialToHorizontal(Vector2i equatorialCoordinate, float partialDate, float partialTime, boolean realign){
        // 値を取り出して定数オフセット
        double eqX = equatorialCoordinate.x();
        double eqY = equatorialCoordinate.y();
        // 経度方向季節オフセット
        eqX += partialDate * (360d / Season.YEAR_LENGTH);

        // 経度方向時刻オフセット
        double dayCycleOffset = (partialTime) * (360d / 24000L);

        if(realign){
            // 季節の太陽位置との相対距離を縮小
            double sunPos = 90;
            //eqX = realignAngle(eqX, dayCycleOffset, RANGE_OFFSET, 1, 0);
            eqX = (eqX-sunPos)%360;
            if(eqX > 180){
                eqX -= 360;
            }else if(eqX <= -180){
                eqX += 360;
            }

            double allocationScale = (1 + Math.cos(-2 * Math.toRadians(eqX)) / 2) * (1 - (Math.abs(eqX)/180)) * 0.90;
            double allocationScaleRef = (1 + Math.cos(Math.toRadians(-ACTUAL_NIGHT_RANGE)) / 2) * (1 - (ACTUAL_NIGHT_RANGE / 360d));
            double allocationStrength = (90d-(ACTUAL_NIGHT_RANGE/2d))/allocationScaleRef;

            if(eqX >= 0) {
                eqX -= allocationScale * allocationStrength;
            }else {
                eqX += allocationScale * allocationStrength;
            }

            /*if(Math.abs(eqX) > (360d - RANGE_OFFSET) / 2){
                if(eqX > 0){
                    eqX = 180 - eqX;
                    eqX *= 180d/RANGE_OFFSET;
                    eqX = 180 - eqX;
                }else {
                    eqX = -180 - eqX;
                    eqX *= 180d/RANGE_OFFSET;
                    eqX = - eqX - 180;
                }
            }else{
                eqX *= 170d/(360-RANGE_OFFSET);
            }*/

            eqX = (360 + eqX)%360;
        }
        eqX += dayCycleOffset;


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

    private void getTrackPos(int x, int y, double mouseX, double mouseY) {
        int size = 9;
        double lastDistance = 10000;
        float partialTime = menu.getTime() % 24000;
        float partialDate = menu.getDate() + partialTime/24000;
        //boolean selected = false;
        selectedStarCoordinate = null;
        Vector2i selectedCoordinate = null;
        for (Vector2i starCoordinate : validCoordinates) {
            // 地平座標に変換
            Vector2d horizontalCoordinate;
            double radX;
            double radY;
            double screenX;
            double screenY;
            if(tabId == 0){
                horizontalCoordinate = equatorialToHorizontal(starCoordinate, partialDate, partialTime, true);
                // スクリーン上の座標に変換
                radX = Math.toRadians(horizontalCoordinate.x() - 45);
                if(horizontalCoordinate.y() < 0){
                    continue;
                }
                radY = Math.toRadians(horizontalCoordinate.y());
                screenX = (1+Math.cos(radX)*Math.cos(radY))*56; //- size/2d;
                screenY = (1+Math.sin(radX)*Math.cos(radY))*56; //+ size/2d;
            }else{
                horizontalCoordinate = new Vector2d((starCoordinate.x() + scrollOffset)%360, starCoordinate.y());
                if(horizontalCoordinate.x() > 180){
                    continue;
                }
                // スクリーン上の座標に変換
                radX = Math.toRadians(horizontalCoordinate.x());
                radY = Math.toRadians(horizontalCoordinate.y());
                screenX = (1+Math.cos(radX) * Math.cos(radY))*56;
                screenY = (1+Math.sin(radY))*56 + (Math.sin(radX))*6;
            }

            double distanceX = Math.abs(x + screenX - mouseX);
            double distanceY = Math.abs(y + screenY - mouseY);
            if( distanceX < size/2d && distanceY < size/2d){
                double distance = Vector2d.length(distanceX, distanceY);
                if(distance < lastDistance){
                    lastDistance = distance;
                    selectedCoordinate = new Vector2i(starCoordinate);
                }
            }
        }
        if(selectedCoordinate != null) {
            //blitFloat(TEXTURE, graphics, (float) selectX, (float) selectY, 185, 237, 9, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            selectedStarCoordinate = selectedCoordinate;
        }
    }

    private void renderButton(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        Component buttonString;
        boolean isValid = true;
        Vector2i trackingCoordinate = menu.getTrackingStarCoordinate();
        if(selectedStarCoordinate != null && !selectedStarCoordinate.equals(trackingCoordinate)){
            buttonString = Component.literal("追従");
        } else if (trackingCoordinate != null) {
            buttonString = Component.literal("追従を解除");
        }else{
            isValid = false;
            //graphics.blit(TEXTURE, x, y, 100,219, 50,18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            buttonString = Component.literal("追従");
        }
        // 有効状態か確認
        if(isValid){
            // ボタン上か確認
            if(mouseX > x && mouseX < x+81 && mouseY > y && mouseY < y+18){
                buttonSelecting = true;
                //押されているか確認
                if(mouseClicking){
                    graphics.blit(TEXTURE, x, y, 0,410, 81,18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                }else{
                    graphics.blit(TEXTURE, x, y, 0,428, 81,18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                }
            }else{
                buttonSelecting = false;
                graphics.blit(TEXTURE, x, y, 0,392, 81,18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
        }

        graphics.drawString(font, buttonString, x+40 - font.width(buttonString)/2, y+5, 0xFFFFFF, false);
    }

    private void renderRecipeProgress(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int x, int y) {
        for (int i = 0; i < 3; i++) {
            int xPos = x + 19 + 28 * i;
            int yPos = y + 34;
            switch (isRecipeValid[i]) {
                case INVALID_SIGHT, TANK_FULL -> {
                    break;
                }
                case VALID -> {
                    int height = Mth.clamp((int) ((13.0 * recipeProgress[i]) / PROGRESS_DIVIDE), 0, 13);
                    graphics.blit(TEXTURE, xPos, yPos, 0, 476, 18, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                    break;
                }
                case INVALID_NO_RECIPE, INVALID_ACCURACY -> {
                    graphics.blit(TEXTURE, xPos, yPos, 18, 476, 18, 13, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                    break;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            int xPos = x + 19 + 28 * i;
            int yPos = y + 34;
            boolean hover = (mouseX > xPos && mouseX < xPos + 18 && mouseY > yPos && mouseY < yPos + 13);
            if(hover){
                switch (isRecipeValid[i]) {
                    case INVALID_NO_RECIPE, TANK_FULL -> {
                        break;
                    }
                    case VALID -> {
                        graphics.renderTooltip(font, Component.literal("観測中"), mouseX, mouseY);
                        break;
                    }
                    case INVALID_SIGHT -> {
                        graphics.renderTooltip(font, Component.literal("観測環境を待機中"), mouseX, mouseY);
                        break;
                    }
                    case INVALID_ACCURACY -> {
                        graphics.renderTooltip(font, Component.literal("鏡筒の精度が足りない!"), mouseX, mouseY);
                        break;
                    }
                }
            }
        }
    }

    private void renderTank(GuiGraphics graphics, int mouseX, int mouseY, int x, int y){
        menu.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidCap -> {
            for (int tank = 0; tank < fluidCap.getTanks(); tank++) {
                FluidStack fluidStack = fluidCap.getFluidInTank(tank);
                if(fluidStack.getAmount() != fluidCap.getTankCapacity(tank)){
                    if(isRecipeValid[tank].equals(recipeCondition.VALID) && recipeProgress[tank] >= PROGRESS_DIVIDE){
                        recipeProgress[tank] %= PROGRESS_DIVIDE;
                    }
                }
                if(fluidStack.isEmpty()){
                    continue;
                }
                // 高さ計算
                int screenTop = (int)Math.max(1, Math.floor((double)45 * fluidStack.getAmount())/fluidCap.getTankCapacity(tank));
                // テクスチャ取得
                ResourceLocation atlasLocation = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);

                int localX = x + 28 * tank;

                for (int i = 16; i <= screenTop; i+=16) {
                    blitSprite(atlasLocation, graphics, localX - 4, y - i, 12,0,16,16);
                    blitSprite(atlasLocation, graphics, localX, y - i, 0,0,16,16);
                    blitSprite(atlasLocation, graphics, localX + 16, y - i, 0,0,4,16);
                }
                int partialRemain = 16 - screenTop % 16;
                blitSprite(atlasLocation, graphics, localX - 4, y - screenTop, 12, partialRemain,16,16);
                blitSprite(atlasLocation, graphics, localX, y - screenTop, 0, partialRemain,16, 16);
                blitSprite(atlasLocation, graphics, localX + 16, y - screenTop, 0, partialRemain,4,16);

                // ツールチップ表示
                if(mouseX >= localX - 4 && mouseX < localX + 20 && mouseY >= y - 45 && mouseY < y){
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(fluidStack.getDisplayName());
                    tooltip.add(Component.literal(fluidStack.getAmount() + " / " + fluidCap.getTankCapacity(tank) + " mb"));
                    graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                }

            }
        });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        // スクロール範囲内か確認
        if(mouseX > leftPos + 12 && mouseX < leftPos + 126 && mouseY > topPos + 37 && mouseY < topPos + 151) {
            scrollOffset = Math.floorMod((int) (scrollOffset - scrollDelta * 5), 360);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // スクロール範囲内か確認
        if(mouseX > leftPos + 12 && mouseX < leftPos + 126 && mouseY > topPos + 37 && mouseY < topPos + 151) {
            scrollOffset = Math.floorMod((int) (scrollOffset - dragX), 360);
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
            if(buttonSelecting){    /*mouseX > leftPos + 165 && mouseX < leftPos + 215 && mouseY > topPos + 31 && mouseY < topPos + 49*/
                // 効果音
                if (level != null) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
            // スクロール上の星確認
            if(tabId == 0 || tabId == 2){
                if(mouseX > leftPos + 12 && mouseX < leftPos + 126 && mouseY > topPos + 37 && mouseY < topPos + 151) {
                    getTrackPos(leftPos + 13, topPos + 38, mouseX, mouseY);
                    //効果音
                    if(level != null && selectedStarCoordinate != null){
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PUT, 2.0f));
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) {
            mouseClicking = false;

            int x,y;
            switch (this.tabId){
                case 0,2 -> {x=125; y=159;}
                case 1 -> {x=125;y=101;}
                default -> {x = 0; y = 0;}
            }
            // ボタン確認
            if(buttonSelecting/*mouseX > leftPos+x && mouseX < leftPos+x+81 && mouseY > topPos+y && mouseY < topPos+y+18*/){    /*mouseX > leftPos + 165 && mouseX < leftPos + 215 && mouseY > topPos + 31 && mouseY < topPos + 49*/
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
            // タブ確認
            if(mouseX > leftPos + 1 && mouseX < leftPos + 24 && mouseY > topPos + 1 && mouseY < topPos + 24){
                sendTab(0);
                for (int slot = 0; slot <= 3; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, new InActiveSlot(old.container, old.getContainerSlot(), old.x, old.y));
                }
                for (int slot = 4; slot <= 30; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, new InActiveSlot(old.container, old.getContainerSlot(), old.x, old.y));
                }
                for (int slot = 31; slot <= 39; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, wholeSlot.get(slot));
                }
            }
            else if(mouseX > leftPos + 28 && mouseX < leftPos + 51 && mouseY > topPos + 1 && mouseY < topPos + 24){
                sendTab(1);
                for (int slot = 0; slot <= 3; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, wholeSlot.get(slot));
                }
                for (int slot = 4; slot <= 30; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, wholeSlot.get(slot));
                }
                for (int slot = 31; slot <= 39; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, wholeSlot.get(slot));
                }
            }
            else if(mouseX > leftPos + 55 && mouseX < leftPos + 79 && mouseY > topPos + 1 && mouseY < topPos + 24){
                sendTab(2);
                for (int slot = 0; slot <= 3; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, new InActiveSlot(old.container, old.getContainerSlot(), old.x, old.y));
                }
                for (int slot = 4; slot <= 30; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, new InActiveSlot(old.container, old.getContainerSlot(), old.x, old.y));
                }
                for (int slot = 31; slot <= 39; slot++) {
                    Slot old = this.menu.slots.get(slot);
                    this.menu.slots.set(slot, wholeSlot.get(slot));
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendCoordinate(int encodedCoordinate){
        Minecraft instance = Minecraft.getInstance();
        if (instance.player != null && instance.gameMode != null && instance.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            instance.gameMode.handleInventoryButtonClick(menu.containerId, -encodedCoordinate);
        }
    }

    private void sendTab(int selectTabId){
        Minecraft instance = Minecraft.getInstance();
        if (instance.player != null && instance.gameMode != null && instance.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            instance.gameMode.handleInventoryButtonClick(menu.containerId, selectTabId);
        }
        this.tabId = selectTabId;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
