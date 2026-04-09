package com.moromoro.heliopause.worldgen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.ArrayList;
import java.util.List;

public class HeliopauseConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVER_ORE_KEY = registerKey("silver_ore");
    
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceable = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        
        List<OreConfiguration.TargetBlockState> silverOres = new ArrayList<>();
        silverOres.add(OreConfiguration.target(stoneReplaceable, BlockRegistry.SILVER_ORE_BLOCK.get().defaultBlockState()));
        silverOres.add(OreConfiguration.target(deepslateReplaceable, BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get().defaultBlockState()));
        register(context, SILVER_ORE_KEY, Feature.ORE, new OreConfiguration(silverOres, 9));
        
        /*register(context, SILVER_ORE_KEY, Feature.ORE,
            new OreConfiguration(deepslateReplaceable, BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK.get().defaultBlockState(), 9));*/
        
    }
    
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Heliopause.MODID, name));
    }
    
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
        BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration)
    {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
