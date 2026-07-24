package com.moromoro.heliopause.compat.jei;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.generic.DescriptionTooltip;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipe;
import com.moromoro.heliopause.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;

import static com.moromoro.heliopause.screen.SiderostatScreen.*;

public class MoonlightPouringCategory implements IRecipeCategory<MoonlightPouringRecipe> {
    public static final RecipeType<MoonlightPouringRecipe> MOONLIGHT_POURING_TYPE =
        new RecipeType<>(MoonlightPouringRecipe.Serializer.ID, MoonlightPouringRecipe.class);
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Heliopause.MODID, "textures/gui/container/siderostat.png");
    private static final int imageWidth = 176;

    private final IDrawable background;
    private final IDrawable indicator;
    private final IDrawable icon;
    private final IDrawableAnimated cachedArrow;
    private final IDrawable cachedMoon;
    private final IDrawable cachedPath;
    private final ITickTimer moonTimer;
    // 初期化
    public MoonlightPouringCategory(IGuiHelper helper){
        // アイコン設定
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.SIDEROSTAT_TOP.get()));
        // 背景
        this.background = helper.createDrawable(TEXTURE, 13,10, 163-13,95-10);
        // 視界インジケータまとめて
        this.indicator = helper.createDrawable(TEXTURE, 13,185,163-13,256-185);
        // アニメーションさせる矢印
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE,imageWidth, 0,ARROW_WIDTH,ARROW_HEIGHT);
        this.cachedArrow = helper.createAnimatedDrawable(staticArrow, 100, IDrawableAnimated.StartDirection.TOP, false);
        // 月
        this.moonTimer = helper.createTickTimer(200, 200, false);
        this.cachedMoon = helper.createDrawable(TEXTURE,imageWidth, ARROW_HEIGHT+1,STAR_WIDTH,STAR_HEIGHT);
        // 月の軌跡
        this.cachedPath = helper.createDrawable(TEXTURE,imageWidth + STAR_WIDTH + 1,ARROW_HEIGHT + 1, 2,2);
    }

    @Override
    public void draw(MoonlightPouringRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        // インジケータ描画
        indicator.draw(guiGraphics,0, 2);
        cachedArrow.draw(guiGraphics,81-13,48-10);
        // 月のアニメーション
        drawMoon(guiGraphics);
        // レシピの処理時間
        drawRecipeTime(recipe, guiGraphics);
        
        // 月明かりの説明を表示
        double indicatorAreaDistance = new Vector2d(mouseX,mouseY).distance(75, 75);
        if(mouseY < 75 && indicatorAreaDistance > 60 && indicatorAreaDistance < 75){
            DescriptionTooltip.drawDescriptionTooltip(guiGraphics, this.getWidth(),
                Component.translatable("recipe.heliopause.moonlight_pouring.moonlight_description"), 200,
                mouseX, (int) mouseY);
        }
    }

    private void drawMoon(GuiGraphics graphics) {
        int x = -13, y = -10;
        int springAmount = Math.min(180, moonTimer.getValue() - 10);
        Vector2d pos = new Vector2d(x + CENTER_X - RADIUS, y + CENTER_Y);
        for (int i = 1; i < springAmount; i++) {
            //firstPos = secondPos;
            pos = new Vector2d(x+ CENTER_X - (int)(Math.cos(Math.toRadians(i)) * RADIUS), y + CENTER_Y - (int)(Math.sin(Math.toRadians(i)) * RADIUS));
            //graphics.hLine(RenderType.LINES,firstPos.x,firstPos.y, secondPos.x,secondPos.y);
            //drawLinePixels(graphics, (int) firstPos.x, (int) firstPos.y, (int) secondPos.x, (int) secondPos.y, 0xFFFFFF);
            cachedPath.draw(graphics, (int)pos.x - 1, (int)pos.y - 1);
            //graphics.blit(TEXTURE, (int)pos.x - 1, (int)pos.y - 1, imageWidth + STAR_WIDTH + 1, ARROW_HEIGHT + 1, 2, 2);
        }
        if(springAmount < 2){
            pos = new Vector2d(x+ CENTER_X - (int)(Math.cos(Math.toRadians(1)) * RADIUS), y + CENTER_Y - (int)(Math.sin(Math.toRadians(1)) * RADIUS));
        }
        //graphics.blit(TEXTURE, (int)(pos.x - STAR_WIDTH/2.0), (int)(pos.y - STAR_HEIGHT/2.0), imageWidth,ARROW_HEIGHT+1, STAR_WIDTH, STAR_HEIGHT);
        cachedMoon.draw(graphics,(int)(pos.x - STAR_WIDTH/2.0), (int)(pos.y - STAR_HEIGHT/2.0));
    }

    private void drawRecipeTime(MoonlightPouringRecipe recipe, GuiGraphics guiGraphics) {
        int recipeTimeSec = recipe.getCraftTime() /20;
        guiGraphics.drawString(
            Minecraft.getInstance().font, Component.translatable("gui.jei.category.smelting.time.seconds", recipeTimeSec),
            81 + 10,
            62 + 10,
            0x808080,false
        );
    }

    @Override
    public RecipeType<MoonlightPouringRecipe> getRecipeType() {
        return MOONLIGHT_POURING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.heliopause.moonlight_pouring");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoonlightPouringRecipe recipe, IFocusGroup focuses) {
        // 材料スロット
        builder.addSlot(RecipeIngredientRole.INPUT, 80-13, 21-1).addIngredients(recipe.getIngredients().get(0));
        // 結果スロット
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80-13,62-1).addItemStack(recipe.getResultItem(null));
    }
}
