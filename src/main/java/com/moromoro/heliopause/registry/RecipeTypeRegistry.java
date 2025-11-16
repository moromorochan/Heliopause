package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.CampfireAlchemyRecipe;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipe;
import com.moromoro.heliopause.recipe.RoastingRecipe;
import com.moromoro.heliopause.recipe.orreryWhirling.OrreryWhirlingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Heliopause.MODID);

    public static final RegistryObject<RecipeType<RoastingRecipe>> ROASTING =
            RECIPE_TYPES.register("roasting", () -> RoastingRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<CampfireAlchemyRecipe>> CAMPFIRE_ALCHEMY =
        RECIPE_TYPES.register("campfire_alchemy", () -> CampfireAlchemyRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<OrreryWhirlingRecipe>> ORRERY_WHIRLING =
        RECIPE_TYPES.register("orrery_whirling", () -> OrreryWhirlingRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<MoonlightPouringRecipe>> MOONLIGHT_POURING =
        RECIPE_TYPES.register("moonlight_pouring", () -> MoonlightPouringRecipe.Type.INSTANCE);

    //public static final RegistryObject<RecipeType<>> ORRERY_WHIRLING =

//    public static final RegistryObject<RecipeType<MagicCircleAssemblyRecipe>> CIRCLE_RECIPE =
//            RECIPE_TYPES.register("magic_circle_assembly", () -> MagicCircleAssemblyRecipe.Type.INSTANCE);
}
