package com.moromoro.heliopause.block;

import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import com.moromoro.heliopause.item.CompassItem;
import com.moromoro.heliopause.recipe.MagicCircleAssemblyRecipe;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
            ItemStack useItemStack = player.getItemInHand(hand);
            if(boardEntity.isRoot(level)){
                if(operateRecipe(level, blockPos, useItemStack)){
                    player.setItemInHand(hand, useItemStack);
                    return InteractionResult.sidedSuccess(!level.isClientSide());
                }
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

    private boolean operateRecipe(Level level, BlockPos blockPos, ItemStack itemStack) {
        
        // レシピ確認用コンテナを作成
        Container matchContainer = new SimpleContainer(itemStack);
        // 全レシピ確認
        for (MagicCircleAssemblyRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.MAGIC_CIRCLE_ASSEMBLY.get())) {
            // トリガー確認
            boolean triggerIsBlock = recipe.getTrigger().isBlock();
            if(triggerIsBlock){
                if(!level.getBlockState(blockPos.above()).getBlock().asItem().equals(itemStack.getItem())){
                    continue;
                }
            }else if (!recipe.matches(matchContainer, level)) {
                continue;
            }
            
            // 陣の構造確認
            List<BlockPos> matchCircles = MagicCircleAssemblyRecipe.matchesAt(level, blockPos, recipe);
            if(matchCircles != null){
                
                // 結果を確認
                MagicCircleAssemblyRecipe.Result result = recipe.getResult();
                if(result == null){
                    continue;
                }
                boolean resultIsBlock = result.isBlock();
                
                // 結果がブロックのとき
                if(resultIsBlock){
                    Block resultBlock = ForgeRegistries.BLOCKS.getValue(result.blockOrItem());
                    if(resultBlock==null){
                        continue;
                    }
                    // 魔方陣の構築なら中心を置き換え
                    if(resultBlock instanceof AbstractWrittenBoardBlock) {
                        AbstractWrittenBoardBlockEntity.changeCircleBoardBlock(level, blockPos, resultBlock.defaultBlockState());
                    }
                    // 魔方陣以外なら
                    else{
                        // ブロックを設置
                        level.setBlock(blockPos.above(), resultBlock.defaultBlockState(), 3);
                        
                        // 陣全体を消す
                        for (BlockPos matchPos : matchCircles) {
                            if(level.getBlockEntity(matchPos) instanceof AbstractWrittenBoardBlockEntity entity){
                                entity.eraseNetwork();
                            }
                        }
                        // 中心を消す
                        AbstractWrittenBoardBlockEntity.changeCircleBoardBlock(level, blockPos, BlockRegistry.BLACKBOARD.get().defaultBlockState());
                    }
                }
                else{
                    Item resultItem = ForgeRegistries.ITEMS.getValue(result.blockOrItem());
                    if(resultItem == null){
                        continue;
                    }
                    // 陣全体を消す
                    for (BlockPos matchPos : matchCircles) {
                        if (level.getBlockEntity(matchPos) instanceof AbstractWrittenBoardBlockEntity entity) {
                            entity.eraseNetwork();
                        }
                    }
                    // 中心を消す
                    AbstractWrittenBoardBlockEntity.changeCircleBoardBlock(level, blockPos, BlockRegistry.BLACKBOARD.get().defaultBlockState());
                    
                    // アイテムをドロップ
                    level.addFreshEntity(new ItemEntity(level, blockPos.getCenter().x(), blockPos.getCenter().y() + 1, blockPos.getCenter().z(),new ItemStack(resultItem)));
                }
                
                // 消費なら
                if(recipe.getTrigger().type().equals("consume")){
                    // 置いたブロックを空気に置き換え
                    if(triggerIsBlock && !resultIsBlock){
                        level.setBlock(blockPos.above(), Blocks.AIR.defaultBlockState(), 3);
                    }
                    // アイテム消費
                    else{
                        itemStack.shrink(1);
                    }
                }
                
                // 効果音を再生
                level.playSound(null,
                    blockPos.getCenter().x(), blockPos.getCenter().y() + 1, blockPos.getCenter().z(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
                
                // パーティクル生成
                if(level instanceof ServerLevel serverLevel){
                    for (BlockPos matchPos : matchCircles) {
                        serverLevel.sendParticles(
                            ParticleTypes.CLOUD,
                            matchPos.getCenter().x(), matchPos.getCenter().y() + 0.7, matchPos.getCenter().z(),
                            1,
                            0,0,0,
                            0
                        );
                    }
                }
                
                // デバッグ用
                /*if(!level.isClientSide()){
                    Heliopause.LOGGER.debug(recipe.getResult().toString());
                    EntityType.FIREWORK_ROCKET.spawn((ServerLevel) level, blockPos.above(2), MobSpawnType.COMMAND);
                }*/
                // 最初の一致を適用して終了
                return true;
            }
        }
        
         return false;
    }

}
