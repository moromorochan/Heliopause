package com.moromoro.heliopause.datagen;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.EnumProperty.SiderostatTopState;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.block.SiderostatTopBlock;
import com.moromoro.heliopause.block.AbstractWrittenBoardBlock;
import com.moromoro.heliopause.EnumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.FluidRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class HEPBlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {
    public HEPBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Heliopause.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(BlockRegistry.ALCHEMY_BIRON_BLOCK);
        blockWithItem(BlockRegistry.GLOWSTONE_ALLOY_BLOCK);
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

        lensBarrelBlockWithItem(BlockRegistry.WOODEN_LENS_BARREL_BLOCK, "barrel","wooden");
        lensBarrelBlockWithItem(BlockRegistry.WOODEN_MAIN_MIRROR_BLOCK, "main","wooden");
        lensBarrelBlockWithItem(BlockRegistry.WOODEN_SECOND_MIRROR_BLOCK, "secondary","wooden");

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

    private void lensBarrelBlockWithItem(RegistryObject<? extends LensBarrelBlock> blockRegistryObject, String part, String type){
        // パスを用意
        ResourceLocation modelPath = modLoc("block/lens_barrel/" + type + "_" + part);
        ResourceLocation sidePath = modLoc("block/lens_barrel/" + type + "/side");

        // モデルを生成
        models().withExistingParent(modelPath + "_top", modelPath + "_single")
            .texture("side", sidePath.getPath() + "_top").renderType("cutout");
        models().withExistingParent(modelPath + "_middle", modelPath + "_single")
            .texture("side", sidePath + "_middle").renderType("cutout");
        models().withExistingParent(modelPath + "_bottom", modelPath + "_single")
            .texture("side", sidePath + "_bottom").renderType("cutout");

        // ブロックステートを用意
        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, true).with(LensBarrelBlock.BOTTOM, true)
            .modelForState().modelFile(models().getExistingFile(modLoc(modelPath.getPath() + "_single"))).addModel();

        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, true).with(LensBarrelBlock.BOTTOM, false)
            .modelForState().modelFile(models().getExistingFile(modLoc(modelPath.getPath() + "_top"))).addModel();

        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, false).with(LensBarrelBlock.BOTTOM, false)
            .modelForState().modelFile(models().getExistingFile(modLoc(modelPath.getPath() + "_middle"))).addModel();

        getVariantBuilder(blockRegistryObject.get())
            .partialState().with(LensBarrelBlock.TOP, false).with(LensBarrelBlock.BOTTOM, true)
            .modelForState().modelFile(models().getExistingFile(modLoc(modelPath.getPath() + "_bottom"))).addModel();

        // アイテムモデルを用意
        simpleBlockItem(blockRegistryObject.get(), models().getExistingFile(modLoc(modelPath.getPath() + "_single")));
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