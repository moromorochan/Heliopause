package com.moromoro.heliopause.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.generic.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConcentratorScreen extends AbstractContainerScreen<ConcentratorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Heliopause.MODID,"textures/gui/container/concentrator.png");

    public ConcentratorScreen(ConcentratorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY -= 1;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

        // テクスチャの用意
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0,TEXTURE);

        // 背景
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(TEXTURE, leftPos + 25, topPos + 14, 176, 61, 22, 58);
        graphics.blit(TEXTURE, leftPos + 129, topPos + 14, 198, 61, 22, 58);
        
        // レシピ進行度
        renderRecipeProgress(graphics, menu.getRecipeProgress(), menu.getMaxProgress(), menu.isSyncedToStar(), partialTick, leftPos + 69, topPos + 20);
        // 視界状態
        renderSkyIndicator(graphics, menu.isSyncedToStar(), mouseX, mouseY, leftPos + 76, topPos + 13);
        // 日付
        renderDates(graphics, menu.getDate(), menu.getTime(), leftPos + 8, topPos + 73);
        // タンク
        menu.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(fluidCap -> {
            renderTank(graphics, fluidCap.getFluidInTank(ConcentratorBlockEntity.SLOT_INPUT_FLUID), fluidCap.getTankCapacity(ConcentratorBlockEntity.SLOT_INPUT_FLUID), mouseX, mouseY, leftPos + 50, topPos + 37, 16, 32);
            renderTank(graphics, fluidCap.getFluidInTank(ConcentratorBlockEntity.SLOT_OUTPUT_FLUID), fluidCap.getTankCapacity(ConcentratorBlockEntity.SLOT_OUTPUT_FLUID), mouseX, mouseY, leftPos + 110, topPos + 37, 16, 32);
        });
    }
    
    public void renderTank(GuiGraphics graphics, FluidStack fluidStack, int tankCapacity, int mouseX, int mouseY, int x, int y, int width, int height){
        if(fluidStack.isEmpty()){
            return;
        }
        // 高さ計算
        int screenTop = (int)Math.max(1, Math.floor((double)height * fluidStack.getAmount())/tankCapacity);
        // テクスチャ取得
        ResourceLocation atlasLocation = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);
        Color tint = new Color(IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack));
        graphics.setColor(tint.getRed() / 255f, tint.getGreen() / 255f, tint.getBlue() / 255f, tint.getAlpha() / 255f);
        for (int spriteY = 0; spriteY < screenTop; spriteY+=16) {
            int partialRemainY = Math.min(16, screenTop - spriteY);
            for (int spriteX = 0; spriteX < width; spriteX+=16) {
                int partialRemainX = Math.min(16, width - spriteX);
                RenderSystem.enableBlend();
                blitSprite(atlasLocation, graphics, x + spriteX, y + height - spriteY - partialRemainY, 0, 16 - partialRemainY, partialRemainX, 16);
                RenderSystem.disableBlend();
            }
        }
        graphics.setColor(1f, 1f, 1f, 1f);
        // ツールチップ表示
        if(mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height){
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(fluidStack.getDisplayName());
            tooltip.add(Component.literal(fluidStack.getAmount() + " / " + tankCapacity + " mb"));
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }
    
    private void renderRecipeProgress(GuiGraphics graphics, int recipeProgress, int maxProgress, boolean isSyncedToStar, float partialTick, int x, int y){
        // 高さ計算
        int height = (int)Math.max(1, Math.floor(45.0 * recipeProgress)/maxProgress);
        // partialTick計算
        if(isSyncedToStar){
            height += (int) (45.0 * partialTick /maxProgress);
        }
        graphics.blit(TEXTURE, x, y, 176, 0, 38, height);
    }
    
    private void renderSkyIndicator(GuiGraphics graphics, boolean isSyncedToStar, int mouseX, int mouseY, int x, int y){
        final int indicatorWidth = 24;
        final int indicatorHeight = 6;
        if(isSyncedToStar){
            graphics.blit(TEXTURE, x, y,184,45, indicatorWidth, indicatorHeight);
        }
        if(mouseX >= x && mouseX < x + indicatorWidth && mouseY >= y && mouseY < y + indicatorHeight){
            List<Component> tooltip = new ArrayList<>();
            if(isSyncedToStar){
                tooltip.add(Component.translatable("gui.heliopause.concentrator.processing"));
            }else{
                tooltip.add(Component.translatable("gui.heliopause.concentrator.waiting"));
            }
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private void renderDates(@NotNull GuiGraphics graphics, long date, long time, int x, int y) {
        graphics.blit(TEXTURE, x, y, 176, 45, 8,8);
        graphics.drawString(font,
            Season.getDateTranslatable(date, time)
                + " " + Season.getTimeTranslatable(time),
            x + 10, y, 0x404040,false);//"12月 4 日 午後 11:02"
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

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
