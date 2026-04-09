package com.moromoro.heliopause.compat.jei;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.RoastingRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RoastingCategory implements IRecipeCategory<RoastingRecipe> {
    //public static final ResourceLocation UID = new ResourceLocation(Heliopause.MODID,"roasting");
    public static final ResourceLocation TEXTURE = new ResourceLocation(Heliopause.MODID,
            "textures/gui/container/alchemy_roasting_table.png");

    public static final RecipeType<RoastingRecipe> ROASTING_TYPE =new RecipeType<>(RoastingRecipe.Serializer.ID, RoastingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated cachedFire;

    //初期化
    public RoastingCategory(IGuiHelper helper) {
        //背景とアイコンを指定
        this.background = helper.createDrawable(TEXTURE,23,23,147-23,77-23);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.ROASTING_TABLE.get()));
        //アニメーションさせるスプライトを切り出す
        IDrawableStatic staticFire = helper.createDrawable(TEXTURE, 176, 0, 33, 7);
        //アニメーションを登録
        this.cachedFire = helper.createAnimatedDrawable(staticFire,300,IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public RecipeType<RoastingRecipe> getRecipeType() {
        return ROASTING_TYPE;
    }

    @Override
    public Component getTitle() {
        //レシピの名前を翻訳ファイルから取得
        return Component.translatable("recipe.heliopause.roasting");
    }

    @Override
    public IDrawable getBackground() {
        //背景テクスチャの取得
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        //レシピタブに表示するアイコンの取得
        return this.icon;
    }

    public IDrawableAnimated getCachedFire() {
        return cachedFire;
    }

    //描画処理
    @Override
    public void draw(RoastingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        //炎を描画
        getCachedFire().draw(guiGraphics,48-23, 63-23);
    }

    //アイテムを表示するスロット位置を用意する
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RoastingRecipe recipe, IFocusGroup focuses) {
        //素材スロット
        for(int i = 0; i < 2; ++i) {
            for(int j = 0; j < 2; ++j) {
                builder.addSlot(RecipeIngredientRole.INPUT, 48-23 + j * 18, 26-23 + i * 18).addIngredients(recipe.getIngredients().get(j + i * 2));
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT,124-23,35-23).addItemStack(recipe.getResultItem(null));
    }
}
