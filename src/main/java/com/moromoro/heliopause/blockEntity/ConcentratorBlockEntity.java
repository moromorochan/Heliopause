package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.ConcentratorBlock;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.entity.LensBarrelEntity;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.recipe.StarlightConcentrationRecipe;
import com.moromoro.heliopause.recipe.StellarInstantiationRecipe;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import com.moromoro.heliopause.registry.TagRegistry;
import com.moromoro.heliopause.screen.ConcentratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.moromoro.heliopause.recipe.StarlightConcentrationRecipe.pickFluidStack;

public class ConcentratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_BARREL_LENGTH = ConfigHolder.MAX_BARREL_LENGTH.get();
    //private int targetStarX;    // ターゲット赤経(α)
    //private int targetStarY;    // ターゲット赤緯(δ)
    private Vector2i targetStarCoordinate;
    //private boolean isSyncedToStar;
    private boolean isSyncedToStar;
    // 星関係の生成
    public List<StellarInstantiationRecipe.StellarInstance> stellarInstances = new ArrayList<>();

    public static final int STAR_COUNT = 150;  // 星の数
    public static final int STAR_RANGE_X = 360;
    public static final int STAR_RANGE_MIN_Y = -60;
    public static final int STAR_RANGE_MAX_Y = 60;
    // メニュー渡し用データ ワールド依存なのでnbtには保存しない
    private long levelSeed;
    private long date;
    private long dayTime;
    private int barrelAccuracy;
    public static final int ACCURACY_DIVIDE = 1000;

    // 内部アイテム(液体取り出し用スロット)
    private final ItemStackHandler itemHandler = new ItemStackHandler(4){
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ConcentratorBlockEntity.this.setChanged();
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
    private static final int TANK_CAPACITY = 12000;
    private static final int TANK_COUNT = 3;
    private static class FluidTankHandler implements IFluidHandler{
        private final FluidTank[] fluidTanks = new FluidTank[]{
        new FluidTank((TANK_CAPACITY)),
        new FluidTank((TANK_CAPACITY)),
        new FluidTank((TANK_CAPACITY))
        };

        public CompoundTag tanksWriteToNbt(CompoundTag nbt){
            for (int i = 0; i < fluidTanks.length; i++) {
                CompoundTag tankNbt = new CompoundTag();
                fluidTanks[i].writeToNBT(tankNbt);
                nbt.put(String.valueOf(i), tankNbt);
            }
            return nbt;
        }

        public void tanksReadFromNbt(CompoundTag nbt){
            for (int i = 0; i < fluidTanks.length; i++) {
                String key = String.valueOf(i);
                if(nbt.contains(key)){
                    fluidTanks[i].readFromNBT(nbt.getCompound(key));
                }
                else {
                    fluidTanks[i].setFluid(FluidStack.EMPTY);
                }
            }
        }

        @Override
        public int getTanks() {
            return fluidTanks.length;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if(tank < 0 || tank > TANK_COUNT){
                return FluidStack.EMPTY;
            }
            return fluidTanks[tank].getFluidInTank(0);
        }

        @Override
        public int getTankCapacity(int tank) {
            if(tank < 0 || tank > TANK_COUNT){
                return 0;
            }
            return fluidTanks[tank].getTankCapacity(0);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if(tank < 0 || tank > TANK_COUNT){
                return false;
            }
            return fluidTanks[tank].isFluidValid(stack);
        }

        public int fillTo(int tank, FluidStack resource, FluidAction action){
            if(tank < 0 || tank > getTanks()){return 0;}
            return fluidTanks[tank].fill(resource, action);
        }

        public FluidStack drainFrom(int tank, FluidStack resource, FluidAction action){
            if(tank < 0 || tank > getTanks()){return FluidStack.EMPTY;}
            return fluidTanks[tank].drain(resource, action);
        }

        public FluidStack drainFrom(int tank, int maxDrain, FluidAction action){
            if(tank < 0 || tank > getTanks()){return FluidStack.EMPTY;}
            return fluidTanks[tank].drain(maxDrain, action);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            for (FluidTank fluidTank : fluidTanks) {
                int amount = fluidTank.fill(resource, action);
                if(amount > 0){
                    return amount;
                }
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            for (FluidTank fluidTank : fluidTanks) {
                FluidStack drained = fluidTank.drain(resource, action);
                if(!drained.isEmpty()){
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            for (FluidTank fluidTank : fluidTanks) {
                FluidStack drained = fluidTank.drain(maxDrain, action);
                if(!drained.isEmpty()){
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }
    };
    private final FluidTankHandler fluidHandler = new FluidTankHandler();

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    private int recipeTimer = 0;
    private static final int RECIPE_FREQ = 20; // 1秒毎

    // long値は上下に分割して送る
    public static final int DATA_ACCESS_LENGTH = 9;
    public static final int LEVEL_SEED_LOW = 0;
    public static final int LEVEL_SEED_HIGH = 1;
    public static final int DATE_LOW = 2;
    public static final int DATE_HIGH = 3;
    public static final int TIME_LOW = 4;
    public static final int TIME_HIGH = 5;
    public static final int COORDINATE = 6;
    public static final int ACCURACY = 7;
    public static final int IS_SYNCED_TO_STAR = 8;
    //public static final int TRACK_Y = 7;

    // GUI用データ格納
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index){
                case LEVEL_SEED_LOW -> (int) (ConcentratorBlockEntity.this.levelSeed & 0xFFFFFFFFL);
                case LEVEL_SEED_HIGH -> (int) (ConcentratorBlockEntity.this.levelSeed >>> 32);
                case DATE_LOW -> (int) (ConcentratorBlockEntity.this.date & 0xFFFFFFFFL);
                case DATE_HIGH -> (int) (ConcentratorBlockEntity.this.date >>> 32);
                case TIME_LOW -> (int) (ConcentratorBlockEntity.this.dayTime & 0xFFFFFFFFL);
                case TIME_HIGH -> (int) (ConcentratorBlockEntity.this.dayTime >>> 32);
                case COORDINATE -> ConcentratorMenu.encodeStarCoordinate(targetStarCoordinate);
                case ACCURACY -> ConcentratorBlockEntity.this.barrelAccuracy;
                case IS_SYNCED_TO_STAR -> ConcentratorBlockEntity.this.isSyncedToStar?1:0;
                //case TRACK_X -> targetStarCoordinate!=null ? targetStarCoordinate.x() : ConcentratorMenu.UNIDENTIFIED_COORDINATE;
                //case TRACK_Y -> targetStarCoordinate!=null ? targetStarCoordinate.y() : ConcentratorMenu.UNIDENTIFIED_COORDINATE;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case LEVEL_SEED_LOW -> {
                    long high = ConcentratorBlockEntity.this.levelSeed >>> 32;
                    ConcentratorBlockEntity.this.levelSeed = (high << 32) | (value & 0xFFFFFFFFL);
                }
                case LEVEL_SEED_HIGH -> {
                    long low = ConcentratorBlockEntity.this.levelSeed & 0xFFFFFFFFL;
                    ConcentratorBlockEntity.this.levelSeed = ((long) value << 32) | low;
                }
                case DATE_LOW -> {
                    long high = ConcentratorBlockEntity.this.date >>> 32;
                    ConcentratorBlockEntity.this.date = (high << 32) | (value & 0xFFFFFFFFL);
                }
                case DATE_HIGH -> {
                    long low = ConcentratorBlockEntity.this.date & 0xFFFFFFFFL;
                    ConcentratorBlockEntity.this.date = ((long) value << 32) | low;
                }
                case TIME_LOW -> {
                    long high = ConcentratorBlockEntity.this.dayTime >>> 32;
                    ConcentratorBlockEntity.this.dayTime = (high << 32) | (value & 0xFFFFFFFFL);
                }
                case TIME_HIGH -> {
                    long low = ConcentratorBlockEntity.this.dayTime & 0xFFFFFFFFL;
                    ConcentratorBlockEntity.this.dayTime = ((long) value << 32) | low;
                }
                case COORDINATE -> {
                    ConcentratorBlockEntity.this.targetStarCoordinate = ConcentratorMenu.decodeStarCoordinate(value);
                }
                case ACCURACY -> {
                    ConcentratorBlockEntity.this.barrelAccuracy = value;
                }
                case IS_SYNCED_TO_STAR -> {
                    ConcentratorBlockEntity.this.isSyncedToStar = (value==1);
                }
                /*case TRACK_X -> {
                    if(value == ConcentratorMenu.UNIDENTIFIED_COORDINATE){
                        ConcentratorBlockEntity.this.targetStarCoordinate = null;
                    } else if(targetStarCoordinate == null){
                        ConcentratorBlockEntity.this.targetStarCoordinate = new Vector2i(value,0);
                    } else{
                        ConcentratorBlockEntity.this.targetStarCoordinate.x = value;
                    }
                }
                case TRACK_Y -> {
                    if(value == ConcentratorMenu.UNIDENTIFIED_COORDINATE - 90){
                        ConcentratorBlockEntity.this.targetStarCoordinate = null;
                    } else if(targetStarCoordinate == null){
                        ConcentratorBlockEntity.this.targetStarCoordinate = new Vector2i(0, value);
                    } else{
                        ConcentratorBlockEntity.this.targetStarCoordinate.y = value;
                    }
                }*/
            }
        }

        @Override
        public int getCount() {
            return DATA_ACCESS_LENGTH;
        }
    };

    public ConcentratorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.CONCENTRATOR_BE.get(), blockPos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> fluidHandler);
        stellarInstances.clear();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if(side==Direction.UP || side==Direction.DOWN){
            return super.getCapability(cap, side);
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER){
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("isTracking", targetStarCoordinate != null);
        if(targetStarCoordinate != null){
            nbt.putInt("tracking_x", targetStarCoordinate.x());
            nbt.putInt("tracking_y", targetStarCoordinate.y());
        }
        nbt.put("Slot", itemHandler.serializeNBT());
        nbt.put("Tanks", fluidHandler.tanksWriteToNbt(new CompoundTag()));

        nbt.putInt("recipeTimer", recipeTimer);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if(nbt.getBoolean("isTracking")){
            targetStarCoordinate = new Vector2i(nbt.getInt("tracking_x"), nbt.getInt("tracking_y"));
        }else{
            targetStarCoordinate = null;
        }
        itemHandler.deserializeNBT(nbt.getCompound("Slot"));
        fluidHandler.tanksReadFromNbt(nbt.getCompound("Tanks"));

        recipeTimer = nbt.getInt("recipeTimer");
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
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
        this.load(nbt);
    }

    /*public double getYaw() {
        return this.yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return this.pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }*/

    /*public List<BlockState> getBarrels() {
        return barrels;
    }*/

    public boolean assemble(){
        if(level == null){
            return false;
        }
        if(level.isClientSide()){
            return true;
        }
        // 下が魔法陣か確認
        /*if(!(level.getBlockEntity(worldPosition.below()) instanceof AltAzimuthCircleBoardBlockEntity)){
            return false;
        }*/
        // 連続する鏡筒を確認
        List<BlockState> validBarrels = new ArrayList<>();
        for (int barrelPos = 0; barrelPos < MAX_BARREL_LENGTH + 1; barrelPos++) {
            BlockState barrelState = level.getBlockState(worldPosition.above(barrelPos + 1));
            if(barrelState.getBlock() instanceof LensBarrelBlock){
                validBarrels.add(barrelState);
            }else{
                break;
            }
        }
        if(validBarrels.size() <= 1 || validBarrels.size() > MAX_BARREL_LENGTH){
            return false;
        }
        // 下端が主鏡か
        if(!validBarrels.get(0).is(TagRegistry.Blocks.MAIN_MIRROR)){
            return false;
        }
        // 上端が副鏡か
        if(!validBarrels.get(validBarrels.size() - 1).is(TagRegistry.Blocks.SECOND_MIRROR)){
            return false;
        }
        // 途中が筒か
        for (BlockState blockState : validBarrels.subList(1, validBarrels.size() - 1)) {
            if(blockState.is(TagRegistry.Blocks.MAIN_MIRROR) || blockState.is(TagRegistry.Blocks.SECOND_MIRROR)){
                return false;
            }
        }
        // 適用
        LensBarrelEntity barrelEntity = new LensBarrelEntity(level, worldPosition.above().getCenter(), validBarrels);//.create(level);
        //barrelEntity.setPos(worldPosition.getCenter());
        //barrelEntity.assemble(validBarrels);
        level.addFreshEntity(barrelEntity);

        //this.barrels.clear();
        //this.barrels.addAll(validBarrels);
        // 鏡筒のブロックステートをブロックエンティティに保存
        for (int barrelPos = 0; barrelPos < validBarrels.size(); barrelPos++) {
            level.setBlock(worldPosition.above(barrelPos + 1), Blocks.AIR.defaultBlockState(), 3);
        }
        // インターフェースブロックを設置
        //level.setBlock(worldPosition.above(), BlockRegistry.STARLIGHT_CONCENTRATOR_INTERFACE.get().defaultBlockState(), 3);
        // enabled切り替え
        level.setBlock(worldPosition, this.getBlockState().setValue(ConcentratorBlock.ENABLED, true), 3);
        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.8f);
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.heliopause.concentrator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        // 表示メニュー
        ConcentratorMenu menu = new ConcentratorMenu(containerId, playerInventory, this, this.data);
        return menu;
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, ConcentratorBlockEntity blockEntity){
        if(level.isClientSide()){
            return;
        }
        if(level instanceof ServerLevel serverLevel){
            levelSeed = serverLevel.getSeed();
            date = Season.getDayInYear(serverLevel);
            dayTime = serverLevel.getDayTime();

            // 星を初期化
            if(stellarInstances.isEmpty()){
                stellarInstances = initStellarInstances(level, levelSeed);
            }

            // 液体を取り出す
            for (int tankSlot = 0; tankSlot < fluidHandler.getTanks(); tankSlot++) {
                FluidStack fluidStack = fluidHandler.getFluidInTank(tankSlot);
                if(fluidStack.isEmpty()){
                    continue;
                }
                ItemStack slotItem = itemHandler.getStackInSlot(tankSlot + 1).copyWithCount(1);
                if(slotItem.getItem() instanceof IFluidHandlerItem fluidHandlerItem){
                    int filled = fluidHandlerItem.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                    if(filled == 0){
                        continue;
                    }
                    FluidStack drained = fluidHandler.drainFrom(tankSlot, filled, IFluidHandler.FluidAction.SIMULATE);
                    fluidHandlerItem.fill(drained, IFluidHandler.FluidAction.EXECUTE);

                    // スロットに入れられるか確認
                    ItemStack remain = itemHandler.insertItem(0, slotItem, false);
                    if(remain == ItemStack.EMPTY){
                        fluidHandler.drainFrom(tankSlot, filled, IFluidHandler.FluidAction.EXECUTE);
                        itemHandler.extractItem(tankSlot + 1, 1, false);
                    }
                }
                else if(slotItem.is(Items.BUCKET)){
                    FluidStack drained = fluidHandler.drainFrom(tankSlot, FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
                    if(drained.getAmount() == 1000){
                        ItemStack resultBucketStack = fluidStack.getFluid().getBucket().getDefaultInstance();
                        ItemStack remain = itemHandler.insertItem(0, resultBucketStack, false);
                        if(remain == ItemStack.EMPTY){
                            fluidHandler.drainFrom(tankSlot, 1000, IFluidHandler.FluidAction.EXECUTE);
                            itemHandler.extractItem(tankSlot + 1, 1, false);
                        }
                    }

                }
            }

            // 液体を増やす
            if(blockState.getValue(ConcentratorBlock.ENABLED).equals(true)){
                if(recipeTimer < RECIPE_FREQ){
                    recipeTimer++;
                }else{
                    recipeTimer = 0;
                    LensBarrelEntity lensBarrelEntity = null;
                    for (LensBarrelEntity entity : level.getEntitiesOfClass(LensBarrelEntity.class, new AABB(worldPosition.above(2)))) {
                        lensBarrelEntity = entity;
                        break;
                    }
                    if(lensBarrelEntity != null){
                        barrelAccuracy = (int)Math.ceil(lensBarrelEntity.getWholeAccuracy() * ACCURACY_DIVIDE);
                        isSyncedToStar = lensBarrelEntity.isSyncedToStar();
                        if(isSyncedToStar){
                            // レシピの天体取得
                            List<StarlightConcentrationRecipe.FluidStackChance> recipeFluidStackChance =
                                getRecipeStellar(level, stellarInstances, targetStarCoordinate);

                            if(!recipeFluidStackChance.isEmpty()){
                                for (int tank = 0; tank < recipeFluidStackChance.size(); tank++) {
                                    if(tank >= fluidHandler.getTanks()){
                                        Heliopause.LOGGER.warn("Recipe length {} longer than Tank Length {}.", recipeFluidStackChance.size(), fluidHandler.getTanks());
                                        break;
                                    }
                                    if(recipeFluidStackChance.get(tank).accuracy() * ACCURACY_DIVIDE <= barrelAccuracy){
                                        FluidStack fluidStack = recipeFluidStackChance.get(tank).fluidStack();
                                        fluidHandler.fluidTanks[tank].fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }
                            }
                        }
                    }else{
                        barrelAccuracy = 0;
                    }
                }
            }

            this.setChanged();
            level.sendBlockUpdated(pos, blockState, blockState, 3);
        }
    }

    public static List<StarlightConcentrationRecipe.FluidStackChance> getRecipeStellar(Level level, List<StellarInstantiationRecipe.StellarInstance> stellarInstances, Vector2i targetStarCoordinate) {
        List<StarlightConcentrationRecipe.FluidStackChance> recipeFluidStackChance = new ArrayList<>();
        // ターゲット星取得
        Optional<StellarInstantiationRecipe.StellarInstance> targetOptional =
            stellarInstances.stream().filter(stellarInstance -> stellarInstance.coordinate().equals(targetStarCoordinate)).findFirst();
        if(targetOptional.isPresent()){
            StellarInstantiationRecipe.StellarInstance target = targetOptional.get();
            List<StarlightConcentrationRecipe> optional =
                level.getRecipeManager().getRecipesFor(RecipeTypeRegistry.STARLIGHT_CONCENTRATION.get(), new SimpleContainer(), level);
            // 特徴スロットごとに全候補からランダムで決定
            double[] slotWholePool = new double[]{0,0,0};
            List<StarlightConcentrationRecipe.FluidStackChance>[] slotStack = new List[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
            optional.sort(Comparator.comparing(recipe -> recipe.getId().toString()));
            for (StarlightConcentrationRecipe recipe : optional) {
                int featureId = recipe.getFeatureId();
                double chance = recipe.getChance(target);
                double accuracy = recipe.getAccuracy(target.localSeed());
                FluidStack fluidStack = recipe.getResultFluidStack();
                if(chance > 0 && !fluidStack.isEmpty()){
                    slotWholePool[featureId] += chance;
                    slotStack[featureId].add(new StarlightConcentrationRecipe.FluidStackChance(fluidStack, fluidStack.getAmount(), chance, accuracy));
                }
            }
            recipeFluidStackChance.addAll(pickFluidStack(RandomSource.create(target.localSeed()), slotStack, slotWholePool));
        }
        return recipeFluidStackChance;
    }


    public static @NotNull List<StellarInstantiationRecipe.StellarInstance> initStellarInstances(Level level, long levelSeed) {
        Optional<StellarInstantiationRecipe> stars =
            level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.STELLAR_INSTANTIATION.get(), new SimpleContainer(), level);
        if(stars.isPresent()){
            StellarInstantiationRecipe recipe = stars.get();
            return StellarInstantiationRecipe.getStars(recipe, levelSeed);
        }
        return new ArrayList<>();
    }

    /*public boolean isSyncedToStar() {
        return isSyncedToStar;
    }

    public void setSyncedToStar(boolean syncedToStar) {
        this.isSyncedToStar = syncedToStar;
    }*/

    public Vector2i getTargetStarCoordinate() {
        return targetStarCoordinate;
    }

    public void setTargetStarCoordinate(Vector2i targetStarCoordinate) {
        this.targetStarCoordinate = targetStarCoordinate;
    }


}
