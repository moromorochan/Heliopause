package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.worldgen.HeliopauseBiomeModifiers;
import com.moromoro.heliopause.worldgen.HeliopauseConfiguredFeatures;
import com.moromoro.heliopause.worldgen.HeliopausePlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HeliopauseWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(Registries.CONFIGURED_FEATURE, HeliopauseConfiguredFeatures::bootstrap)
        .add(Registries.PLACED_FEATURE, HeliopausePlacedFeatures::bootstrap)
        .add(ForgeRegistries.Keys.BIOME_MODIFIERS, HeliopauseBiomeModifiers::bootstrap);
    
    public HeliopauseWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Heliopause.MODID));
    }
}
