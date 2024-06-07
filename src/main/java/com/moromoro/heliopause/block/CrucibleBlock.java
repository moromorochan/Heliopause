package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import com.moromoro.heliopause.blockEntity.OrbBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlock extends BaseEntityBlock {

    public CrucibleBlock(Properties p_49224_){super(p_49224_);}
    public static VoxelShape SHAPE = Shapes.join(
            Block.box(0,3,0,16,16,16),
            Block.box(2,6,2,14,16,14),
            BooleanOp.ONLY_FIRST
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos,state);
    }

    @NotNull
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }
        // 現在地のブロックエンティティを取得
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CrucibleBlockEntity) {
            CrucibleBlockEntity crucibleBlockEntity = (CrucibleBlockEntity) blockEntity;

            // バケツアイテムを持っているなら
            if (heldItem.getItem() instanceof BucketItem) {
                BucketItem bucketItem = (BucketItem) heldItem.getItem();
                if (bucketItem == Items.BUCKET) {
                    //取り出し可能か確認
                    FluidStack drainTest = crucibleBlockEntity.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
                    // 取り出しが成功した場合 プレイヤーに中身入りのバケツを渡す
                    if (drainTest.getAmount()==FluidType.BUCKET_VOLUME) {
                        //取り出しを実行
                        FluidStack drained = crucibleBlockEntity.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                        // クリエイティブかつ同一NBTのバケツがインベントリにある場合追加しない
                        ItemStack filledBucket = new ItemStack(drained.getFluid().getBucket());
                        if(player.isCreative()){
                            // プレイヤーのインベントリ内を走査
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                ItemStack stackInSlot = player.getInventory().getItem(i);
                                if (ItemStack.isSameItemSameTags(stackInSlot, filledBucket)) {
                                    // 効果音を再生
                                    level.playSound(null, pos, drained.getFluid().getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                                    //更新
                                    crucibleBlockEntity.setChanged();
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                        if (!player.addItem(filledBucket)) {
                            // プレイヤーのインベントリが一杯なら、バケツをドロップする
                            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), filledBucket);
                            level.addFreshEntity(itemEntity);
                        }
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        // 効果音を再生
                        level.playSound(null, pos, drained.getFluid().getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        //更新
                        crucibleBlockEntity.setChanged();
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    // バケツが中身入りなら
                    Fluid fluid = bucketItem.getFluid();
                    //流し入れ可能か確認
                    int fillTest = crucibleBlockEntity.fill(new FluidStack(fluid, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE);
                    // 流し入れに成功したら、プレイヤーに空のバケツを渡す
                    if (fillTest == FluidType.BUCKET_VOLUME) {
                        //流し入れを実行
                        int filled = crucibleBlockEntity.fill(new FluidStack(fluid, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                        // クリエイティブの場合は入れ替えない
                        if (!player.isCreative()) {
                            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        // 効果音を再生
                        level.playSound(null, pos, fluid.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        //更新
                        crucibleBlockEntity.setChanged();
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.CONSUME;
    }

}
