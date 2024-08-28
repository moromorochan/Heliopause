package com.moromoro.heliopause.item;

import com.moromoro.ConfigHolder;
import com.moromoro.heliopause.registry.KeyMapRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FluidBottle extends Item implements IFluidHandlerItem, IhasBlockHoverTexts {

    private static final String FLUID_NBT_KEY = "FluidStack";
    public static final String COLOR_NBT_KEY = "color";
    public static final String MODEL_NBT_KEY = "CustomModelData";
    protected FluidTank mainTank;
    protected int USE_AMOUNT;

    public FluidBottle(Properties properties, int capacity,int useAmount) {
        super(properties);
        this.mainTank = new FluidTank(capacity);
        USE_AMOUNT = useAmount;
    }

    //液体の色をconfigまたはテクスチャから取得
    private int getFluidColor(FluidStack fluidStack){
        //液体のidを取得
        String fluidName = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString();
        if(ConfigHolder.FLUID_COLORS.containsKey(fluidName)){
            return ConfigHolder.FLUID_COLORS.get(fluidName).get();
        }
        //configに無ければ、テクスチャから生成
        //液体の種類を取り出す
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        //(とどまる)液体テクスチャの取得
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(fluidTypeExtensions.getStillTexture(fluidStack));

        int color = sprite.getPixelRGBA(0, 7, 7);
        //float alpha = (color >> 24 & 255) / 255f;
        //int blue = (color >> 16 & 255);
        //int green = (color >> 8 & 255);
        //int red = (color & 255);
        //int combinedColor = (red << 16) | (green << 8) | blue;
        int combinedColor = FastColor.ARGB32.color(
                255,
                FastColor.ABGR32.red(color),
                FastColor.ABGR32.green(color),
                FastColor.ABGR32.blue(color)
        );
        return combinedColor;
    }

    //アイテムの色を取得
    private int getNbtColor(ItemStack stack)
    {
        //nbtがないなら、紫色を返す
        if (!stack.hasTag()){
            return 0xFF00FF;
        }
        CompoundTag nbt = stack.getTag();
        //nbtに色情報がないなら、紫色(0xFF00FF)を返す
        if(!nbt.contains(COLOR_NBT_KEY)){
            return 0xFF00FF;
        }
        return nbt.getInt(FLUID_NBT_KEY);
    }

    //アイテムに色を適用
    private void setNbtColor(ItemStack stack, int color)
    {
        //NBTを取得(なければ生成)
        CompoundTag nbt = stack.getOrCreateTag();
        //色を保存
        nbt.putInt(COLOR_NBT_KEY,color);
    }

    //カスタムモデルデータ値を液体から用意する
    private int getFluidCustomModelVal(float fluidPercentage) {
        if (fluidPercentage == 0.0f) {
            return 0;
        } else if (fluidPercentage < 0.20f) {
            return 1;
        } else if (fluidPercentage < 0.40f) {
            return 2;
        } else if (fluidPercentage < 0.60f) {
            return 3;
        } else if (fluidPercentage < 0.80f) {
            return 4;
        } else if (fluidPercentage < 1.00f) {
            return 5;
        } else {
            return 6;
        }
    }
    //光る液体の見た目を使う場合のオプション
    private int getFluidCustomModelVal(float fluidPercentage,boolean bright) {
        if(bright)
        {
            if (fluidPercentage == 0.0f) {
                return 10;
            } else if (fluidPercentage < 0.20f) {
                return 11;
            } else if (fluidPercentage < 0.40f) {
                return 12;
            } else if (fluidPercentage < 0.60f) {
                return 13;
            } else if (fluidPercentage < 0.80f) {
                return 14;
            } else if (fluidPercentage < 1.00f) {
                return 15;
            } else {
                return 16;
            }
        }
        else
        {
            if (fluidPercentage == 0.0f) {
                return 0;
            } else if (fluidPercentage < 0.20f) {
                return 1;
            } else if (fluidPercentage < 0.40f) {
                return 2;
            } else if (fluidPercentage < 0.60f) {
                return 3;
            } else if (fluidPercentage < 0.80f) {
                return 4;
            } else if (fluidPercentage < 1.00f) {
                return 5;
            } else {
                return 6;
            }
        }
    }

    //カスタムモデルデータ値を用意
    private void setCustomModelDataValue(ItemStack stack,FluidStack fluidStack,int tankCapacity){
        //カスタムモデルデータ値を変更
        stack.getOrCreateTag().putInt(
                MODEL_NBT_KEY,
                getFluidCustomModelVal(
                        (float) fluidStack.getAmount() / tankCapacity,
                        (fluidStack.getFluid().getFluidType().getLightLevel() >= 7)
                )
        );
    }

    //nbtから液体情報を取得
    private FluidStack getNbtFluid(ItemStack stack){
        //nbtがないなら、空状態を返す
        if (!stack.hasTag()){
            return FluidStack.EMPTY;
        }
        CompoundTag nbt = stack.getTag();
        //nbtにタンク情報がないなら、空状態を返す
        if(!nbt.contains(FLUID_NBT_KEY)){
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(nbt.getCompound(FLUID_NBT_KEY));
    }

    //nbtに液体情報を書き込み
    private void setNbtFluid(ItemStack stack, FluidStack fluid) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.put(FLUID_NBT_KEY,fluid.writeToNBT(new CompoundTag()));
    }

    //瓶の名前を用意
    private void setBottleName(ItemStack itemStack, FluidStack fluidStack) {
        //カスタムされた名称がある場合はスキップ
        if(itemStack.hasCustomHoverName()){return;}

        // 液体の翻訳名を取得
        String fluidName = fluidStack.getTranslationKey();
        // 瓶の名前を設定
        String bottleName = getDescriptionId(itemStack)+".filled";
        //スタイルを設定
        Style style = Style.EMPTY.withItalic(false);
        // 翻訳キーを設定
        itemStack.setHoverName(Component.translatable(bottleName,Component.translatable(fluidName)).withStyle(style));
    }

    //ツールチップを用意
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(itemStack, level, tooltip, flag);

        //数値を取得
        int fluidAmount = getNbtFluid(itemStack).getAmount();
        int tankCapacity = this.mainTank.getCapacity();
        // 液体の量を表示
        tooltip.add(Component.translatable("item.heliopause.bottle.tooltip.amount", fluidAmount,tankCapacity));
    }
    //ツールチップをクロスヘアの右側に表示
    public @NotNull List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, BlockPos pos){
        List<Component> tooltip = new ArrayList<>();
        Minecraft instance = Minecraft.getInstance();
        BlockEntity blockEntity = clientLevel.getBlockEntity(pos);
        if(blockEntity==null){return tooltip;}
        if(blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()){
            //操作キーを取得
            Component useKey = instance.options.keyUse.getTranslatedKeyMessage();
            Component drainKey = KeyMapRegistry.BOTTLE_DRAIN.getKeyMapping().getTranslatedKeyMessage();
        if (itemStack.getCount() == 1) {
            //操作キーを押している間は行を反転
            if(!KeyMapRegistry.BOTTLE_DRAIN.isPressed()){
                tooltip.add(Component.literal("[").append(useKey).append("] :"));
                tooltip.add(Component.translatable("item.heliopause.bottle.tooltip.description1"));
                tooltip.add(Component.literal("[").append(drainKey).append(" + ").append(useKey).append("] :").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.heliopause.bottle.tooltip.description2").withStyle(ChatFormatting.GRAY));
            }else{
                tooltip.add(Component.literal("[").append(drainKey).append(" + ").append(useKey).append("] :"));
                tooltip.add(Component.translatable("item.heliopause.bottle.tooltip.description2"));
                tooltip.add(Component.literal("[").append(useKey).append("] :").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.heliopause.bottle.tooltip.description1").withStyle(ChatFormatting.GRAY));
            }
        } else {
            //シフトを押している間は行を反転
            if(!KeyMapRegistry.BOTTLE_DRAIN.isPressed()){
                tooltip.add(Component.literal("[").append(useKey).append("] :"));
                tooltip.add(Component.translatable("item.heliopause.bottleStack.tooltip.description1"));
                tooltip.add(Component.literal("[").append(drainKey).append(" + ").append(useKey).append("] :").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.heliopause.bottleStack.tooltip.description2").withStyle(ChatFormatting.GRAY));
            }else{
                tooltip.add(Component.literal("[").append(drainKey).append(" + ").append(useKey).append("] :"));
                tooltip.add(Component.translatable("item.heliopause.bottleStack.tooltip.description2"));
                tooltip.add(Component.literal("[").append(useKey).append("] :").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("item.heliopause.bottleStack.tooltip.description1").withStyle(ChatFormatting.GRAY));
            }
        }
        }
        return tooltip;
    }

    //液体の移動
    private FluidStack transferFluid(IFluidHandler fillStack,IFluidHandler drainStack, int maxTransfer){
        // fillStackにどれだけ流し入れられるか確認 0なら動作を終わる
        int fillAllowance = fillStack.fill(drainStack.drain(maxTransfer,FluidAction.SIMULATE),FluidAction.SIMULATE);
        if (fillAllowance > 0) {
            // drainStackから液体を取り出す
            FluidStack drainAllowance = drainStack.drain(fillAllowance,FluidAction.EXECUTE);
            if (!drainAllowance.isEmpty()) {
                // fillStackの液体を増やす
                fillStack.fill(drainAllowance, FluidAction.EXECUTE);
            }
            return drainAllowance;
        }
        return FluidStack.EMPTY;
    }

    //アイテムを渡す 渡せないならドロップ
    private void addOrDrop(Player player, Level level, BlockPos pos, ItemStack resultItem) {
        boolean addSucceed = player.addItem(resultItem);
        if (!addSucceed) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), resultItem);
            level.addFreshEntity(itemEntity);
        }
    }

    //液体効果音を再生
    private void playFluidSound(Level level, BlockPos pos, Fluid fluid){
        // 液体の効果音を再生
        if(fluid.getPickupSound().isPresent())
        {
            level.playSound(null, pos, fluid.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }else{ // 液体に効果音設定が無い場合、代わりに水の音で代用
            level.playSound(null,pos, Fluids.WATER.getPickupSound().get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        // 瓶の効果音を再生
        level.playSound(null,pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    //液体の出し入れ
    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //干渉するブロックエンティティのデータを取得
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);
        if(blockEntity == null){return InteractionResult.PASS;}

        //ブロックエンティティ側が対応しているか確認
        if (blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            IFluidHandler blockFluidHandler =
                    blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER)
                    .orElseThrow(() -> new RuntimeException("blockEntityCapability is null. pos:"+ pos));
            //その他のデータを取得
            Player player = context.getPlayer();
            //プレイヤー以外の操作はパス
            if(player==null){return InteractionResult.PASS;}
            //ワールドとアイテムを取得
            Level level = context.getLevel();
            ItemStack heldItem = player.getItemInHand(context.getHand());

            //nbtを取り出す
            mainTank.setFluid(getNbtFluid(heldItem));
            //アイテムの数を取得
            final int itemCount = heldItem.getCount();
            //アイテムがひとつのときは、USE_AMOUNTずつ出し入れ
            if (itemCount==1)
            {
                if (KeyMapRegistry.BOTTLE_DRAIN.isPressed()) {
                    // シフト右クリック: アイテムからブロックへ移す
                    FluidStack transferred = transferFluid(blockFluidHandler,this,USE_AMOUNT);
                    if (!transferred.isEmpty()) {
                        // 効果音を再生
                        playFluidSound(level,pos, transferred.getFluid());
                        //ブロックエンティティの更新を保存
                        blockEntity.setChanged();
                        //容量が0になった場合、nbtを削除
                        if(this.mainTank.isEmpty()){
                            ItemStack resultItem = new ItemStack(heldItem.getItem());
                            resultItem.setTag(null);
                            player.setItemInHand(context.getHand(),resultItem);
                        }else{
                            //アイテムのnbtを更新
                            setNbtFluid(heldItem, this.mainTank.getFluidInTank(0));
                            setCustomModelDataValue(heldItem,this.mainTank.getFluid(),this.mainTank.getCapacity());
                        }
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    // 右クリック: ブロックからアイテムへ移す
                    FluidStack transferred = transferFluid(this,blockFluidHandler,USE_AMOUNT);
                    if (!transferred.isEmpty()) {
                        // 効果音を再生
                        playFluidSound(level,pos, transferred.getFluid());
                        //ブロックエンティティの更新を保存
                        blockEntity.setChanged();
                        //アイテムのnbtを更新
                        setNbtFluid(heldItem, this.mainTank.getFluidInTank(0));
                        setBottleName(heldItem,this.mainTank.getFluidInTank(0));
                        setNbtColor(heldItem, getFluidColor(this.mainTank.getFluidInTank(0)));
                        setCustomModelDataValue(heldItem,this.mainTank.getFluid(),this.mainTank.getCapacity());

                        return InteractionResult.SUCCESS;
                    }
                }
            }
            else//アイテムがスタックされているときは、一度に移せるだけ移す
            {
                //スタック内の液体をいったん統合
                FluidStack wholeStack = new FluidStack(this.mainTank.getFluid().getFluid(), itemCount * this.mainTank.getFluidAmount());
                FluidTank wholeTank = new FluidTank(itemCount * mainTank.getCapacity());
                wholeTank.setFluid(wholeStack);

                if(KeyMapRegistry.BOTTLE_DRAIN.isPressed()){
                    // シフト右クリック: アイテムスタックからブロックへ移す
                    FluidStack transferred = transferFluid(blockFluidHandler,wholeTank, wholeTank.getCapacity());
                    if(!transferred.isEmpty()){
                        // 効果音を再生
                        playFluidSound(level,pos, transferred.getFluid());
                        //ブロックエンティティの更新を保存
                        blockEntity.setChanged();

                        //transferredがアイテム幾つ分か計算
                        //一つ一つのアイテムの操作可能量
                        int itemFluidTransferAllowance = this.mainTank.getFluidAmount();
                        //完全に移送しうるアイテムの数
                        int fullTransferredItemCount = transferred.getAmount()/itemFluidTransferAllowance;
                        //端数の液体の量
                        int fractionalTransferredFluidAmount = transferred.getAmount() % itemFluidTransferAllowance;

                        //空瓶を用意
                        ItemStack resultItem = new ItemStack(heldItem.getItem(),fullTransferredItemCount);

                        //手持ちを全部消費する場合、操作後のスタックに置き換え
                        if(
                                itemCount == fullTransferredItemCount || //アイテムを全部完全に移送可能か あるいは
                                (itemCount == fullTransferredItemCount-1 && fractionalTransferredFluidAmount>0)//ひとつだけ端数で、ほかは完全に移送可能な場合
                        ){
                            player.setItemInHand(context.getHand(),resultItem);
                        }else
                        {
                            //手持ちを消費
                            heldItem.shrink(fullTransferredItemCount);
                            //操作後のスタックを渡す
                            addOrDrop(player,level,pos,resultItem);
                        }
                        //端数がある場合アイテムを渡す
                        if(fractionalTransferredFluidAmount>0){
                            //手持ちを消費
                            heldItem.shrink(1);
                            //端数の液体スタックを用意
                            FluidStack fractionalFluid = new FluidStack(transferred.getFluid(),fractionalTransferredFluidAmount);
                            //アイテムを用意
                            ItemStack fractionalItem = new ItemStack(heldItem.getItem());
                            //アイテムのnbtを設定
                            setNbtFluid(fractionalItem, fractionalFluid);
                            setBottleName(fractionalItem, fractionalFluid);
                            setNbtColor(fractionalItem, getFluidColor(fractionalFluid));
                            setCustomModelDataValue(fractionalItem,fractionalFluid,this.mainTank.getCapacity());
                            //渡す
                            addOrDrop(player,level,pos,fractionalItem);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }else{
                    // 右クリック: ブロックからアイテムスタックへ移す
                    FluidStack transferred = transferFluid(wholeTank,blockFluidHandler,blockFluidHandler.getTankCapacity(0));
                    if(!transferred.isEmpty()){
                        // 効果音を再生
                        playFluidSound(level,pos, transferred.getFluid());
                        //ブロックエンティティの更新を保存
                        blockEntity.setChanged();

                        //transferredがアイテム幾つ分か計算
                        //一つ一つのアイテムの操作可能量
                        int itemFluidTransferAllowance = this.mainTank.getCapacity()-this.mainTank.getFluidAmount();
                        //完全に移送しうるアイテムの数
                        int fullTransferredItemCount = transferred.getAmount()/itemFluidTransferAllowance;
                        //端数の液体の量
                        int fractionalTransferredFluidAmount = transferred.getAmount() % itemFluidTransferAllowance;

                        //満タンの液体を用意
                        FluidStack resultFluid = new FluidStack(transferred.getFluid(),this.mainTank.getCapacity());
                        //満タンの瓶を用意
                        ItemStack resultItem = new ItemStack(heldItem.getItem(),fullTransferredItemCount);
                        //アイテムのnbtを設定
                        setNbtFluid(resultItem, resultFluid);
                        setBottleName(resultItem, resultFluid);
                        setNbtColor(resultItem, getFluidColor(resultFluid));
                        setCustomModelDataValue(resultItem,resultFluid,this.mainTank.getCapacity());

                        //手持ちを全部消費する場合、操作後のスタックに置き換え
                        if(
                                itemCount == fullTransferredItemCount || //アイテムを全部完全に移送可能か あるいは
                                (itemCount == fullTransferredItemCount-1 && fractionalTransferredFluidAmount>0)//ひとつだけ端数で、ほかは完全に移送可能な場合
                        ){
                            player.setItemInHand(context.getHand(),resultItem);
                        }else
                        {
                            //手持ちを消費
                            heldItem.shrink(fullTransferredItemCount);
                            //操作後のスタックを渡す
                            addOrDrop(player,level,pos,resultItem);
                        }
                        //端数がある場合アイテムを渡す
                        if(fractionalTransferredFluidAmount>0){
                            //手持ちを消費
                            heldItem.shrink(1);
                            //端数の液体スタックを用意
                            FluidStack fractionalFluid = new FluidStack(transferred.getFluid(),fractionalTransferredFluidAmount);
                            //アイテムを用意
                            ItemStack fractionalItem = new ItemStack(heldItem.getItem());
                            //アイテムのnbtを設定
                            setNbtFluid(fractionalItem, fractionalFluid);
                            setBottleName(fractionalItem, fractionalFluid);
                            setNbtColor(fractionalItem, getFluidColor(fractionalFluid));
                            setCustomModelDataValue(fractionalItem,fractionalFluid,this.mainTank.getCapacity());
                            //渡す
                            addOrDrop(player,level,pos,fractionalItem);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return new ItemStack(this);
    }

    @Override
    public int getTanks() {
        return this.mainTank.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.mainTank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.mainTank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.mainTank.isFluidValid(tank,stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.mainTank.fill(resource,action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return this.mainTank.drain(resource,action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return this.mainTank.drain(maxDrain,action);
    }
}
