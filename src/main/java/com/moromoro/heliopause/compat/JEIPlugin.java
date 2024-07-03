package com.moromoro.heliopause.compat;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.RoastingRecipe;
import com.moromoro.heliopause.screen.RoastingTableScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Heliopause.MODID,"jei_plugin");
    }

    //レシピカテゴリの登録
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RoastingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    //レシピの登録
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<RoastingRecipe> roastingRecipes = recipeManager.getAllRecipesFor(RoastingRecipe.Type.INSTANCE);
        registration.addRecipes(RoastingCategory.ROASTING_TYPE, roastingRecipes);
    }

    //クリックしたときにレシピを表示させる範囲の設定
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //IModPlugin.super.registerGuiHandlers(registration);
        registration.addRecipeClickArea(
                RoastingTableScreen.class,90,35,22,15, RoastingCategory.ROASTING_TYPE
        );
    }
}
