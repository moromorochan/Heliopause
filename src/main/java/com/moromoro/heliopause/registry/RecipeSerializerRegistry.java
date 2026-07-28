package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Heliopause.MODID);

    public static final RegistryObject<RecipeSerializer<RoastingRecipe>> ROASTING_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("roasting",() -> RoastingRecipe.Serializer.INSTANCE);
    
    public static final RegistryObject<RecipeSerializer<MoonlightPouringRecipe>> MOONLIGHT_POURING =
        RECIPE_SERIALIZERS.register(MoonlightPouringRecipe.Type.ID,() -> MoonlightPouringRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MagicCircleAssemblyRecipe>> MAGIC_CIRCLE_ASSEMBLY =
        RECIPE_SERIALIZERS.register(MagicCircleAssemblyRecipe.Type.ID,() -> MagicCircleAssemblyRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<ImitationCoreAssemblyRecipe>> IMITATION_CORE_ASSEMBLY =
        RECIPE_SERIALIZERS.register(ImitationCoreAssemblyRecipe.Type.ID,() -> ImitationCoreAssemblyRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<OrreryTransferenceRecipe>> ORRERY_TRANSFERENCE =
        RECIPE_SERIALIZERS.register(OrreryTransferenceRecipe.Type.ID,() -> OrreryTransferenceRecipe.Serializer.INSTANCE);
    
    public static final RegistryObject<RecipeSerializer<StarlightConcentrationRecipe>> STARLIGHT_CONCENTRATION =
        RECIPE_SERIALIZERS.register(StarlightConcentrationRecipe.Type.ID,() -> StarlightConcentrationRecipe.Serializer.INSTANCE);

//    public static final RegistryObject<RecipeSerializer<MagicCircleAssemblyRecipe>> CIRCLE_RECIPE_SERIALIZER =
//            RECIPE_SERIALIZERS.register("magic_circle_assembly",() -> MagicCircleAssemblyRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus){
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
