package com.moromoro.heliopause.compat;

import com.moromoro.heliopause.recipe.StarlightConcentrationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public class StarlightConcentrationCategory implements IRecipeCategory<StarlightConcentrationRecipe> {
    @Override
    public RecipeType<StarlightConcentrationRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, StarlightConcentrationRecipe starlightConcentrationRecipe, IFocusGroup iFocusGroup) {

    }
}
