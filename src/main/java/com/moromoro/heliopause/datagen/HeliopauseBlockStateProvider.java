package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.RectangularBlock;
import com.moromoro.heliopause.registry.enumProperty.SiderostatTopState;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.block.SiderostatTopBlock;
import com.moromoro.heliopause.block.AbstractWrittenBoardBlock;
import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.FluidRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class HeliopauseBlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {
    public HeliopauseBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Heliopause.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(BlockRegistry.ALCHEMY_BIRON_BLOCK);
        blockWithItem(BlockRegistry.POLISHED_BIRON_BLOCK);
        rectangularBlockRandomTexture(BlockRegistry.CHISELED_BIRON_BLOCK);
        rectangularBlock(BlockRegistry.ALCHEMY_BIRON_GLASS, "cutout");
        blockWithItem(BlockRegistry.GLOWSTONE_ALLOY_BLOCK);
        blockWithItem(BlockRegistry.SILVER_BLOCK);
        blockWithItem(BlockRegistry.RAW_SILVER_BLOCK);
        blockWithItem(BlockRegistry.SILVER_ORE_BLOCK);
        blockWithItem(BlockRegistry.DEEPSLATE_SILVER_ORE_BLOCK);
        
        customModelBlockWithItem(BlockRegistry.CRUCIBLE);
        litableBlockWithItem(BlockRegistry.ROASTING_TABLE,false);
        //customModelBlockWithItem(BlockRegistry.ALCHEMY_CAMPFIRE);

        // モデルなしブロック
        simpleBlock(BlockRegistry.STELLAR_INGREDIENT_BLOCK.get(), models().getExistingFile(mcLoc("block/air")));
        //simpleBlock(BlockRegistry.STARLIGHT_CONCENTRATOR_INTERFACE.get(), models().getExistingFile(mcLoc("block/air")));

        /*simpleBlockWithItem(BlockRegistry.PENETRATOR.get(), models().getExistingFile(modLoc("block/penetrator")));
        cylinderBlockWithItem(BlockRegistry.CONVERGE_CYLINDER);*/

        //simpleBlockWithItem(BlockRegistry.WOODEN_LENS_BARREL_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/wooden_barrel")));
        //simpleBlockWithItem(BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/wooden_main")));
        //simpleBlockWithItem(BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/wooden_secondary")));
        //simpleBlockItem(BlockRegistry.STARLIGHT_CONCENTRATOR_BASE.get(), models().getExistingFile(modLoc("block/starlight_concentrator/base_item")));

        simpleBlockItem(BlockRegistry.CONCENTRATOR.get(), models().getExistingFile(modLoc("block/concentrator/base_disable")));
        
        // 鏡筒
        lensBarrelBlockWithItem(BlockRegistry.WOODEN_LENS_BARREL_BLOCK, "wooden");
        lensBarrelBlockWithItem(BlockRegistry.STONE_LENS_BARREL_BLOCK, "stone");
        lensBarrelBlockWithItem(BlockRegistry.ALCHEMY_BIRON_LENS_BARREL_BLOCK, "alchemy_biron");
        lensBarrelBlockWithItem(BlockRegistry.THERMOIMMOBILANT_LENS_BARREL_BLOCK, "thermoimmobilant");
        // 鏡
        simpleBlockWithItem(BlockRegistry.IRON_MAIN_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/iron_mirror/main")));
        simpleBlockWithItem(BlockRegistry.IRON_SECOND_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/iron_mirror/secondary")));
        simpleBlockWithItem(BlockRegistry.GRAPHITE_MAIN_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/graphite_mirror/main")));
        simpleBlockWithItem(BlockRegistry.GRAPHITE_SECOND_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/graphite_mirror/secondary")));
        simpleBlockWithItem(BlockRegistry.SILVER_MAIN_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/silver_mirror/main")));
        simpleBlockWithItem(BlockRegistry.SILVER_SECOND_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/silver_mirror/secondary")));
        simpleBlockWithItem(BlockRegistry.QUINCE_STEEL_MAIN_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/quince_steel_mirror/main")));
        simpleBlockWithItem(BlockRegistry.QUINCE_STEEL_SECOND_MIRROR_BLOCK.get(), models().getExistingFile(modLoc("block/lens_barrel/quince_steel_mirror/secondary")));
        //lensBarrelBlockWithItem(BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK, "main","wooden");
        //lensBarrelBlockWithItem(BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK, "secondary","wooden");

        //simpleBlock(BlockRegistry.CENTRAL_STAR.get(),models().getExistingFile(mcLoc("block/air")));
        //simpleBlock(BlockRegistry.SIDEROSTAT_MOON.get(), models().getExistingFile(modLoc("block/celestial_bodies/moon")));

        simpleBlock(BlockRegistry.SIDEROSTAT_BASE.get(), models().getExistingFile(modLoc("block/siderostat/mount")));
        //simpleBlockItem(BlockRegistry.SIDEROSTAT_BASE.get(), models().getExistingFile(modLoc("item/siderostat")));
        //simpleBlock(BlockRegistry.SIDEROSTAT_ORB.get(), models().getExistingFile(modLoc("block/siderostat/orb")));
        //simpleBlock(BlockRegistry.SIDEROSTAT_TOP.get(), models().getExistingFile(modLoc("block/siderostat/bow")));
        bowBlock(BlockRegistry.SIDEROSTAT_TOP, models().getExistingFile(modLoc("block/siderostat/bow")));
        //simpleBlock(BlockRegistry.SIDEROSTAT_MOTOR.get(), models().getExistingFile(modLoc("block/siderostat/motor")));

        simpleBlockWithItem(BlockRegistry.BLACKBOARD.get(),
            models().cubeBottomTop(BlockRegistry.BLACKBOARD.getId().getPath(),
                modLoc("block/blackboard/side"),
                modLoc("block/blackboard/bottom"),
                modLoc("block/blackboard/top")
                ));
        writtenBoardBlock(BlockRegistry.WRITTEN_BOARD);
        largeWrittenBoardBlock(BlockRegistry.ORRERY_CIRCLE_BOARD);
        largeWrittenBoardBlock(BlockRegistry.ALT_AZIMUTH_CIRCLE_BOARD);

        /*fluidBlock(FluidRegistry.STARRY_MIXTURE,"cutout");
        fluidBlock(FluidRegistry.LIQUEFIED_STARLIGHT, "translucent");
        fluidBlock(FluidRegistry.LIQUEFIED_TWILIGHT, "opacue");*/
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void slabBlockWithItem(RegistryObject<SlabBlock> slabBlockRegistryObject, RegistryObject<Block> baseBlock) {
        getVariantBuilder(slabBlockRegistryObject.get())
            .partialState()
            .with(SlabBlock.TYPE, SlabType.BOTTOM)
            .modelForState()
            .modelFile(models().slab(
                slabBlockRegistryObject.getId().getPath(),
                modLoc("block/" + baseBlock.getId().getPath()),
                modLoc("block/" + baseBlock.getId().getPath()),
                modLoc("block/" + baseBlock.getId().getPath())
            ))
            .addModel();

        getVariantBuilder(slabBlockRegistryObject.get())
            .partialState()
            .with(SlabBlock.TYPE, SlabType.TOP)
            .modelForState()
            .modelFile(models().slabTop(
                slabBlockRegistryObject.getId().getPath() + "_top",
                modLoc("block/" + baseBlock.getId().getPath()),
                modLoc("block/" + baseBlock.getId().getPath()),
                modLoc("block/" + baseBlock.getId().getPath())
            ))
            .addModel();

        getVariantBuilder(slabBlockRegistryObject.get())
            .partialState()
            .with(SlabBlock.TYPE, SlabType.DOUBLE)
            .modelForState()
            .modelFile(models().getExistingFile(modLoc("block/" + baseBlock.getId().getPath())))
            .addModel();

        simpleBlockItem(slabBlockRegistryObject.get(), models().slab(
            slabBlockRegistryObject.getId().getPath(),
            modLoc("block/" + baseBlock.getId().getPath()),
            modLoc("block/" + baseBlock.getId().getPath()),
            modLoc("block/" + baseBlock.getId().getPath())
        ));
    }

    private void stairsBlockWithItem(RegistryObject<StairBlock> stairsBlockRegistryObject, RegistryObject<Block> baseBlock) {
        stairsBlock(stairsBlockRegistryObject.get(), modLoc("block/" + baseBlock.getId().getPath()));
        simpleBlockItem(stairsBlockRegistryObject.get(), models().stairs(
            stairsBlockRegistryObject.getId().getPath(),
            modLoc("block/" + baseBlock.getId().getPath()),
            modLoc("block/" + baseBlock.getId().getPath()),
            modLoc("block/" + baseBlock.getId().getPath())
        ));
    }

    private void wallBlockWithItem(RegistryObject<WallBlock> wallBlockRegistryObject, RegistryObject<Block> baseBlock) {
        wallBlock(wallBlockRegistryObject.get(), modLoc("block/" + baseBlock.getId().getPath()));
        simpleBlockItem(wallBlockRegistryObject.get(), models().wallInventory(wallBlockRegistryObject.getId().getPath(),
            modLoc("block/" + baseBlock.getId().getPath())));
    }

    private void pillarBlockWithItem(RegistryObject<RotatedPillarBlock> pillarBlockRegistryObject) {
        axisBlock(pillarBlockRegistryObject.get(),
            modLoc("block/" + pillarBlockRegistryObject.getId().getPath()),
            modLoc("block/" + pillarBlockRegistryObject.getId().getPath() + "_top")
            );
        simpleBlockItem(pillarBlockRegistryObject.get(), models().cubeColumn(
            pillarBlockRegistryObject.getId().getPath(),
            modLoc("block/" + pillarBlockRegistryObject.getId().getPath()),
            modLoc("block/" + pillarBlockRegistryObject.getId().getPath() + "_top")
        ));
    }
    private void pillarBlockWithItem(RegistryObject<RotatedPillarBlock> pillarBlockRegistryObject,String sideTexturePath, String topTexturePath) {
        axisBlock(pillarBlockRegistryObject.get(),
            modLoc(sideTexturePath),
            modLoc(topTexturePath)
        );
        simpleBlockItem(pillarBlockRegistryObject.get(), models().cubeColumn(
            pillarBlockRegistryObject.getId().getPath(),
            modLoc(sideTexturePath),
            modLoc(topTexturePath)
        ));
    }

    private void customModelBlockWithItem(RegistryObject<? extends Block> blockRegistryObject){
        simpleBlock(blockRegistryObject.get(), models().getExistingFile(modLoc("block/"+blockRegistryObject.getId().getPath())));
        simpleBlockItem(blockRegistryObject.get(),models().getExistingFile(modLoc("block/"+blockRegistryObject.getId().getPath())));
    }
    
    private void rectangularBlock(RegistryObject<? extends RectangularBlock> blockRegistryObject, String renderType){
        final BooleanProperty[] blockHorizontal = {RectangularBlock.SOUTH, RectangularBlock.WEST, RectangularBlock.NORTH, RectangularBlock.EAST};
        String parentModelPath = "block/"+BlockRegistry.CHISELED_BIRON_BLOCK.getId().getPath() + "/";
        String modelPath = "block/"+blockRegistryObject.getId().getPath();
        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockRegistryObject.get());
        
        // 上下面
        builder.part()
            .modelFile(models().withExistingParent(
                modLoc(modelPath + "/" + "top").getPath(), modLoc(parentModelPath + "top"))
                .texture("top",modelPath + "_top")
                .texture("particle", modelPath + "_top")
                .renderType(renderType)
            )
            .addModel();
        
        for (int up = 0; up <= 1; up++) {
            boolean upState = up == 1;
            for (int down = 0; down <= 1; down++) {
                boolean downState = down == 1;
                // 上下の状態
                String verticalState;
                if(upState){
                    if(downState)
                        verticalState = "middle/";
                    else
                        verticalState = "bottom/";
                }else{
                    if(downState)
                        verticalState = "top/";
                    else
                        verticalState = "single/";
                }
                
                // シングル
                ResourceLocation parentSingleModelPath = modLoc(parentModelPath + verticalState + "single");
                ResourceLocation singleModelPath = modLoc(modelPath + "/" + verticalState + "single");
                builder.part()
                    .modelFile(models().withExistingParent(singleModelPath.getPath(), parentSingleModelPath)
                        .texture("side", modelPath)
                        .texture("particle", modelPath + "_top")
                        .renderType(renderType)
                    )
                    .addModel()
                    .condition(blockHorizontal[0], false)
                    .condition(blockHorizontal[1], false)
                    .condition(blockHorizontal[2], false)
                    .condition(blockHorizontal[3], false)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                // インナー
                ResourceLocation parentInnerModelPath = modLoc(parentModelPath + verticalState + "inner");
                ResourceLocation innerModelPath = modLoc(modelPath + "/" + verticalState + "inner");
                builder.part()
                    .modelFile(models().withExistingParent(innerModelPath.getPath(), parentInnerModelPath)
                        .texture("side", modelPath)
                        .texture("particle", modelPath + "_top")
                        .renderType(renderType)
                    )
                    .addModel()
                    .condition(blockHorizontal[0], true)
                    .condition(blockHorizontal[1], true)
                    .condition(blockHorizontal[2], true)
                    .condition(blockHorizontal[3], true)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                builder.part()
                    .modelFile(models().getExistingFile(innerModelPath))
                    .addModel()
                    .condition(blockHorizontal[0], true)
                    .condition(blockHorizontal[1], false)
                    .condition(blockHorizontal[2], true)
                    .condition(blockHorizontal[3], false)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                builder.part()
                    .modelFile(models().getExistingFile(innerModelPath))
                    .addModel()
                    .condition(blockHorizontal[0], false)
                    .condition(blockHorizontal[1], true)
                    .condition(blockHorizontal[2], false)
                    .condition(blockHorizontal[3], true)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                ResourceLocation sideParentModelPath = modLoc(parentModelPath + verticalState + "side");
                ResourceLocation sideModelPath = modLoc(modelPath + "/" + verticalState + "side");
                ResourceLocation cornerParentModelPath = modLoc(parentModelPath + verticalState + "corner");
                ResourceLocation cornerModelPath = modLoc(modelPath + "/" + verticalState + "corner");
                
                ModelFile sideModel = models().withExistingParent(sideModelPath.getPath(), sideParentModelPath)
                    .texture("side", modelPath)
                    .texture("particle", modelPath + "_top")
                    .renderType(renderType);
                ModelFile cornerModel = models().withExistingParent(cornerModelPath.getPath(), cornerParentModelPath)
                    .texture("side", modelPath)
                    .texture("particle", modelPath + "_top")
                    .renderType(renderType);
                
                for (int direction = 0; direction < 4; direction++) {
                    // サイド
                    builder.part()
                        .modelFile(sideModel)
                        .rotationY(direction * 90)
                        .addModel()
                        .condition(blockHorizontal[(0 + direction) % 4], true)
                        .condition(blockHorizontal[(1 + direction) % 4], false)
                        .condition(blockHorizontal[(2 + direction) % 4], false)
                        .condition(blockHorizontal[(3 + direction) % 4], false)
                        .condition(RectangularBlock.UP, upState)
                        .condition(RectangularBlock.DOWN, downState);
                    
                    // コーナー
                    builder.part()
                        .modelFile(cornerModel)
                        .rotationY(direction * 90)
                        .addModel()
                        .condition(blockHorizontal[(0 + direction) % 4], true)
                        .condition(blockHorizontal[(1 + direction) % 4], true)
                        .condition(blockHorizontal[(2 + direction) % 4], false)
                        .condition(blockHorizontal[(3 + direction) % 4], false)
                        .condition(RectangularBlock.UP, upState)
                        .condition(RectangularBlock.DOWN, downState);
                    
                    // インナー
                    builder.part()
                        .modelFile(models().getExistingFile(innerModelPath))
                        .addModel()
                        .condition(blockHorizontal[(0 + direction) % 4], true)
                        .condition(blockHorizontal[(1 + direction) % 4], true)
                        .condition(blockHorizontal[(2 + direction) % 4], true)
                        .condition(blockHorizontal[(3 + direction) % 4], false)
                        .condition(RectangularBlock.UP, upState)
                        .condition(RectangularBlock.DOWN, downState);
                }
            }
        }
    }
    
    private void rectangularBlockRandomTexture(RegistryObject<? extends RectangularBlock> blockRegistryObject){
        final BooleanProperty[] blockHorizontal = {RectangularBlock.SOUTH, RectangularBlock.WEST, RectangularBlock.NORTH, RectangularBlock.EAST};
        String modelPath = "block/"+blockRegistryObject.getId().getPath() + "/";
        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockRegistryObject.get());
        
        // 上下面
        builder.part()
            .modelFile(models().getExistingFile(modLoc(modelPath + "top")))
            .addModel();
        
        for (int up = 0; up <= 1; up++) {
            boolean upState = up == 1;
            for (int down = 0; down <= 1; down++) {
                boolean downState = down == 1;
                // 上下の状態
                String verticalState;
                if(upState){
                    if(downState)
                        verticalState = "middle/";
                    else
                        verticalState = "bottom/";
                }else{
                    if(downState)
                        verticalState = "top/";
                    else
                        verticalState = "single/";
                }
                
                // シングル
                ResourceLocation singleModelPath = modLoc(modelPath + verticalState + "single");
                builder.part()
                    .modelFile(models().getExistingFile(singleModelPath)).nextModel()
                    .modelFile(models().withExistingParent( singleModelPath + "_b",singleModelPath).texture("side",modelPath+"b")).nextModel()
                    .modelFile(models().withExistingParent( singleModelPath + "_c",singleModelPath).texture("side",modelPath+"c")).nextModel()
                    .modelFile(models().withExistingParent( singleModelPath + "_d",singleModelPath).texture("side",modelPath+"d"))
                    .addModel()
                    .condition(blockHorizontal[0], false)
                    .condition(blockHorizontal[1], false)
                    .condition(blockHorizontal[2], false)
                    .condition(blockHorizontal[3], false)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                // インナー
                ResourceLocation innerModelPath = modLoc(modelPath + verticalState + "inner");
                builder.part()
                    .modelFile(models().getExistingFile(innerModelPath)).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_b",innerModelPath).texture("side",modelPath+"b")).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_c",innerModelPath).texture("side",modelPath+"c")).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_d",innerModelPath).texture("side",modelPath+"d"))
                    .addModel()
                    .condition(blockHorizontal[0], true)
                    .condition(blockHorizontal[1], true)
                    .condition(blockHorizontal[2], true)
                    .condition(blockHorizontal[3], true)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                builder.part()
                    .modelFile(models().getExistingFile(innerModelPath)).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_b",innerModelPath).texture("side",modelPath+"b")).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_c",innerModelPath).texture("side",modelPath+"c")).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_d",innerModelPath).texture("side",modelPath+"d"))
                    .addModel()
                    .condition(blockHorizontal[0], true)
                    .condition(blockHorizontal[1], false)
                    .condition(blockHorizontal[2], true)
                    .condition(blockHorizontal[3], false)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                builder.part()
                    .modelFile(models().getExistingFile(innerModelPath)).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_b",innerModelPath).texture("side",modelPath+"b")).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_c",innerModelPath).texture("side",modelPath+"c")).nextModel()
                    .modelFile(models().withExistingParent( innerModelPath + "_d",innerModelPath).texture("side",modelPath+"d"))
                    .addModel()
                    .condition(blockHorizontal[0], false)
                    .condition(blockHorizontal[1], true)
                    .condition(blockHorizontal[2], false)
                    .condition(blockHorizontal[3], true)
                    .condition(RectangularBlock.UP, upState)
                    .condition(RectangularBlock.DOWN, downState);
                
                ResourceLocation sideModelPath = modLoc(modelPath + verticalState + "side");
                ResourceLocation cornerModelPath = modLoc(modelPath + verticalState + "corner");
                
                for (int direction = 0; direction < 4; direction++) {
                    // サイド
                    builder.part()
                        .modelFile(models().getExistingFile(sideModelPath)).rotationY(direction * 90).nextModel()
                        .modelFile(models().withExistingParent( sideModelPath + "_b",sideModelPath).texture("side",modelPath+"b")).rotationY(direction * 90).nextModel()
                        .modelFile(models().withExistingParent( sideModelPath + "_c",sideModelPath).texture("side",modelPath+"c")).rotationY(direction * 90).nextModel()
                        .modelFile(models().withExistingParent( sideModelPath + "_d",sideModelPath).texture("side",modelPath+"d")).rotationY(direction * 90)
                        .addModel()
                        .condition(blockHorizontal[(0 + direction) % 4], true)
                        .condition(blockHorizontal[(1 + direction) % 4], false)
                        .condition(blockHorizontal[(2 + direction) % 4], false)
                        .condition(blockHorizontal[(3 + direction) % 4], false)
                        .condition(RectangularBlock.UP, upState)
                        .condition(RectangularBlock.DOWN, downState);
                    
                    // コーナー
                    builder.part()
                        .modelFile(models().getExistingFile(cornerModelPath)).rotationY(direction * 90).nextModel()
                        .modelFile(models().withExistingParent( cornerModelPath + "_b",cornerModelPath).texture("side",modelPath+"b")).rotationY(direction * 90).nextModel()
                        .modelFile(models().withExistingParent( cornerModelPath + "_c",cornerModelPath).texture("side",modelPath+"c")).rotationY(direction * 90).nextModel()
                        .modelFile(models().withExistingParent( cornerModelPath + "_d",cornerModelPath).texture("side",modelPath+"d")).rotationY(direction * 90)
                        .addModel()
                        .condition(blockHorizontal[(0 + direction) % 4], true)
                        .condition(blockHorizontal[(1 + direction) % 4], true)
                        .condition(blockHorizontal[(2 + direction) % 4], false)
                        .condition(blockHorizontal[(3 + direction) % 4], false)
                        .condition(RectangularBlock.UP, upState)
                        .condition(RectangularBlock.DOWN, downState);
                    
                    // インナー
                    builder.part()
                        .modelFile(models().getExistingFile(innerModelPath)).nextModel()
                        .modelFile(models().withExistingParent( innerModelPath + "_b",innerModelPath).texture("side",modelPath+"b")).nextModel()
                        .modelFile(models().withExistingParent( innerModelPath + "_c",innerModelPath).texture("side",modelPath+"c")).nextModel()
                        .modelFile(models().withExistingParent( innerModelPath + "_d",innerModelPath).texture("side",modelPath+"d"))
                        .addModel()
                        .condition(blockHorizontal[(0 + direction) % 4], true)
                        .condition(blockHorizontal[(1 + direction) % 4], true)
                        .condition(blockHorizontal[(2 + direction) % 4], true)
                        .condition(blockHorizontal[(3 + direction) % 4], false)
                        .condition(RectangularBlock.UP, upState)
                        .condition(RectangularBlock.DOWN, downState);
                }
            }
        }
    }

    private void customModelHorizontalRotationalBlockWithItem(RegistryObject<? extends Block> blockRegistryObject){
        horizontalBlock(blockRegistryObject.get(), models().getExistingFile(modLoc("block/"+blockRegistryObject.getId().getPath())));
        simpleBlockItem(blockRegistryObject.get(),models().getExistingFile(modLoc("block/"+blockRegistryObject.getId().getPath())));
    }

    private void bowBlock(RegistryObject<? extends Block> blockRegistryObject, ModelFile model){
        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(SiderostatTopBlock.FACING_SIDEROSTAT, SiderostatTopState.FULL)
            .modelForState().modelFile(model).rotationY(90).addModel()

            .partialState().with(SiderostatTopBlock.FACING_SIDEROSTAT, SiderostatTopState.EMPTY)
            .modelForState().modelFile(model).rotationY(90).rotationX(180).addModel()

            .partialState().with(SiderostatTopBlock.FACING_SIDEROSTAT, SiderostatTopState.MOVING)
            .modelForState().modelFile(model).rotationY(90).rotationX(270).addModel();
    }

    private void cylinderBlockWithItem(RegistryObject<? extends Block> blockRegistryObject){
        //テクスチャパスを用意
        String modelName = blockRegistryObject.getId().getPath();
        ResourceLocation upperSideTex = modLoc("block/" + modelName + "/upper");
        ResourceLocation lowerSideTex = modLoc("block/" + modelName + "/lower");
        ResourceLocation topTex = modLoc("block/" + modelName + "/top");

        //ブロックステートの設定
        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(BlockStateProperties.HALF, Half.TOP)
            .modelForState().modelFile(models().cubeColumn(modelName + "_upper", upperSideTex, topTex)).addModel()
            .partialState().with(BlockStateProperties.HALF, Half.BOTTOM)
            .modelForState().modelFile(models().cubeColumn(modelName + "_lower", lowerSideTex, topTex)).addModel();

        //アイテム設定
        simpleBlockItem(blockRegistryObject.get(),models().withExistingParent(modelName, modLoc("item/cylinder_item"))
            .texture("upper", upperSideTex)
            .texture("lower", lowerSideTex)
            .texture("top", topTex)
        );
    }

    private void lensBarrelBlockWithItem(RegistryObject<? extends LensBarrelBlock> blockRegistryObject, String type){
        // パスを用意
        String modelPath = modLoc("block/lens_barrel/" + type + "/").getPath();

        String barrelPath = modelPath + "barrel";

        // ブロックステートを用意
        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, true).with(LensBarrelBlock.BOTTOM, true)
            .modelForState().modelFile(models().getExistingFile(modLoc(barrelPath + "_single"))).addModel();

        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, true).with(LensBarrelBlock.BOTTOM, false)
            .modelForState().modelFile(models().getExistingFile(modLoc(barrelPath + "_top"))).addModel();

        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, false).with(LensBarrelBlock.BOTTOM, false)
            .modelForState().modelFile(models().getExistingFile(modLoc(barrelPath + "_middle"))).addModel();

        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, false).with(LensBarrelBlock.BOTTOM, true)
            .modelForState().modelFile(models().getExistingFile(modLoc(barrelPath + "_bottom"))).addModel();

        // アイテムモデルを用意
        simpleBlockItem(blockRegistryObject.get(), models().getExistingFile(modLoc(barrelPath + "_single")));
    }

    private void litableBlockWithItem(RegistryObject<? extends Block> blockRegistryObject, boolean itemModelLit){
        //モデルパスを用意
        String modelName = blockRegistryObject.getId().getPath();
        String modelPathLit ="block/" + modelName + "/lit";
        String modelPathUnlit = "block/" + modelName + "/unlit";

        //ブロックステートの設定
        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(BlockStateProperties.LIT,true)
            .modelForState().modelFile(models().getExistingFile(modLoc(modelPathLit))).addModel()

            .partialState().with(BlockStateProperties.LIT,false)
            .modelForState().modelFile(models().getExistingFile(modLoc(modelPathUnlit))).addModel();

        //アイテムの設定
        simpleBlockItem(blockRegistryObject.get(),models().getExistingFile(modLoc(itemModelLit ? modelPathLit : modelPathUnlit)));
    }
    private void litableHorizontalRotationalBlockWithItem(RegistryObject<? extends Block> registryObject, RegistryObject<Block> baseBlock){

    }

    private void writtenBoardBlock(RegistryObject<? extends Block> blockRegistryObject){
        WrittenBoardDrawType[] types = WrittenBoardDrawType.values();
        for (WrittenBoardDrawType writtenBoardDrawType : types) {
            // モデルを置くパスを用意
            ResourceLocation modelLoc =
                modLoc("block/"+ blockRegistryObject.getId().getPath() +"/"+ writtenBoardDrawType.getSerializedName());
            // シンボル名
            String symbolName = writtenBoardDrawType.getSerializedName();
            // 大きいシンボルは代用
            if(WrittenBoardDrawType.isLargeNode(writtenBoardDrawType)){
                //continue;
                symbolName = WrittenBoardDrawType.BLANK_CIRCLE.getSerializedName();
            }
            // テクスチャパスを用意
            ResourceLocation textureLoc =
                modLoc("block/written_board/"+ symbolName);

            // モデルを生成
            models().withExistingParent(modelLoc.getPath(), modLoc("block/written_board"))
                .texture("layer", textureLoc.getPath()).renderType("cutout");

            // ブロックステートを生成
            getVariantBuilder(blockRegistryObject.get())
                .partialState().with(AbstractWrittenBoardBlock.CIRCLE_TYPE, writtenBoardDrawType)
                .modelForState().modelFile(models().getExistingFile(modelLoc)).addModel();
        }
    }

    private void largeWrittenBoardBlock(RegistryObject<? extends Block> blockRegistryObject){
        WrittenBoardDrawType[] types = WrittenBoardDrawType.values();
        for (WrittenBoardDrawType writtenBoardDrawType : types) {
            // モデルを置くパスを用意
            ResourceLocation modelLoc =
                modLoc("block/"+ blockRegistryObject.getId().getPath() +"/"+ writtenBoardDrawType.getSerializedName());
            // シンボル名
            String symbolName = blockRegistryObject.getId().getPath();
            String blockName = "large_written_board";
            // 大きいシンボル以外は代用
            if(!WrittenBoardDrawType.isLargeNode(writtenBoardDrawType)){
                //continue;
                symbolName = WrittenBoardDrawType.BLANK_CIRCLE.getSerializedName();
                blockName = "written_board";
            }
            // テクスチャパスを用意
            ResourceLocation textureLoc =
                modLoc("block/written_board/"+ symbolName);

            // モデルを生成
            models().withExistingParent(modelLoc.getPath(), modLoc("block/" + blockName))
                .texture("layer", textureLoc.getPath()).renderType("cutout");

            // ブロックステートを生成
            getVariantBuilder(blockRegistryObject.get())
                .partialState().with(AbstractWrittenBoardBlock.CIRCLE_TYPE, writtenBoardDrawType)
                .modelForState().modelFile(models().getExistingFile(modelLoc)).addModel();
        }
        // アイテムを生成
        ResourceLocation itemLoc = modLoc("item/"+ blockRegistryObject.getId().getPath());
        models().withExistingParent(itemLoc.getPath(), modLoc("item/large_circle_board"))
            .texture("3", modLoc("block/written_board/"+ blockRegistryObject.getId().getPath()).getPath());
    }

    private void fluidBlock(FluidRegistry.FluidEntry fluidEntryObject, String renderType) {
        // パスを用意
        String modelName = fluidEntryObject.still().getId().getPath();
        String texPathStill = "block/fluid/" + modelName + "_still";
        // モデルを生成
        ModelFile stillModel = models().withExistingParent("block/" + modelName, "block/water")
            .texture("particle", texPathStill).renderType(renderType);
        simpleBlock(fluidEntryObject.block().get(), stillModel);
    }
}