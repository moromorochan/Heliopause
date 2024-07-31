package com.moromoro.heliopause.worldgen.tree;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;

public class PaperBushTreeGrower extends AbstractTreeGrower {
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource randomSource, boolean p_255923_) {
        return TreeFeatures.AZALEA_TREE;
    }
}
