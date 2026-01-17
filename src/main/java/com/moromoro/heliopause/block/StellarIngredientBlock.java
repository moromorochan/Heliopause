package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.StellarIngredientBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StellarIngredientBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(3, 3, 3, 13, 13, 13);

    public StellarIngredientBlock(Properties properties) {
        super(properties);
    }


    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new StellarIngredientBlockEntity(blockPos, blockState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if(level.getBlockEntity(pos) instanceof StellarIngredientBlockEntity entity){
            LazyOptional<IItemHandler> itemCapability = entity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if(itemCapability.isPresent()){
                IItemHandler itemHandler = itemCapability.orElseThrow(IllegalStateException::new);
                ItemStack itemStack = itemHandler.getStackInSlot(0);
                if(!itemStack.isEmpty()){
                    return itemStack;
                }
            }
            LazyOptional<IFluidHandler> fluidCapability = entity.getCapability(ForgeCapabilities.FLUID_HANDLER);
            if(fluidCapability.isPresent()){
                IFluidHandler fluidHandler = fluidCapability.orElseThrow(IllegalStateException::new);
                FluidStack fluidStack = fluidHandler.getFluidInTank(0);
                if(!fluidStack.isEmpty()){
                    ItemStack bucketStack = fluidStack.getFluid().getBucket().getDefaultInstance();
                    if(!bucketStack.isEmpty()){
                        return bucketStack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(level.getBlockEntity(pos) instanceof StellarIngredientBlockEntity entity){
            entity.drops();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //ブロックエンティティを取得
        if(!(level.getBlockEntity(blockPos) instanceof StellarIngredientBlockEntity stellarEntity)){
            return InteractionResult.CONSUME;
        }
        // 入力を取得
        ItemStack inputStack = player.getItemInHand(hand);
        // 液体取り出し
        inputStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(itemFluidCap -> {
            stellarEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(entityFluidCap -> {
                FluidStack available = entityFluidCap.getFluidInTank(0);
                if (!available.isEmpty()) {
                    // 入れられる量確認
                    int canFill = itemFluidCap.fill(available, IFluidHandler.FluidAction.SIMULATE);
                    if (canFill > 0) {
                        // 操作を実行
                        FluidStack toDrain = available.copy();
                        toDrain.setAmount(canFill);
                        entityFluidCap.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                        itemFluidCap.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);

                        // 効果音を再生
                        SoundEvent sound = toDrain.getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                        if(sound!=null){
                            level.playSound(player,
                                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                                sound, SoundSource.BLOCKS, 1,1);
                        }
                    }
                }
            });
        });

        // アイテム取り出し
        stellarEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(entityItemCap -> {

            ItemStack stackInSlot = entityItemCap.getStackInSlot(0);
            if (!stackInSlot.isEmpty()) {
                // 操作を実行
                ItemStack extracted = entityItemCap.extractItem(0, stackInSlot.getCount(), false);
                if (!extracted.isEmpty()) {
                    player.addItem(extracted);
                }
                // 効果音を再生
                level.playSound(player,
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                    SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
            }
        });

        stellarEntity.checkRemove();

        return InteractionResult.sidedSuccess(!level.isClientSide());
    }
}
