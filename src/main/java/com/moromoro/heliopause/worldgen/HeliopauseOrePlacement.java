package com.moromoro.heliopause.worldgen;

import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class HeliopauseOrePlacement {
    public static List<PlacementModifier> orePlacement(PlacementModifier modifierFirst, PlacementModifier modifierSecond){
        return List.of(modifierFirst, InSquarePlacement.spread(), modifierSecond, BiomeFilter.biome());
    }
    
    public static List<PlacementModifier> commonOrePlacement(int placeCount, PlacementModifier placeHeightRange) {
        return orePlacement(CountPlacement.of(placeCount), placeHeightRange);
    }
    
    public static List<PlacementModifier> rareOrePlacement(int placeChance, PlacementModifier placeHeightRange) {
        return orePlacement(RarityFilter.onAverageOnceEvery(placeChance), placeHeightRange);
    }
}
