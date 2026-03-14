package com.moromoro.heliopause.compat;

import com.moromoro.heliopause.recipe.StellarInstantiationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public class StellarInstantiationCategory implements IRecipeCategory<StellarInstantiationRecipe> {
    @Override
    public RecipeType<StellarInstantiationRecipe> getRecipeType() {
        return null;
    }

    @Override
    public Component getTitle() {
        return null;
    }

    @Override
    public IDrawable getBackground() {
        return null;
    }

    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, StellarInstantiationRecipe stellarInstantiationRecipe, IFocusGroup iFocusGroup) {

    }
}
