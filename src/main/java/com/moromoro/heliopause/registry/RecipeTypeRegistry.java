package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.RoastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Heliopause.MODID);

    public static final RegistryObject<RecipeType<RoastingRecipe>> ROASTING =
            RECIPE_TYPES.register("roasting", () -> RoastingRecipe.Type.INSTANCE);

//    public static final RegistryObject<RecipeType<MagicCircleAssemblyRecipe>> CIRCLE_RECIPE =
//            RECIPE_TYPES.register("magic_circle_assembly", () -> MagicCircleAssemblyRecipe.Type.INSTANCE);
}
