package com.moromoro.heliopause.compat.jei;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.*;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import com.moromoro.heliopause.screen.ConcentratorScreen;
import com.moromoro.heliopause.screen.RoastingTableScreen;
import com.moromoro.heliopause.screen.SiderostatScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

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
        registration.addRecipeCategories(new MoonlightPouringCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MagicCircleAssemblyCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new OrreryTransferenceCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new StarlightConcentrationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    //レシピの登録
    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        RecipeManager recipeManager = level.getRecipeManager();

        List<RoastingRecipe> roastingRecipes = recipeManager.getAllRecipesFor(RoastingRecipe.Type.INSTANCE);
        registration.addRecipes(RoastingCategory.ROASTING_TYPE, roastingRecipes);

        List<MoonlightPouringRecipe> moonlightPouringRecipes = recipeManager.getAllRecipesFor(MoonlightPouringRecipe.Type.INSTANCE);
        registration.addRecipes(MoonlightPouringCategory.MOONLIGHT_POURING_TYPE, moonlightPouringRecipes);

        List<MagicCircleAssemblyRecipe> magicCircleAssemblyRecipes = recipeManager.getAllRecipesFor(MagicCircleAssemblyRecipe.Type.INSTANCE);
        registration.addRecipes(MagicCircleAssemblyCategory.MAGIC_CIRCLE_ASSEMBLY_TYPE, magicCircleAssemblyRecipes);

        List<OrreryTransferenceRecipe> orreryTransferenceRecipes = recipeManager.getAllRecipesFor(OrreryTransferenceRecipe.Type.INSTANCE);
        registration.addRecipes(OrreryTransferenceCategory.ORRERY_TRANSFERENCE_TYPE, orreryTransferenceRecipes);
        
        List<StarlightConcentrationRecipe> starlightConcentrationRecipes = recipeManager.getAllRecipesFor(StarlightConcentrationRecipe.Type.INSTANCE);
        registration.addRecipes(StarlightConcentrationCategory.STARLIGHT_CONCENTRATION_TYPE, starlightConcentrationRecipes);
    }

    //クリックしたときにレシピを表示させる範囲の設定
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //IModPlugin.super.registerGuiHandlers(registration);
        registration.addRecipeClickArea(
                RoastingTableScreen.class,90,35,22,15, RoastingCategory.ROASTING_TYPE
        );

        registration.addRecipeClickArea(
                SiderostatScreen.class, 81,39,14,16, MoonlightPouringCategory.MOONLIGHT_POURING_TYPE
        );
        
        registration.addRecipeClickArea(
            ConcentratorScreen.class, 77,21, 22, 43, StarlightConcentrationCategory.STARLIGHT_CONCENTRATION_TYPE
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        //IModPlugin.super.registerRecipeCatalysts(registration);
        registration.addRecipeCatalyst(BlockRegistry.ROASTING_TABLE.get().asItem().getDefaultInstance(), RoastingCategory.ROASTING_TYPE);
        registration.addRecipeCatalyst(BlockRegistry.SIDEROSTAT_TOP.get().asItem().getDefaultInstance(),MoonlightPouringCategory.MOONLIGHT_POURING_TYPE);

        registration.addRecipeCatalyst(ItemRegistry.COMPASS_ITEM.get().getDefaultInstance(), MagicCircleAssemblyCategory.MAGIC_CIRCLE_ASSEMBLY_TYPE);
        registration.addRecipeCatalyst(BlockRegistry.BLACKBOARD.get().asItem().getDefaultInstance(), MagicCircleAssemblyCategory.MAGIC_CIRCLE_ASSEMBLY_TYPE);

        //registration.addRecipeCatalyst(ItemRegistry.IMITATION_CORE_ITEM.get().getDefaultInstance(), OrreryTransferenceCategory.ORRERY_TRANSFERENCE_TYPE);
        registration.addRecipeCatalyst(BlockRegistry.CRUCIBLE.get().asItem().getDefaultInstance(), OrreryTransferenceCategory.ORRERY_TRANSFERENCE_TYPE);
        registration.addRecipeCatalyst(BlockRegistry.ORRERY_CIRCLE_BOARD.get().asItem().getDefaultInstance(), OrreryTransferenceCategory.ORRERY_TRANSFERENCE_TYPE);
        
        registration.addRecipeCatalyst(BlockRegistry.CONCENTRATOR.get().asItem().getDefaultInstance(), StarlightConcentrationCategory.STARLIGHT_CONCENTRATION_TYPE);
        ITagManager<Block> tagManager = ForgeRegistries.BLOCKS.tags();
        if(tagManager != null){
            for (Block block : tagManager.getTag(TagRegistry.Blocks.LENS_BARREL)) {
                if (block.asItem() != Items.AIR) {
                    registration.addRecipeCatalyst(block.asItem().getDefaultInstance(), StarlightConcentrationCategory.STARLIGHT_CONCENTRATION_TYPE);
                }
            }
        }
    }
}
