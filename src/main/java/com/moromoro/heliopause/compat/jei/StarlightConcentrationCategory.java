package com.moromoro.heliopause.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.registry.enumProperty.LensBarrelCoverageIconValue;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.recipe.LensBarrelCoverageListener;
import com.moromoro.heliopause.recipe.StarlightConcentrationRecipe;
import com.moromoro.heliopause.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StarlightConcentrationCategory implements IRecipeCategory<StarlightConcentrationRecipe> {
    public static final RecipeType<StarlightConcentrationRecipe> STARLIGHT_CONCENTRATION_TYPE =
        new RecipeType<>(StarlightConcentrationRecipe.Serializer.ID, StarlightConcentrationRecipe.class);
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Heliopause.MODID,"textures/gui/container/concentrator.png");
    private static final ResourceLocation COVERAGE_ICON =
        new ResourceLocation(Heliopause.MODID, "textures/gui/coverage_icon.png");
    private static final int GUI_X = 24;
    
    private final IDrawable background;
    private final IDrawable indicator;
    private final IDrawable icon;
    private final IDrawableAnimated cachedProgress;
    
    // 初期化
    public StarlightConcentrationCategory(IGuiHelper helper){
        // アイコン設定
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.CONCENTRATOR.get()));
        // 背景
        this.background = helper.createDrawable(TEXTURE, 45 - GUI_X,12, 86 + 2 * GUI_X, 62);
        // 視界インジケータ
        this.indicator = helper.createDrawable(TEXTURE, 184,45, 24, 6);
        // アニメーションさせるレシピ進行度
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE,176, 0,38,45);
        this.cachedProgress = helper.createAnimatedDrawable(staticArrow, 100, IDrawableAnimated.StartDirection.TOP, false);
    }
    
    /*@Override
    public int getWidth() {
        return 150;
    }*/
    
    @Override
    public int getHeight() {
        return 138;
    }
    
    @Override
    public void draw(StarlightConcentrationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        final Font font = Minecraft.getInstance().font;
        // インジケータ描画
        indicator.draw(guiGraphics, 31 + GUI_X, 1);
        // レシピ進行描画
        cachedProgress.draw(guiGraphics, 24 + GUI_X, 8);
        // レシピの処理時間
        drawRecipeTime(recipe, guiGraphics);
        // 液体の量
        /*FluidStack inputFluid = recipe.getIngredientFluid();
        if(!inputFluid.isEmpty()) {
            guiGraphics.drawString(font, inputFluid.getAmount() +"mb", 5 + GUI_X, 25, 0xFFFFFF);
        }
        FluidStack outputFluid = recipe.getResultFluid();
        if(!outputFluid.isEmpty()) {
            guiGraphics.drawString(font, outputFluid.getAmount() +"mb", 65 + GUI_X, 25, 0xFFFFFF);
        }*/
        StarlightConcentrationRecipe.Conditions conditions = recipe.getConditions();
        // ディメンションの表示
        guiGraphics.blit(TEXTURE, 2, 63, 176, 53, 8, 8);
        guiGraphics.drawString(font, conditions.dimension(), 13,63, 0x808080, false);
        // 時期の表示
        guiGraphics.blit(TEXTURE, 2, 73, 176, 45, 8, 8);
        String dateRange;
        if((conditions.seasonRange().start() + 359)%360 - conditions.seasonRange().end() == 0){
            dateRange = Component.translatable("season.heliopause.year_round").getString();
        }else{
            String startDate = Season.getDateTranslatable((long)((conditions.seasonRange().start()/360.0) * Season.YEAR_LENGTH), 12000L);
            String endDate = Season.getDateTranslatable((long)((conditions.seasonRange().end()/360.0) * Season.YEAR_LENGTH), 12000L);
            dateRange = startDate+" ~ "+endDate;
        }
        guiGraphics.drawString(font, dateRange, 13, 73, 0x808080, false);
        // 必要な星明かりの表示
        guiGraphics.drawString(font, Component.translatable("recipe.heliopause.starlight_concentration.starlight").getString() + " :", 2, 85, 0x808080, false);
        
        // 内訳を表示
        int slotHeight = 96;
        for (String coverageName : conditions.coverage()) {
            Optional<LensBarrelCoverageListener.BarrelCoverageData> dataOpt =
                LensBarrelCoverageListener.DATA.values().stream()
                    .filter(d -> d.name().equals(coverageName))
                    .findFirst();
            if(dataOpt.isPresent()){
                LensBarrelCoverageListener.BarrelCoverageData data = dataOpt.get();
                LensBarrelCoverageIconValue icon = LensBarrelCoverageIconValue.fromString(data.icon());
                if(icon == null){
                    continue;
                }
                int iconIndex = icon.ordinal();
                int[] color = data.icon_color();
                RenderSystem.setShaderColor(color[0]/255f, color[1]/255f, color[2]/255f, 1f);
                guiGraphics.blit(COVERAGE_ICON, 2, slotHeight,iconIndex * 7,0,7,7, 32, 16);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                // 名前を切り出して描画
                String coverageNameTranslatable = Component.translatable("gui.heliopause.lens_barrel_coverage." + data.name()).getString();
                if(font.width(coverageNameTranslatable) >= 85){
                    while(font.width(coverageNameTranslatable) > 80){
                        coverageNameTranslatable = coverageNameTranslatable.substring(0, coverageNameTranslatable.length() - 1);
                    }
                    coverageNameTranslatable = coverageNameTranslatable + "...";
                }
                guiGraphics.drawString(font, coverageNameTranslatable, 13, slotHeight, 0x808080, false);
                slotHeight += 10;
            }
        }
        
        // 星明かりの説明を表示
        if(mouseX >= 2 && mouseX <= background.getWidth() - 4 && mouseY >= 85 && mouseY <= 85 + Minecraft.getInstance().font.lineHeight){
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenMouseX = (int) (mouseX + (double) (screenWidth - this.getWidth()) /2);
            TooltipTransform tooltipTransform = getTooltipTransform(screenWidth, screenMouseX, 10, 200);
            List<FormattedCharSequence> coverageTooltip = new ArrayList<>();
            String[] lines = Component.translatable("recipe.heliopause.starlight_concentration.starlight_description").getString().split("\n", -1);
            int tooltipWidth = 0;
            for (String line : lines) {
                coverageTooltip.addAll(font.split(Component.literal(line), tooltipTransform.width()));
                for (FormattedCharSequence component : coverageTooltip) {
                    tooltipWidth = Math.max(tooltipWidth, font.width(component));
                }
            }
            
            if( tooltipTransform.isLeft()) {
                guiGraphics.renderTooltip(font, coverageTooltip, (int) mouseX - tooltipWidth - 20, (int) mouseY);
            }else{
                guiGraphics.renderTooltip(font, coverageTooltip, (int) mouseX, (int) mouseY);
            }
        }
    }
    
    private void drawRecipeTime(StarlightConcentrationRecipe recipe, GuiGraphics guiGraphics) {
        int recipeTimeSec = recipe.getTime() /20;
        String sec = Component.translatable("gui.jei.category.smelting.time.seconds", recipeTimeSec).getString();
        guiGraphics.drawString(
            Minecraft.getInstance().font, sec,
            88 + GUI_X, 50,
            0x808080,false
        );
    }
    
    public record TooltipTransform(int width, boolean isLeft) {}
    private TooltipTransform getTooltipTransform(int screenWidth, int screenMouseX, int padding, int maxWidth) {
        // 右側に確保できる幅
        int availableRight = Math.max(0, screenWidth - screenMouseX - padding);
        // 左側に確保できる幅
        int availableLeft = Math.max(0, screenMouseX - padding);
        
        // 右に収まるか
        if (availableRight >= maxWidth) {
            return new TooltipTransform(maxWidth, false);
        }
        // 左に収まるか
        if (availableLeft >= maxWidth) {
            return new TooltipTransform(maxWidth, true);
        }
        
        // どちらにも収まらない場合
        if (availableRight >= availableLeft) {
            return new TooltipTransform(availableRight, false);
        } else {
            return new TooltipTransform(availableLeft, true);
        }
    }
    
    @Override
    public RecipeType<StarlightConcentrationRecipe> getRecipeType() {
        return STARLIGHT_CONCENTRATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.heliopause.starlight_concentration");
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
    public void setRecipe(IRecipeLayoutBuilder builder, StarlightConcentrationRecipe recipe, IFocusGroup focuses) {
        // 材料スロット
        Ingredient inputItem = recipe.getIngredientItem();
        if (!inputItem.isEmpty()){
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + GUI_X, 5).addIngredients(inputItem);
        }
        FluidStack inputFluid = recipe.getIngredientFluid();
        if(!inputFluid.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + GUI_X, 25)
                .addFluidStack(inputFluid.getFluid(), inputFluid.getAmount(), inputFluid.getTag())
                .setFluidRenderer(ConcentratorBlockEntity.TANK_CAPACITY, true, 16, 32);
        }
        // 結果スロット
        ItemStack resultItem = recipe.getResultItem(null);
        if (!resultItem.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 65 + GUI_X, 5).addItemStack(resultItem);
        }
        FluidStack outputFluid = recipe.getResultFluid();
        if(!outputFluid.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 65 + GUI_X, 25)
                .addFluidStack(outputFluid.getFluid(), outputFluid.getAmount(), outputFluid.getTag())
                .setFluidRenderer(ConcentratorBlockEntity.TANK_CAPACITY, true, 16, 32);;
        }
    }
}
