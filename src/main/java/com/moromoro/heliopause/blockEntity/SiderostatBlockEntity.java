package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.EnumProperty.SiderostatTopState;
import com.moromoro.heliopause.block.SiderostatBaseBlock;
import com.moromoro.heliopause.block.SiderostatTopBlock;
import com.moromoro.heliopause.recipe.MoonlightPouringRecipe;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import com.moromoro.heliopause.registry.SoundRegistry;
import com.moromoro.heliopause.screen.SiderostatMenu;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class SiderostatBlockEntity extends BlockEntity implements MenuProvider {

    // 表示バウンディングボックス
    private static final AABB RENDER_SHAPE = new AABB(-0.5D, 0, 0,1.5D,2.5D,1);
    // 表示メニュー
    private SiderostatMenu menu;

    // ゼンマイ量 0~180(角度)
    private int springAmount = 180;
    // ゼンマイ巻き状態
    private int springCharge = 0;
    // 角度が合っているか
    private boolean angleSynced = false;
    // 加工の進捗
    private int craftingProgress = 0;
    // 加工の要求
    private int craftingTotalTime = 0;

    // 視野の状況
    public static final int SKY_SLICES = 16;
    public static final double SLICE_ANGLE = 180.0/SKY_SLICES;
    private short canSeeSkies = 0b00000000;
    private static final double[] CHECK_ANGLES = new double[]{
        /*11.25, 33.75, 56.25, 78.75, 101.25, 123.75, 146.25, 168.75*/
        5.625, 16.875, 28.125, 39.375, 50.625, 61.875, 73.125, 84.375,
        95.625, 106.875, 118.125, 129.375, 140.625, 151.875, 163.125, 174.375
    };
    private static final double RAY_MAX_DISTANCE = 255;

    // 内部アイテム(材料・完成品)
    private final ItemStackHandler itemHandler = new ItemStackHandler(2){
        // 内容更新毎にセーブ
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            SiderostatBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // アイテム搬入できるかどうか制御
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            ItemStack existingStack = this.getStackInSlot(slot);
            // 既存のスタックが空、または同種のアイテムでスタックが満杯でない場合
            if (
                existingStack.isEmpty() ||
                    (
                        (existingStack.getItem() == stack.getItem()) &&
                            (existingStack.getCount() < existingStack.getMaxStackSize())
                    )
            ) {
                return super.insertItem(slot, stack, simulate);
            } else {
                // 種類が一致しないか、スタックが満杯の場合
                return stack;
            }
        }
    };

    // 呼び出し時の構築用
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public static final int DATA_ACCESS_LENGTH = 5;
    public static final int SPRING_AMOUNT_INDEX = 0;
    public static final int ANGLE_SYNCED = 1;
    public static final int CRAFTING_PROGRESS_INDEX = 2;
    public static final int CRAFTING_TOTAL_TIME_INDEX = 3;
    public static final int CAN_SEE_SKIES_INDEX = 4;
    //データ格納用
    //private int springAmount = 0;
    //private int craftingDuration = 0;
    //private int craftingTime = 0;
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index){
                case SPRING_AMOUNT_INDEX -> SiderostatBlockEntity.this.springAmount;
                case ANGLE_SYNCED -> SiderostatBlockEntity.this.angleSynced? 1:0;
                case CRAFTING_PROGRESS_INDEX -> SiderostatBlockEntity.this.craftingProgress;
                case CRAFTING_TOTAL_TIME_INDEX -> SiderostatBlockEntity.this.craftingTotalTime;
                case CAN_SEE_SKIES_INDEX -> SiderostatBlockEntity.this.canSeeSkies;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index){
                case SPRING_AMOUNT_INDEX -> SiderostatBlockEntity.this.springAmount = value;
                case ANGLE_SYNCED -> SiderostatBlockEntity.this.angleSynced = (value == 1);
                case CRAFTING_PROGRESS_INDEX -> SiderostatBlockEntity.this.craftingProgress = value;
                case CRAFTING_TOTAL_TIME_INDEX -> SiderostatBlockEntity.this.craftingTotalTime = value;
                case CAN_SEE_SKIES_INDEX -> SiderostatBlockEntity.this.canSeeSkies = (short) value;
            };
        }

        @Override
        public int getCount() {
            return DATA_ACCESS_LENGTH;
        }
    };// 0:現在指している時刻 1:角度が合っているか 2:クラフトの要求長さ 3:クラフトの残り 4:視界状況

    public SiderostatBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.SIDEROSTAT_BE.get(), blockPos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RENDER_SHAPE.move(this.getBlockPos());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if(side == null){
                return lazyItemHandler.cast();
            }
            if(side == Direction.UP){
                return super.getCapability(cap, side);
            }
            if(side == Direction.DOWN){
                final IntArrayList downSlot = IntArrayList.of(0);
                return lazyItemHandler.lazyMap(map -> createFilteredItemHandler(map, downSlot, false, true)).cast();
            }
            if(side.getAxis().isHorizontal()){
                final IntArrayList sideSlot = IntArrayList.of(1);
                return lazyItemHandler.lazyMap(map -> createFilteredItemHandler(map, sideSlot, true, true)).cast();
            }
        }
        return super.getCapability(cap, side);
    }

    private @NotNull IItemHandler createFilteredItemHandler(IItemHandler itemHandler, IntArrayList allowedSlots, boolean allowInsert, boolean allowExtract) {
        // 重複を無くす
        List<Integer> whiteList = new IntArrayList(new LinkedHashSet<>(allowedSlots));

        return new IItemHandler() {
            @Override
            public int getSlots() {
                return whiteList.size();
            }

            // 外部2内部
            private int toInternal(int externalSlot) {
                if (externalSlot < 0 || externalSlot >= whiteList.size()) {
                    return -1;
                }
                return whiteList.get(externalSlot);
            }

            // 内部2外部
            private int toExternal(int internalSlot) {
                return whiteList.indexOf(internalSlot);
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                int internal = toInternal(slot);
                if (internal != -1) {
                    return itemHandler.getStackInSlot(internal);
                }
                return ItemStack.EMPTY;
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (allowInsert) {
                    int internal = toInternal(slot);
                    if (internal != -1) {
                        return itemHandler.insertItem(internal, stack, simulate);
                    }
                }
                return stack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (allowExtract) {
                    int internal = toInternal(slot);
                    if (internal != -1) {
                        return itemHandler.extractItem(internal, amount, simulate);
                    }
                }
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                int internal = toInternal(slot);
                if (internal != -1) {
                    return itemHandler.getSlotLimit(internal);
                }
                return 0;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (allowInsert) {
                    int internal = toInternal(slot);
                    if (internal != -1) {
                        return itemHandler.isItemValid(internal, stack);
                    }
                }
                return false;
            }
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("Slot", itemHandler.serializeNBT());
        nbt.putInt("SpringAmount",this.springAmount);
        nbt.putInt("SpringCharge",this.springCharge);
        nbt.putBoolean("AngleSynced",this.angleSynced);
        nbt.putInt("CraftingProgress",this.craftingProgress);
        nbt.putInt("CraftingTotalTime",this.craftingTotalTime);
        nbt.putShort("CanSeeSkies",this.canSeeSkies);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("Slot"));
        springAmount = nbt.getInt("SpringAmount");
        springCharge = nbt.getInt("SpringCharge");
        angleSynced = nbt.getBoolean("AngleSynced");
        craftingProgress = nbt.getInt("CraftingProgress");
        craftingTotalTime = nbt.getInt("CraftingTotalTime");
        this.canSeeSkies = nbt.getShort("CanSeeSkies");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
        this.load(nbt);
    }

    public void drops(){
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.heliopause.siderostat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        menu = new SiderostatMenu(containerId, playerInventory, this, this.data);
        checkCanSeeSkies();
        return menu;
    }

    protected void checkCanSeeSkies() {
        this.canSeeSkies = 0;
        if (level == null) {
            return;
        }
        if(!angleSynced || level.isRaining() || level.isThundering()){
            return;
        }

        for (double checkAngle : CHECK_ANGLES) {
            this.canSeeSkies = checkCanSeeSkySlice(checkAngle, this.canSeeSkies);
        }

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    protected short checkCanSeeSkySlice(double angleDeg, short canSeeSkies){
        if (level == null) {
            return 0;
        }

        // 角度を丸める
        int index = (int)(angleDeg/SLICE_ANGLE);

        if(index<0){
            index = 0;
        }
        if(index >=SKY_SLICES) {
            index = SKY_SLICES - 1;
        }

        // 対象を遮蔽判定に設定
        canSeeSkies = (short) (canSeeSkies & ~(1 << index));

        // 原点（上ブロック中心）
        Vec3 centerPos = worldPosition.above().getCenter();

        double checkRad = Math.toRadians(CHECK_ANGLES[index]);

        // 東西-天頂方向のみ（Z成分は 0）
        double normalX = Math.cos(checkRad);
        double normalY = Math.sin(checkRad);

        Vec3 clipStart = new Vec3(normalX* 0.7, normalY * 0.7, 0).add(centerPos);
        Vec3 clipEnd = new Vec3(normalX * RAY_MAX_DISTANCE, normalY * RAY_MAX_DISTANCE, 0).add(centerPos);

        // ClipContext のブロックモードは COLLIDER（衝突ボックス）を利用
        ClipContext context = new ClipContext(clipStart, clipEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null);
        BlockHitResult result = level.clip(context);

        //Heliopause.LOGGER.debug(level.getBlockState());
        // 遮蔽がないなら
        if (result.getType() == HitResult.Type.MISS) {
            canSeeSkies |= (short) (1 << index);
        }
        return canSeeSkies;
    }

    // ゼンマイ巻く
    public void chargeSpring(){
        if(springAmount > 0 && !angleSynced){
            springCharge += 23;
            if(level != null){
                level.playSound(Minecraft.getInstance().player,worldPosition, SoundRegistry.SIDEROSTAT_WINDING.get(), SoundSource.BLOCKS,1.0f,1.0f);
                //level.playSound(Minecraft.getInstance().player,worldPosition, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.BLOCKS, 1.0f,0.2f);
            }
            /*angleSynced = false;
            springCharge -= 5;
            level.playSound(Minecraft.getInstance().player,worldPosition, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.BLOCKS, 1.0f,0.2f);
            setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }*/
        }
    }

    public void tick(Level level, BlockPos basePos, BlockState baseBlockState, SiderostatBlockEntity blockEntity){
        if(level.isClientSide()){
            return;
        }
        // 月の角度を取り出す
        double currentMoonAngle = (level.getTimeOfDay(1.0F) * 360 + 270) % 360;
        boolean isNight = Math.ceil(currentMoonAngle) > 0 && currentMoonAngle < 180;
        // 昼や雨天はインジケータを無効化
        if(!isNight || level.isRaining() || level.isThundering()){
            canSeeSkies = 0;
        }
        // ブロックステートから稼働状態を取り出す
        BlockPos topPos = basePos.above();
        BlockState topState = level.getBlockState(topPos);
        if (!topState.is(BlockRegistry.SIDEROSTAT_TOP.get())) {
            return;
        }
        SiderostatTopState topType = topState.getValue(SiderostatTopBlock.FACING_SIDEROSTAT);

        // 赤石入力がある場合、ゼンマイを巻く&自動で動作しない
        boolean powered = baseBlockState.getValue(SiderostatBaseBlock.POWERED);
        if(powered){
            angleSynced = false;
            if(springAmount - springCharge > 0){
                ++springCharge;
            }
        }
        // 夜の始まりに自動で起動
        if (!powered && springAmount <= 0 && topType == SiderostatTopState.FULL && isNight){
            BlockState newState = topState.setValue(SiderostatTopBlock.FACING_SIDEROSTAT,SiderostatTopState.MOVING);
            level.setBlock(topPos, newState, 3);
            level.playSound(Minecraft.getInstance().player, basePos, SoundRegistry.SIDEROSTAT_LOCK.get(), SoundSource.BLOCKS, 1.0f,1.0f);
            //angleSynced = true;
        }
        // ゼンマイが巻かれ始めたとき
        else if(springCharge > 0 && topType == SiderostatTopState.EMPTY){
            BlockState newState = topState.setValue(SiderostatTopBlock.FACING_SIDEROSTAT,SiderostatTopState.MOVING);
            level.setBlock(topPos, newState, 3);
            level.playSound(Minecraft.getInstance().player, basePos, SoundRegistry.SIDEROSTAT_LOCK.get(), SoundSource.BLOCKS, 1.0f,1.0f);
        }
        // 稼働状態で
        else if(topType == SiderostatTopState.MOVING){

            // 角度が揃っていないとき
            if(!angleSynced){
                // 夜かつ遅れているなら進める
                if(!powered && isNight && springAmount < currentMoonAngle){
                    ++springAmount;
                    // 追いついたらSync
                    if(springAmount > currentMoonAngle){
                        angleSynced = true;
                    }
                    //level.playSound(Minecraft.getInstance().player, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.5f,0.5f);
                }
                else if(springCharge > 0){
                    // チャージ
                    springAmount -= (int)Math.ceil(springCharge/2.0);
                    springCharge -= (int)Math.ceil(springCharge/2.0);
                    // 手動では戻しすぎない
                    if(!powered && isNight && springAmount < currentMoonAngle){
                        angleSynced = true;
                        springCharge = 0;
                    }
                    // ゼンマイが巻かれきったなら停止
                    if(springAmount <= 0){
                        springAmount = 0;
                        springCharge = 0;
                        BlockState newState = topState.setValue(SiderostatTopBlock.FACING_SIDEROSTAT,SiderostatTopState.FULL);
                        level.setBlock(topPos, newState, 3);
                        level.playSound(Minecraft.getInstance().player, basePos, SoundRegistry.SIDEROSTAT_LOCK.get(), SoundSource.BLOCKS, 1.0f,1.0f);
                    }
                }
            }
            // 揃っているとき
            else{
                // 視線方向の視野チェック
                canSeeSkies = checkCanSeeSkySlice(currentMoonAngle, canSeeSkies);
                canSeeSkies = checkCanSeeSkySlice(currentMoonAngle + SLICE_ANGLE, canSeeSkies);

                // 合わせる
                springAmount = (int)Math.floor(currentMoonAngle);

                // レシピ挙動
                operateRecipe(level, currentMoonAngle);
            }

            // クライアントに送信
            setChanged();
            level.sendBlockUpdated(topPos, level.getBlockState(topPos), topState, 3);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            // 夜明け、ゼンマイが最大角度になったら停止
            if(springAmount >= 180 /*&& !isNight*/){
                BlockState newState = topState.setValue(SiderostatTopBlock.FACING_SIDEROSTAT,SiderostatTopState.EMPTY);
                level.setBlock(topPos, newState, 3);
                level.playSound(Minecraft.getInstance().player, basePos, SoundRegistry.SIDEROSTAT_LOCK.get(), SoundSource.BLOCKS, 1.0f,1.0f);

                // 停止状態に
                springAmount = 180;
                springCharge = 0;
                angleSynced = false;
            }
        }
    }

    private void operateRecipe(Level level, double currentMoonAngle) {
        if ((!isAngleVisible(currentMoonAngle, canSeeSkies))|| level.isRaining() || level.isThundering()) {
            return;
        }
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        // 材料が空でないなら
        if (!inventory.getItem(1).isEmpty()) {
            // レシピ確認
            Optional<MoonlightPouringRecipe> optional =
                level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.MOONLIGHT_POURING.get(), inventory, level);
            if(optional.isPresent()){
                // レシピを取得
                MoonlightPouringRecipe recipe = optional.get();
                craftingTotalTime = recipe.getCraftTime();
                // 空と角度が適切なら
                /*if()*/{
                    // 進捗を追加
                    ++craftingProgress;
                    // 進捗が完了したら
                    if(craftingProgress >= craftingTotalTime){
                        // 結果アイテムを取得して追加
                        ItemStack tempStack = recipe.assemble(inventory, level.registryAccess());
                        itemHandler.insertItem(0, tempStack, false);
                        // 材料アイテムを消費して進捗リセット
                        if(!tempStack.isEmpty()){
                            inventory.getItem(1).shrink(1);
                            craftingProgress = 0;
                        }
                        // 入れられないなら進捗を待機
                        else{
                            craftingProgress = craftingTotalTime;
                        }
                    }
            /*setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);*/
                }
            }
        }else{
            craftingProgress = 0;
            craftingTotalTime = 0;
        }
    }

    // 角度と視野から支障物判定
    public boolean isAngleVisible(double angleDeg, short sight){
        // 角度が外ならfalse
        if(angleDeg < 0 || angleDeg > 180){
            return false;
        }
        // 11.25度刻みのスライス
        int index = (int)(angleDeg/SLICE_ANGLE);
        // 180度は最後のスライスに含める
        if(index >=SKY_SLICES) {
            --index;
        }
        // ビット判定
        return (sight & (1 << index)) != 0;
    }

    public int getSpringAmount() {
        return springAmount;
    }

    public int getCraftingProgress() {
        return craftingProgress;
    }

    public int getCraftingTotalTime() {
        return craftingTotalTime;
    }

    public boolean getSynced() {
        return angleSynced;
    }

    public int getSpringCharge() {
        return springCharge;
    }

    public short getCanSeeSkies() {
        return canSeeSkies;
    }
}
