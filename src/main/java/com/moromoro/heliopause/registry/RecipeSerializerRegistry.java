package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.RoastingRecipe;
import com.moromoro.heliopause.recipe.SprayingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Heliopause.MODID);

    public static final RegistryObject<RecipeSerializer<RoastingRecipe>> ROASTING_RECIPE_SERIALIZER =
            SERIALIZERS.register("roasting",() -> RoastingRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<SprayingRecipe>> SPRAYING_RECIPE_SERIALIZER =
            SERIALIZERS.register("spraying",() -> SprayingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus){
        SERIALIZERS.register(eventBus);
    }
}
