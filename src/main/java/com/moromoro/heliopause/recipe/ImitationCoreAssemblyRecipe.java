package com.moromoro.heliopause.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.enumProperty.ImitationStellarModelValue;
import com.moromoro.heliopause.item.ImitationCoreItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImitationCoreAssemblyRecipe implements Recipe<Container> {

    public record ImitationCoreProperty(String key, String model, float scale, int[] color, float centForce, Fluid fluid, int usagePerSec) {}
    private final ImitationCoreProperty coreProperty;
    private final ResourceLocation recipeId;
    ImitationCoreAssemblyRecipe(ImitationCoreProperty coreProperty, ResourceLocation recipeId){
        this.coreProperty = coreProperty;
        this.recipeId = recipeId;
    }

    public static class Type implements RecipeType<ImitationCoreAssemblyRecipe>{
        public static final ImitationCoreAssemblyRecipe.Type INSTANCE = new ImitationCoreAssemblyRecipe.Type();
        public static final String ID = "imitation_core_assembly";
    }

    public static class Serializer implements RecipeSerializer<ImitationCoreAssemblyRecipe> {
        public static final ImitationCoreAssemblyRecipe.Serializer INSTANCE = new ImitationCoreAssemblyRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, ImitationCoreAssemblyRecipe.Type.ID);

        @Override
        public ImitationCoreAssemblyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String key = GsonHelper.getAsString(json,"key", "");
            String model = GsonHelper.getAsString(json, "model", "gas_plain");
            if(ImitationStellarModelValue.fromString(model) == null){
                Heliopause.LOGGER.warn("Unknown icon '{}' in {}, using gas_plain", model, recipeId);
                model = "gas_plain";
            }
            float scale = GsonHelper.getAsFloat(json, "scale", 1.0f);
            int[] color = new int[]{255,255,255};
            if (json.has("color") && json.get("color").isJsonArray()) {
                JsonArray colorJson = json.getAsJsonArray("color");
                color = new int[]{
                    colorJson.get(0).getAsInt(),
                    colorJson.get(1).getAsInt(),
                    colorJson.get(2).getAsInt()
                };
            }
            float cent = GsonHelper.getAsFloat(json, "cent_force", 0.5f);
            Fluid fluid = Fluids.WATER;
            if (json.has("fluid")) {
                String fluidId = GsonHelper.getAsString(json, "fluid", "");
                if (!fluidId.isEmpty()) {
                    Fluid found = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId));
                    if (found != null) {
                        fluid = found;
                    }
                }
            }
            int usage = GsonHelper.getAsInt(json, "usage_per_sec", 10);

            return new ImitationCoreAssemblyRecipe(new ImitationCoreProperty(key, model, scale, color, cent, fluid, usage), recipeId);
        }

        @Override
        public @Nullable ImitationCoreAssemblyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Heliopause.LOGGER.debug("read from network, {}", recipeId);
            String key = buffer.readUtf();
            String model = buffer.readUtf();
            float scale = buffer.readFloat();
            int[] color = new int[]{buffer.readInt(), buffer.readInt(), buffer.readInt()};
            float cent = buffer.readFloat();
            FluidStack fluidStack = FluidStack.readFromPacket(buffer);
            return new ImitationCoreAssemblyRecipe(new ImitationCoreProperty(key, model, scale, color, cent, fluidStack.getFluid(), fluidStack.getAmount()), recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ImitationCoreAssemblyRecipe recipe) {
            ImitationCoreProperty property = recipe.coreProperty;
            buffer.writeUtf(property.key);
            buffer.writeUtf(property.model);
            buffer.writeFloat(property.scale);
            buffer.writeInt(property.color[0]);
            buffer.writeInt(property.color[1]);
            buffer.writeInt(property.color[2]);
            buffer.writeFloat(property.centForce);
            new FluidStack(property.fluid, property.usagePerSec).writeToPacket(buffer);
        }
    }

    @Override
    public boolean matches(Container container, @NotNull Level level) {
        /*if(level.isClientSide()){
            return false;
        }*/
        // コンテナから中心星を取得
        ItemStack centerItem = container.getItem(0);
        if(!(centerItem.getItem() instanceof ImitationCoreItem)){
            return false;
        }
        // 中心星の種類を確認
        String starId = ImitationCoreItem.getStarId(centerItem);
        if(starId == null || !starId.equals(this.coreProperty.key)){
            return false;
        }
        return true;
    }

    public ImitationCoreProperty getCoreProperty() {
        return coreProperty;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return null;
    }

    public FluidStack getFluidPerSecond() {
        return new FluidStack(coreProperty.fluid, coreProperty.usagePerSec);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ImitationCoreAssemblyRecipe.Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ImitationCoreAssemblyRecipe.Type.INSTANCE;
    }

}
