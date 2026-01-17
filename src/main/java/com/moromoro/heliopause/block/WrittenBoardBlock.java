package com.moromoro.heliopause.block;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.EnumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import com.moromoro.heliopause.item.CompassItem;
import com.moromoro.heliopause.recipe.MagicCircleAssemblyRecipe;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrittenBoardBlock extends AbstractWrittenBoardBlock{
    public WrittenBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CIRCLE_TYPE, WrittenBoardDrawType.CROSS_CIRCLE));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new WrittenBoardBlockEntity(blockPos, blockState);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if(level.getBlockEntity(blockPos) instanceof AbstractWrittenBoardBlockEntity boardEntity){
            ItemStack useItemStack = player.getUseItem();
            if(boardEntity.isRoot(level)){
                operateRecipe(level, blockPos, useItemStack);
            }
            else if (!(player.getItemInHand(hand).getItem() instanceof CompassItem)) {
                BlockPos rootPos = boardEntity.getRootPos(level, blockPos);
                if(level.getBlockEntity(rootPos) instanceof AbstractWrittenBoardBlockEntity rootBoardEntity){
                    if(rootBoardEntity.operateFromArea(level, blockPos, blockPos.getCenter().add(0,0.5,0), player, hand)){
                        return InteractionResult.sidedSuccess(!level.isClientSide());
                    }
                }
            }
        }
        return super.use(blockState, level, blockPos, player, hand, blockHitResult);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, @NotNull Block block, BlockPos neighbor, boolean update) {
        if (neighbor.equals(pos.above())) {
            BlockState neighborBlockState = level.getBlockState(neighbor);
            if (!neighborBlockState.isAir()) {
                Item triggerItem = neighborBlockState.getBlock().asItem();
                if (!triggerItem.equals(Items.AIR)) {
                    operateRecipe(level, pos, new ItemStack(triggerItem));
                }
            }
        }
        super.neighborChanged(state, level, pos, block, neighbor, update);
    }

    private void operateRecipe(Level level, BlockPos blockPos, ItemStack itemStack) {
        // レシピ確認用コンテナを作成
        Container matchContainer = new SimpleContainer(itemStack);
        // 全レシピ確認
        for (MagicCircleAssemblyRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.MAGIC_CIRCLE_ASSEMBLY.get())) {
            // トリガー確認
            if (!recipe.matches(matchContainer, level)) {
                continue;
            }
            // 陣の構造確認
            if(MagicCircleAssemblyRecipe.matchesAt(level, blockPos, recipe)){
                // 結果ブロックを確認
                Block resultBlock = ForgeRegistries.BLOCKS.getValue(recipe.getResult());
                if(resultBlock!=null){
                    AbstractWrittenBoardBlockEntity.changeCircleBoardBlock(level, blockPos, resultBlock.defaultBlockState());
                }
                // デバッグ用
                if(!level.isClientSide()){
                    Heliopause.LOGGER.debug(recipe.getResult().toString());
                    EntityType.FIREWORK_ROCKET.spawn((ServerLevel) level, blockPos.above(2), MobSpawnType.COMMAND);
                }
                // 最初の一致を適用して終了
                break;
            }
        }
    }

}
