package com.moromoro.heliopause.block;

import com.moromoro.heliopause.recipe.CampfireAlchemyRecipe;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class AlchemyCampfireBlock extends CampfireBlock {
    //protected int process_tick = 0;
    public AlchemyCampfireBlock(boolean spawnParticles, int fireDamage, Properties properties) {
        super(spawnParticles, fireDamage, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState neighborState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos neighborPos) {
        if(blockState.getValue(LIT) && direction == Direction.UP){
            Block aboveBlock = neighborState.getBlock();

            SimpleContainer container = new SimpleContainer(new ItemStack(aboveBlock));
            RecipeManager recipeManager = ((Level) levelAccessor).getRecipeManager();

            Optional<CampfireAlchemyRecipe> recipeOpt = recipeManager.getRecipeFor(RecipeTypeRegistry.CAMPFIRE_ALCHEMY.get(), container, (Level) levelAccessor);
            recipeOpt.ifPresent(recipe -> {
                Level level = (Level) levelAccessor;
                level.destroyBlock(neighborPos, false);
                level.setBlock(neighborPos, recipe.getResult().defaultBlockState(), 3);

                //レシピからNBTを取得して適用
                CompoundTag nbt = recipe.getResultNBT();
                BlockEntity entity = levelAccessor.getBlockEntity(neighborPos);
                if (entity != null) {
                    entity.load(nbt);
                    entity.setChanged();
                }
            });
        }
        return super.updateShape(blockState, direction, neighborState, levelAccessor, blockPos, neighborPos);
    }
}
