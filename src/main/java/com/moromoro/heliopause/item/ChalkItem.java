package com.moromoro.heliopause.item;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.screen.CircleSelectMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChalkItem extends Item implements IhasHoverTexts {

    /*private static final String CORNER_NBT_KEY = "CornerPos";
    private static final int SIDE_LENGTH_LIMIT = 16;*/

    private CircleSelectMenu menu;

    public ChalkItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, HitResult hitResult) {
        List<Component> tooltip = new ArrayList<>();
        Minecraft instance = Minecraft.getInstance();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState blockState = clientLevel.getBlockState(pos);
            if(blockState.isAir()){return tooltip;}
            //操作キーを取得
            Component useKey = instance.options.keyUse.getTranslatedKeyMessage();
            //tooltip.add(Component.literal("[").append(useKey).append("] :"));
            tooltip.add(Component.literal("[").append(Component.translatable("item.heliopause.chalk.tooltip.description1",useKey)).append("] :"));
            tooltip.add(Component.translatable("item.heliopause.chalk.tooltip.description2"));
        }
        return tooltip;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int p_41431_) {
        super.onUseTick(level, entity, itemStack, p_41431_);
    }

    //ブロックをクリックしたとき
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //レベルを取得
        Level level = context.getLevel();
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState blockState = level.getBlockState(pos);

        if(!stack.is(this)){
            return InteractionResult.PASS;
        }
        if(blockState.is(BlockRegistry.BLACKBOARD.get())){
            level.setBlock(pos,BlockRegistry.WRITTEN_BOARD.get().defaultBlockState(), 0);
        } else if (blockState.is(BlockRegistry.WRITTEN_BOARD.get())) {

        }

        //最初の角を選択
        /*if(!hasCornerPos(stack)){
            setCornerPos(stack,pos);
        }*/
        //次の角を選択
        /*else{
            //最初の角をnbtから取得
            BlockPos lastPos = getNbtCorner(stack);
            //座標の距離を確認して、遠すぎたらキャンセル
            if(checkSideLength(lastPos,pos)){
                //角を最小と最大の位置に置き換え
                BlockPos firstCorner = new BlockPos(
                    Math.min(pos.getX(),lastPos.getX()),
                    Math.min(pos.getY(),lastPos.getY()),
                    Math.min(pos.getZ(),lastPos.getZ())
                );
                BlockPos secondCorner = new BlockPos(
                    Math.max(pos.getX(),lastPos.getX()),
                    Math.max(pos.getY(),lastPos.getY()),
                    Math.max(pos.getZ(),lastPos.getZ())
                );
                //ブロック取得
                BlockState[][][] selectedBlocks = getSelectedBlocks(level,firstCorner,secondCorner);
                //レシピの確認&実行
                applyRecipe(firstCorner, selectedBlocks, level, direction);
            }
            //保存した座標を削除
            removeCornerPos(stack);
        }*/
        /*
        //縦のブロックを取得(テスト用)
        BlockState[] OverBlocks = new BlockState[8];
        for (int i = 0; i < 8; i++) {
            OverBlocks[i]= level.getBlockState(pos.above(i));
        }
        //レシピのチェック
        Boolean Check = true;

        for (int i = 0; i < 8; i++) {
            switch (i){
                case 0:
                    if(!OverBlocks[i].is(BlockTags.LOGS)){Check = false;}
                    break;
                case 1:
                    if(!OverBlocks[i].is(Blocks.COPPER_BLOCK)){Check = false;}
                    break;
                case 7:
                    if(!OverBlocks[i].is(Blocks.AIR)){Check = false;}
                    break;
                default:
                    if(!OverBlocks[i].is(BlockTags.PLANKS)){Check = false;}
                    break;
            }
            Heliopause.LOGGER.debug(OverBlocks[i].toString());
            Heliopause.LOGGER.debug(Check.toString());
        }
        if(!Check){return InteractionResult.PASS;}
        //置き換える
        level.setBlock(
                pos.above(7),
                BlockRegistry.FLUID_SPREADER_ORB.get().defaultBlockState(),
                0
        );
        for (int i = 0; i < 8; i++) {
            level.destroyBlock(pos.above(i),false);
            if (i == 7) {
                level.setBlock(
                        pos.above(i),
                        BlockRegistry.FLUID_SPREADER_ORB.get().defaultBlockState(),
                        0
                );
            } else {
                level.setBlock(
                        pos.above(i),
                        BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                .setValue(BlockStateProperties.LEVEL, i)
                                .trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction),
                        0
                );
            }
        }
        */
        return InteractionResult.SUCCESS;
        //NetworkHooks.openScreen((ServerPlayer) context.getPlayer(), );
    }

    //選択範囲の長さが限界を越えていないかチェックする
    /*private boolean checkSideLength(@Nullable BlockPos pos1, @Nullable BlockPos pos2) {
        if(pos1==null||pos2==null) return false;
        return (
                    (Math.abs(pos1.getX()-pos2.getX())<=SIDE_LENGTH_LIMIT)
                &&  (Math.abs(pos1.getY()-pos2.getY())<=SIDE_LENGTH_LIMIT)
                &&  (Math.abs(pos1.getZ()-pos2.getZ())<=SIDE_LENGTH_LIMIT)
                );
    }*/

    //レシピを確認・適用する
    private void applyRecipe(BlockPos firstCorner,BlockState[][][] selectedBlocks, Level level, Direction direction) {
        //方角ごとにレシピを検索
        for (int clockWiseCount = 0; clockWiseCount < 4; clockWiseCount++) {
            BlockState[][][] rotatedSelectedBlocks = rotateBlockStateArray(selectedBlocks,clockWiseCount);
            //サイズが合致するレシピを検索
            int sizeX = selectedBlocks.length;
            int sizeY = selectedBlocks[0].length;
            int sizeZ = selectedBlocks[0][0].length;
            Heliopause.LOGGER.debug(String.format("Selected Size: %d, %d, %d",sizeX,sizeY,sizeZ));
            if(sizeX!=1||sizeY!=7||sizeZ!=1) return;
            Heliopause.LOGGER.debug("Size Check Passed!");
            //内容が合致するレシピの検索
            //材料を取得
            Object[][][] recipe =
                    new Object[][][]{
                            {{BlockTags.LOGS}, {Blocks.COPPER_BLOCK}, {BlockTags.PLANKS}, {BlockTags.PLANKS}, {BlockTags.PLANKS}, {BlockTags.PLANKS}, {BlockTags.PLANKS}}
                    };
            //マッチング
            boolean recipeCheck = true;
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    for (int z = 0; z < sizeZ; z++) {
                        Heliopause.LOGGER.debug(recipe[x][y][z].getClass().toString());
                        //条件がブロックのとき
                        if(Block.class.isAssignableFrom(recipe[x][y][z].getClass())){
                            if(!rotatedSelectedBlocks[x][y][z].is((Block) recipe[x][y][z])){
                                Heliopause.LOGGER.debug(String.format("block match denied on %d, %d, %d , block: %s",x,y,z,rotatedSelectedBlocks[x][y][z]));
                                recipeCheck = false;
                                break;
                            }
                        }
                        //条件がブロックタグのとき
                        else if(TagKey.class.isAssignableFrom(recipe[x][y][z].getClass())){
                            if(!rotatedSelectedBlocks[x][y][z].is((TagKey<Block>) recipe[x][y][z])){
                                Heliopause.LOGGER.debug(String.format("tag match denied on %d, %d, %d , block: %s",x,y,z,rotatedSelectedBlocks[x][y][z]));
                                recipeCheck = false;
                                break;
                            }
                        }
                        //条件が通過ではなく、かつ上2つでもないとき
                        else if(recipe[x][y][z] != null){
                            Heliopause.LOGGER.debug(String.format("pass match denied on %d, %d, %d , block: %s",x,y,z,rotatedSelectedBlocks[x][y][z]));
                            recipeCheck = false;
                            break;
                        }
                    }if(!recipeCheck) break;
                }if(!recipeCheck) break;
            }
            if(recipeCheck){
                //結果を取得
                BlockState[][][] resultBlocks =
                    new BlockState[][][]{
                        {
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,0)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                            },
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,1)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                            },
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,2)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                            },
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,3)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                            },
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,4)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                            },
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,5)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                            },
                            {
                                BlockRegistry.FLUID_SPREADER_TOWER.get().defaultBlockState()
                                    .setValue(BlockStateProperties.LEVEL,6)
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH)
                                    .setValue(BooleanProperty.create("top"),true)
                            }
                        }
                    };
                //結果を回転
                BlockState[][][] rotatedResultBlocks = rotateBlockStateArray(resultBlocks,clockWiseCount);
                //設置
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        for (int z = 0; z < sizeZ; z++) {
                            level.destroyBlock(firstCorner.offset(x,y,z),false);
                            level.setBlock(firstCorner.offset(x,y,z),rotatedResultBlocks[x][y][z],0);
                        }
                    }
                }
            }
        }
    }

    //ブロック状態配列の回転 ブロック状態に角度がある場合はそれも回転
    private BlockState[][][] rotateBlockStateArray(BlockState[][][] selectedBlocks, int clockWiseCount) {
//        for (int j = 0; j < clockWiseCount%4; j++) {
//
//        }
        return selectedBlocks;
    }

    //選択範囲内のブロック配列を取得する
    private BlockState[][][] getSelectedBlocks(Level level, BlockPos pos1, BlockPos pos2) {

        //選択範囲の大きさを取得
        int selectedLengthX = pos2.getX() - pos1.getX() +1;
        int selectedLengthY = pos2.getY() - pos1.getY() +1;
        int selectedLengthZ = pos2.getZ() - pos1.getZ() +1;

        //ブロック状態を格納する配列を宣言
        BlockState[][][] stateArray = new BlockState[selectedLengthX][selectedLengthY][selectedLengthZ];

        //ブロック状態を取得する
        for (int x = 0; x < selectedLengthX; x++) {
            for (int y = 0; y < selectedLengthY; y++) {
                for (int z = 0; z < selectedLengthZ; z++) {
                    stateArray[x][y][z] = level.getBlockState(pos1.offset(x,y,z));
                }
            }
        }
        Heliopause.LOGGER.debug(String.join("First:",pos1.toString()));
        Heliopause.LOGGER.debug(String.join("Second:",pos2.toString()));
        Heliopause.LOGGER.debug(String.join("Selected:", Arrays.deepToString(stateArray)));
        return stateArray;
    }

    /*/nbtから座標を取得する
    private BlockPos getNbtCorner(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if(nbt!=null) {
            int[] posArray = nbt.getIntArray(CORNER_NBT_KEY);
            if(posArray.length!=3) return null;
            return new BlockPos(posArray[0],posArray[1],posArray[2]);
        }
        return null;
    }

    //選択した座標をnbtに保存する
    private void setCornerPos(ItemStack stack, BlockPos pos) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putIntArray(CORNER_NBT_KEY, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    //nbtから座標を削除する
    private void removeCornerPos(ItemStack stack){
        CompoundTag nbt = stack.getTag();
        if(nbt!=null) {
            nbt.remove(CORNER_NBT_KEY);
        }
    }

    //nbtに選択した座標があるか確認する
    private boolean hasCornerPos(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if(nbt!=null) {
            return (nbt.contains(CORNER_NBT_KEY));
        }
        return false;
    }
     */
}
