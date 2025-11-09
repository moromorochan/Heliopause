package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.CampfireAlchemyRecipe;
import com.moromoro.heliopause.recipe.RoastingRecipe;
import com.moromoro.heliopause.recipe.orreryWhirling.OrreryWhirlingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Heliopause.MODID);

    public static final RegistryObject<RecipeSerializer<RoastingRecipe>> ROASTING_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("roasting",() -> RoastingRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<CampfireAlchemyRecipe>> CAMPFIRE_ALCHEMY_RECIPE_SERIALIZER =
        RECIPE_SERIALIZERS.register("campfire_alchemy",() -> CampfireAlchemyRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<OrreryWhirlingRecipe>> ORRERY_WHIRLING_RECIPE_SERIALIZER =
        RECIPE_SERIALIZERS.register("orrery_whirling",() -> OrreryWhirlingRecipe.Serializer.INSTANCE);

//    public static final RegistryObject<RecipeSerializer<MagicCircleAssemblyRecipe>> CIRCLE_RECIPE_SERIALIZER =
//            RECIPE_SERIALIZERS.register("magic_circle_assembly",() -> MagicCircleAssemblyRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus){
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
