package com.moromoro.heliopause.registry;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Heliopause.MODID);

    public static final RegistryObject<RecipeType<RoastingRecipe>> ROASTING =
            RECIPE_TYPES.register("roasting", () -> RoastingRecipe.Type.INSTANCE);

/*    public static final RegistryObject<RecipeType<CampfireAlchemyRecipe>> CAMPFIRE_ALCHEMY =
        RECIPE_TYPES.register("campfire_alchemy", () -> CampfireAlchemyRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<OrreryWhirlingRecipe>> ORRERY_WHIRLING =
        RECIPE_TYPES.register("orrery_whirling", () -> OrreryWhirlingRecipe.Type.INSTANCE);*/

    public static final RegistryObject<RecipeType<MoonlightPouringRecipe>> MOONLIGHT_POURING =
        RECIPE_TYPES.register(MoonlightPouringRecipe.Type.ID, () -> MoonlightPouringRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<MagicCircleAssemblyRecipe>> MAGIC_CIRCLE_ASSEMBLY =
        RECIPE_TYPES.register(MagicCircleAssemblyRecipe.Type.ID,() -> MagicCircleAssemblyRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<ImitationCoreAssemblyRecipe>> IMITATION_CORE_ASSEMBLY =
        RECIPE_TYPES.register(ImitationCoreAssemblyRecipe.Type.ID,() -> ImitationCoreAssemblyRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<OrreryTransferenceRecipe>> ORRERY_TRANSFERENCE =
        RECIPE_TYPES.register(OrreryTransferenceRecipe.Type.ID,() -> OrreryTransferenceRecipe.Type.INSTANCE);

    public static final RegistryObject<RecipeType<StarlightConcentrationRecipe>> STARLIGHT_CONCENTRATION =
        RECIPE_TYPES.register(StarlightConcentrationRecipe.Type.ID,()-> StarlightConcentrationRecipe.Type.INSTANCE);

    //public static final RegistryObject<RecipeType<>> ORRERY_WHIRLING =

//    public static final RegistryObject<RecipeType<MagicCircleAssemblyRecipe>> CIRCLE_RECIPE =
//            RECIPE_TYPES.register("magic_circle_assembly", () -> MagicCircleAssemblyRecipe.Type.INSTANCE);
}
