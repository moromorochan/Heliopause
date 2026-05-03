package com.moromoro.heliopause.blockEntity;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.OrreryCircleBoardBlock;
import com.moromoro.heliopause.entity.StellarIngredientEntity;
import com.moromoro.heliopause.generic.PolarCoordinates;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.item.ImitationCoreItem;
import com.moromoro.heliopause.particle.StarRippleParticles;
import com.moromoro.heliopause.recipe.ImitationCoreAssemblyRecipe;
import com.moromoro.heliopause.recipe.OrreryTransferenceRecipe;
import com.moromoro.heliopause.registry.*;
import com.moromoro.heliopause.render.OrreryCircleBoardRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector2d;
import oshi.util.tuples.Pair;

import java.util.*;

import static java.lang.Math.max;

public class OrreryCircleBoardBlockEntity extends AbstractWrittenBoardBlockEntity/* implements Hopper*/ {
    // 動作ティック
    public static int STARTING_TICK = 100;
    public static int FINISHING_TICK = 100;
    //パーティクルやレンダリングに使用する向心力
    public static final float DEFAULT_CENT_FORCE = 0.001f;
    //周転する天体の配列
    private final List<CircumstellarIngredient> ingredients = new ArrayList<>();

    // 動作中のレシピ
    private ResourceLocation[] processingRecipe;
    //private boolean isCraftingInBreak = false;
    private boolean isCraftingInProgress = false;
    private boolean isCraftingInFinish = false;
    // レシピタイマー
    private int progressTimer = 0;
    // 中心星アイテム / 完成品
    //private CircumstellarIngredient.StellarStack centerStack;
    private ItemStack centerItemStack = ItemStack.EMPTY;
    
    // tickごとのsetChanged用
    //private boolean changed = false;
    
    // 中心星アイテムを格納するスロット
    /*protected ItemStackHandler centerItemHandler = new ItemStackHandler(1){
        // 内容更新毎にセーブ
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            OrreryCircleBoardBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        *//*@Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // 搬入不可
            return stack;
        }*//*
    };*/

    public OrreryCircleBoardBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ORRERY_CIRCLE_BOARD_BE.get(), pos, blockState);
    }

    @Override
    public ResourceLocation getLineType(){
        return CustomModelRegistry.LINE_ORRERY;
    }
    @Override
    public ResourceLocation getCircleType(){
        return CustomModelRegistry.CIRCLE_ORRERY;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        ingredients.clear();
        ingredients.addAll(circumStellarFromNbt(nbt));
        int progress = nbt.getInt("isCrafting");
        isCraftingInProgress = (progress==1);
        isCraftingInFinish = (progress==2);
        if(isCraftingInProgress){
            processingRecipe = new ResourceLocation[]{
                new ResourceLocation(nbt.getString("star")),
                new ResourceLocation(nbt.getString("recipe"))
            };
        }
        //isCraftingInBreak = nbt.getBoolean("isBreaking");

        progressTimer = nbt.getInt("timer");

        //centerItemHandler.deserializeNBT(nbt.getCompound("center"));
        /*CompoundTag center = nbt.getCompound("center");
        centerStack = new CircumstellarIngredient.StellarStack(
            center.contains("centerItemStack") ? ItemStack.of(center.getCompound("centerItemStack")): ItemStack.EMPTY,
            center.contains("fluidStack") ? FluidStack.loadFluidStackFromNBT(center.getCompound("fluidStack")): FluidStack.EMPTY
        );*/
        centerItemStack.deserializeNBT(nbt.getCompound("centerStack"));//.of(nbt.getCompound("center"));
        //changed = true;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("ingredients", circumStellarToNbt(ingredients));
        int progress = 0;
        if(isCraftingInProgress){
            progress = 1;
            nbt.putString("star", processingRecipe[0].toString());
            nbt.putString("recipe",processingRecipe[1].toString());
        } else if (isCraftingInFinish) {
            progress = 2;
        }
        nbt.putInt("isCrafting", progress);
        //nbt.putBoolean("isBreaking",isCraftingInBreak);
        nbt.putInt("timer", progressTimer);

        nbt.put("centerStack", Objects.requireNonNullElse(centerItemStack, ItemStack.EMPTY).serializeNBT());
        //nbt.put("center",centerItemHandler.serializeNBT());
        /*CompoundTag center = new CompoundTag();
        if(!this.centerStack.itemStack().isEmpty()){
            center.put("centerItemStack", this.centerStack.itemStack().save(new CompoundTag()));
        } else if (!this.centerStack.fluidStack().isEmpty()) {
            center.put("fluidStack", this.centerStack.fluidStack().writeToNBT(new CompoundTag()));
        }
        nbt.put("center", center);*/
    }

    //IngredientsをNBTに書き込む
    private static ListTag circumStellarToNbt(List<CircumstellarIngredient> ingredients){
        ListTag ingredientsList = new ListTag();
        for (CircumstellarIngredient ingredient : ingredients) {
            if (!ingredient.isValid()) {
                //ingredients.remove(ingredient);
                continue;
            }
            ingredientsList.add(CircumstellarIngredient.writeToNBT(new CompoundTag(), ingredient));
        }
        return ingredientsList;
    }

    // IngredientsをNBTから読み込む
    public static List<CircumstellarIngredient> circumStellarFromNbt(@NotNull CompoundTag nbt) {
        List<CircumstellarIngredient> newIngredient = new ArrayList<>();
        ListTag ingredientsList = nbt.getList("ingredients", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < ingredientsList.size(); i++) {
            CompoundTag ingredientNbt = ingredientsList.getCompound(i);
            CircumstellarIngredient ingredient = CircumstellarIngredient.readFromNbt(ingredientNbt);
            if(!ingredient.isValid()) {
                //newIngredient.remove(ingredient);
                continue;
            }
            newIngredient.add(ingredient);
        }
        return newIngredient;
    }

    @Override
    public boolean isFunctionalRoot(@NotNull Level worldlevel){
        return true;
    }

    /*@Override
    public void setRemoved() {
        super.setRemoved();
    }*/

    // ネットワークへの追加・削除時の挙動
    @Override
    protected void nodeNetworkChanged(){
        this.eraseNode(1);
    }

    @Override
    public AABB getRenderBoundingBox(){
        // 円の範囲
        List<Double> renderCircleRadii = new ArrayList<>(getCircleRadii());
        double maxCircle = renderCircleRadii.isEmpty() ? 0.5 : Collections.max(renderCircleRadii) + CLICK_SIZE;
        double minPosX = worldPosition.getCenter().x() - maxCircle;
        double minPosZ = worldPosition.getCenter().z() - maxCircle;
        double maxPosX = worldPosition.getCenter().x() + maxCircle;
        double maxPosZ = worldPosition.getCenter().z() + maxCircle;

        // 高さの範囲
        double maxPosY = worldPosition.getY() + (getCircumstellars().isEmpty()? 1 : OrreryCircleBoardRenderer.getProgressOffsetY(this, 0) + 1.5);
        return new AABB(minPosX, worldPosition.getY(), minPosZ, maxPosX, maxPosY, maxPosZ);
    }

    @Override
    public boolean operateFromArea(Level level, BlockPos blockPos, Vec3 location, Player player, InteractionHand hand) {
        // クラフトが進行中は受け付けない
        if(isCraftingInProgress){
            return false;
        }

        // 周転材料として追加
        return this.setCircumstellar(level, player, hand, blockPos, location);

    }

    public List<CircumstellarIngredient> getCircumstellars() {
        return ingredients;
    }

    public double getCentForce(float partialTicks) {
        double defaultCentForce = OrreryCircleBoardBlockEntity.DEFAULT_CENT_FORCE;
        double timer = getProgressTimer() + partialTicks;
        if(level == null){
            return defaultCentForce;
        }
        ResourceLocation[] processingRecipe = getProcessingRecipe();
        if(processingRecipe!=null && (isCraftingInProgress || isCraftingInFinish)){
            Optional<? extends Recipe<?>> starOptional = level.getRecipeManager().byKey(processingRecipe[0]);
            if(starOptional.isPresent()&& starOptional.get() instanceof ImitationCoreAssemblyRecipe star){
                double starCentForce = star.getCoreProperty().centForce();
                if(isCraftingInProgress){
                    return Math.lerp(starCentForce, defaultCentForce, -(Math.min(0, timer) / STARTING_TICK));
                } else {
                    return Math.lerp(starCentForce, DEFAULT_CENT_FORCE,Math.min(FINISHING_TICK - 10,timer)/(FINISHING_TICK - 10));
                }
            }
        }
        return defaultCentForce;
    }

    public boolean isCraftingInProgress() {
        return isCraftingInProgress;
    }
    public boolean isCraftingInFinish() {
        return isCraftingInFinish;
    }
    /*public boolean isCraftingInBreak(){
        return isCraftingInBreak;
    }*/

    public int getProgressTimer() {
        return progressTimer;
    }

    public ResourceLocation[] getProcessingRecipe() {
        return processingRecipe;
    }

    public boolean setCircumstellar(@NotNull Level level, Player player, InteractionHand hand, BlockPos blockPos, Vec3 location) {
        // 範囲が存在するか確認
        if(getCircleRadii().isEmpty()){
            return false;
        }
        // 中心からの相対位置を取り出す
        double relativeSetPosX = location.x() - worldPosition.getCenter().x();
        double relativeSetPosZ = location.z() - worldPosition.getCenter().z();
        // 範囲内か確認
        double circleSize = Collections.max(getCircleRadii()) + CLICK_SIZE;
        double distance = new Vector2d(relativeSetPosX, relativeSetPosZ).length();
        // 範囲外なら処理しない
        if(distance > circleSize){
            return false;
        }
        // 最小半径以下なら最小半径に
        if(distance < OrreryCircleBoardBlock.InnerLimitRadius){
            relativeSetPosX *= OrreryCircleBoardBlock.InnerLimitRadius/distance;
            relativeSetPosZ *= OrreryCircleBoardBlock.InnerLimitRadius/distance;
        }
        // レシピ稼働中は操作不可(クリックは消費)
        if(isCraftingInProgress || isCraftingInFinish){
            return true;
        }
        //操作位置を極座標に変換
        Pair<Double, Double> setPolarPos = PolarCoordinates.getPolarCoordinates(relativeSetPosX, relativeSetPosZ);
        double radius = setPolarPos.getA();
        double theta = setPolarPos.getB();

        // 入力を取得
        ItemStack inputStack = player.getItemInHand(hand);
        // 入力が液体保持アイテムのとき
        LazyOptional<IFluidHandlerItem> fluidCapability = inputStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if(fluidCapability.isPresent()){
            IFluidHandlerItem fluidHandler = fluidCapability.orElseThrow(IllegalStateException::new);
            // 液体を取り出す
            FluidStack drained = fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            if(!drained.isEmpty()){
                //inputIngredient = new OrreryIngredient(drained);
                // 周転材料を作成
                ingredients.add(new CircumstellarIngredient((float) radius, (float) theta, drained, false));
                // 効果音を再生
                SoundEvent sound = drained.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                if(sound!=null){
                    Vector2d pos = PolarCoordinates.getCartesianCoordinates(radius, theta);
                    level.playSound(player,
                        worldPosition.getX() + pos.x(), worldPosition.getY(), worldPosition.getZ() + pos.y(),
                        sound, SoundSource.BLOCKS, 1,1);
                }
                // アイテムを更新
                player.setItemInHand(hand, fluidHandler.getContainer());
                
            }else {
                // 空の液体保持アイテムなら既存を回収
                this.retrieveCircumstellarFluid(fluidHandler, player, hand, radius, theta);
                return true;
            }
        }
        // 入力がアイテムのとき
        else if(!inputStack.isEmpty()){
            //inputIngredient = new OrreryIngredient(inputStack.copyWithCount(1));
            // 周転材料を作成
            ingredients.add(new CircumstellarIngredient((float) radius, (float) theta, inputStack.copyWithCount(1), false));
            // 効果音を再生
            Vector2d pos = PolarCoordinates.getCartesianCoordinates(radius, theta);
            level.playSound(player,
                worldPosition.getX() + pos.x(), worldPosition.getY(), worldPosition.getZ() + pos.y(),
                SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1);
            //アイテムを消費
            inputStack.shrink(1);
        }else {
            // 素手なら既存を回収
            this.retrieveCircumstellarItem(player, hand, radius, theta);
            return true;
        }

        //軌道半径順にソート
        ingredients.sort(Comparator.comparing(CircumstellarIngredient::getOrbitalRadius));
        //changed = true;
        //this.setChanged();
        //level.sendBlockUpdated(blockPos, level.getBlockState(blockPos), level.getBlockState(blockPos), 3);
        return true;
    }

    // 液体を回収
    private boolean retrieveCircumstellarFluid(IFluidHandlerItem fluidHandler, Player player, InteractionHand hand, double radius, double theta) {
        CircumstellarIngredient ingredient = getCircumStellarFromPos(this.ingredients, radius, theta, CLICK_SIZE * 2);
        if(ingredient == null){
            return false;
        }
        FluidStack ingredientFluidStack = ingredient.getFluidStack();
        if(!ingredientFluidStack.isEmpty()){
            int filled = fluidHandler.fill(ingredientFluidStack, IFluidHandler.FluidAction.EXECUTE);
            ingredientFluidStack.shrink(filled);
            // 効果音を再生
            if(filled > 0 && level!=null){
                SoundEvent sound = fluidHandler.getFluidInTank(0).getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                if(sound!=null){
                    Vector2d pos = PolarCoordinates.getCartesianCoordinates(radius, theta);
                    level.playSound(player,
                        worldPosition.getX() + pos.x(), worldPosition.getY(), worldPosition.getZ() + pos.y(),
                        sound, SoundSource.BLOCKS, 1,1);
                }
                
                // アイテムを更新
                player.setItemInHand(hand, fluidHandler.getContainer());
            }
            // 空になったら周転材料を削除
            if(ingredientFluidStack.isEmpty()){
                ingredients.remove(ingredient);
            }
            ingredient.setFluidStack(ingredientFluidStack);
        }
        return true;
    }

    // アイテムを回収
    private boolean retrieveCircumstellarItem(Player player, InteractionHand hand, double radius, double theta) {
        CircumstellarIngredient ingredient = getCircumStellarFromPos(this.ingredients, radius, theta, CLICK_SIZE * 2);
        if(ingredient == null){
            return false;
        }
        player.setItemInHand(hand, ingredient.getItemStack());
        // 効果音を再生
        if(level!=null){
            Vector2d pos = PolarCoordinates.getCartesianCoordinates(radius, theta);
            level.playSound(player,
                worldPosition.getX() + pos.x(), worldPosition.getY(), worldPosition.getZ() + pos.y(),
                SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
        }
        ingredients.remove(ingredient);
        return true;
    }

    // 位置から周転材料を取得
    private @Nullable CircumstellarIngredient getCircumStellarFromPos(List<CircumstellarIngredient> ingredients, double radius, double theta, double getSize) {
        for (CircumstellarIngredient ingredient : ingredients) {
            // 半径が遠いものを除外
            if(Math.abs(ingredient.getOrbitalRadius() - radius) > getSize){
                continue;
            }
            // 直交座標系で距離を計算
            Vector2d operatePos = PolarCoordinates.getCartesianCoordinates(radius, theta);
            Vector2d ingredientPos = PolarCoordinates.getCartesianCoordinates(ingredient.getOrbitalRadius(), ingredient.getRevolutionOffset());
            // 近いものを返す
            if(operatePos.distance(ingredientPos) <= getSize){
                return ingredient;
            }
        }
        return null;
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, OrreryCircleBoardBlockEntity blockEntity) {
        if(level==null){
            return;
        }
        if(!level.isClientSide()){
            if(ingredients.isEmpty()){
                return;
            }
            RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
            //星周天体の生成
            setIngredientsBehavior(pos, randomSource);
            // レシピ動作
            operateRecipe(level, pos, blockState);
            
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, blockState, blockState, 3);
        }
    }

    private void operateRecipe(Level level, BlockPos pos, BlockState blockState) {
        if(level.isClientSide){
            return;
        }
        // レシピ取得済みの場合
        if(processingRecipe!=null){
            Optional<? extends Recipe<?>> starOptional = level.getRecipeManager().byKey(processingRecipe[0]);
            Optional<? extends Recipe<?>> recipeOptional = level.getRecipeManager().byKey(processingRecipe[1]);
            if(
                starOptional.isPresent() && recipeOptional.isPresent()
                    && starOptional.get() instanceof ImitationCoreAssemblyRecipe star
                    && recipeOptional.get() instanceof OrreryTransferenceRecipe recipe
            ){
                //changed = true;
                // 中断中なら
                /*if(isCraftingInBreak){
                    breakingRecipe(level, pos, recipe);
                    return;
                }*/
                // 遷移が終わって組み立ての段階なら
                if(isCraftingInFinish){
                    finishingRecipe(level, pos, recipe);
                    return;
                }
                // 稼働しているなら軌道を動かす
                if(isCraftingInProgress){
                    progressingRecipe(level, pos, star, recipe);
                    return;
                }
                return;
            }
        }
        // 稼働していないならレシピチェック
        {
            // 中心スタックにアイテムが残っているならレシピ確認しない
            if (!centerItemStack.isEmpty()/*this.centerStack.itemStack().isEmpty() || !this.centerStack.fluidStack().isEmpty()*/) {
                return;
            }
            // 中心星が出現する位置の障害物チェック
            if(!level.getBlockState(worldPosition.above(2)).isAir()){
                return;
            }
            // 模造天体が投げ込まれたか確認
            AABB itemCheckArea = new AABB(pos.getCenter().add(-0.5,1,-0.5), pos.getCenter().add(0.5,1.5, 0.5));
            List<ItemEntity> itemEntity = level.getEntitiesOfClass(ItemEntity.class, itemCheckArea, EntitySelector.ENTITY_STILL_ALIVE);
            for (ItemEntity entity : itemEntity) {
                if (!entity.isAlive()) {
                    continue;
                }
                ItemStack itemStack = entity.getItem().copy();
                if(startRecipe(level, itemStack, pos)){
                    itemStack.shrink(1);
                    if (itemStack.isEmpty()) {
                        entity.discard();
                    }
                    entity.setItem(itemStack);
                    //changed = true;
                    return;
                }
            }

            // 上のブロックが模造天体を持っているか確認
            BlockEntity aboveBlockEntity = level.getBlockEntity(pos.above());
            if(aboveBlockEntity == null){
                return;
            }
            LazyOptional<IItemHandler> itemCapability = aboveBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if(itemCapability.isPresent()){
                IItemHandler itemHandler = itemCapability.orElseThrow(IllegalStateException::new);
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack itemStack = itemHandler.getStackInSlot(i).copy();
                    if(startRecipe(level, itemStack, pos)){
                        itemHandler.extractItem(i, 1, false);
                        //changed = true;
                    }
                }
            }
        }
    }

    private boolean startRecipe(Level level, ItemStack itemStack, BlockPos pos){
        //中心星チェック
        if (!(itemStack.getItem() instanceof ImitationCoreItem)) {
            return false;
        }
        // ブロックエンティティをアイテム化
        ItemStack blockEntityItem = new ItemStack(BlockRegistry.ORRERY_CIRCLE_BOARD.get());
        BlockItem.setBlockEntityData(blockEntityItem, BlockEntityRegistry.ORRERY_CIRCLE_BOARD_BE.get(), this.getUpdateTag());
        // レシピチェック用コンテナを作成
        Container starmatchContainer = new SimpleContainer(itemStack);
        Container recipeMatchContainer = new SimpleContainer(itemStack, blockEntityItem);
        //レシピチェック
        Optional<ImitationCoreAssemblyRecipe> newstarOptional =
            level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.IMITATION_CORE_ASSEMBLY.get(), starmatchContainer, level);
        Optional<OrreryTransferenceRecipe> newRecipeOptional =
            level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.ORRERY_TRANSFERENCE.get(), recipeMatchContainer, level);
        if(newstarOptional.isPresent() && newRecipeOptional.isPresent()) {
            ImitationCoreAssemblyRecipe star = newstarOptional.get();
            OrreryTransferenceRecipe recipe = newRecipeOptional.get();

            // 星の初回液体消費確認
            if (!fillStarFluid(level, pos, star)) {
                return false;
            }
            // 模造天体コアを消費して中心ブロックに入れる
            //this.centerStack = new CircumstellarIngredient.StellarStack(itemStack.copyWithCount(1), FluidStack.EMPTY);
            centerItemStack = itemStack.copyWithCount(1);

            // レシピを保持
            processingRecipe = new ResourceLocation[]{
                star.getId(),
                recipe.getId()
            };
            // 演出用に負の値から開始
            progressTimer = -100;
            isCraftingInProgress = true;

            // 効果音を再生
            level.playSound(null,
                pos.getX(), pos.getY() + 1, pos.getZ(),
                SoundRegistry.RIPPLE.get(), SoundSource.BLOCKS, 1.5f, 1f);
            level.playSound(null,
                pos.getX(), pos.getY() + 1, pos.getZ(),
                SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.7f);

            // パーティクルを生成
            StarRippleParticles particles = (StarRippleParticles)Minecraft.getInstance().particleEngine.createParticle(
                ParticleRegistry.STAR_RIPPLE_PARTICLES.get(),
                pos.getX()+0.5,pos.getY()+1.5,pos.getZ()+0.5, 0,0,0
            );
            if(particles!=null) {
                particles.setScale((float) Math.max(1.5,(this.getMaxCircleRadius() - CLICK_SIZE) * 0.5));
            }
            return true;
        }
        return false;
    }

    private void progressingRecipe(Level level, BlockPos pos, ImitationCoreAssemblyRecipe star, OrreryTransferenceRecipe recipe) {
        // タイマーごとの挙動
        if(progressTimer == 0){
            // 星が液体を消費
            boolean fillSucceed = fillStarFluid(level, pos, star);
            // 足りないならレシピ停止
            if(!fillSucceed){
                breakRecipe();
                //isCraftingInProgress = false;
                //processingRecipe = null;
                //dropCenter();
                return;
            }
        }
        progressTimer++;
        if(progressTimer >= 20){
            progressTimer = 0;
        }
        if(progressTimer % 20 == 0){
            for (CircumstellarIngredient ingredient : ingredients) {
                // 効果音を再生
                level.playSound(null,
                    pos.getX(), pos.getY() + 1, pos.getZ(),
                    SoundRegistry.ORRERY_ROTATE.get(), SoundSource.BLOCKS, 1.5f/(ingredients.size()*0.8f), 3.5f/ingredient.getOrbitalRadius());
            }
        }

        if(progressTimer >= 0){
            // 軌道をレシピの比に遷移
            OrreryTransferenceRecipe.TransferOrbitsResult transferResult =
                recipe.transferOrbits(this.ingredients, star.getCoreProperty().centForce(), OrreryCircleBoardBlock.InnerLimitRadius, Collections.max(getCircleRadii()) - CLICK_SIZE);
            this.ingredients.clear();
            this.ingredients.addAll(transferResult.ingredients());
            // 軌道が揃ったら
            if(transferResult.transferCompleted()){
                isCraftingInProgress = false;
                progressTimer = 0;
                // 最終工程へ進む
                isCraftingInFinish = true;

                // 効果音を再生
                level.playSound(null,
                    pos.getX(), pos.getY() + 1, pos.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.5f, 0.5f);
            }
        }
    }

    private void finishingRecipe(Level level, BlockPos pos, OrreryTransferenceRecipe recipe) {
        if(progressTimer == 100){
            // 中心星を消費
            //this.centerStack = new CircumstellarIngredient.StellarStack(ItemStack.EMPTY, FluidStack.EMPTY);
            //centerItemHandler.getStackInSlot(0);
            centerItemStack.shrink(1);
            // 軌道の材料を全消費
            this.ingredients.clear();
            // 結果アイテムを保持するブロックを設置
            BlockPos stellarBlockPos = pos.above(2);
            BlockState stellarBlockState = BlockRegistry.STELLAR_INGREDIENT_BLOCK.get().defaultBlockState();
            level.setBlock(stellarBlockPos, stellarBlockState, 3);
            StellarIngredientBlockEntity newEntity = BlockEntityRegistry.STELLAR_INGREDIENT_BE.get().create(stellarBlockPos, stellarBlockState);
            if(newEntity != null){
                level.setBlockEntity(newEntity);
                newEntity.setStellarStack(recipe.getResultStellarStack());
                newEntity.setChanged();
                level.sendBlockUpdated(stellarBlockPos, stellarBlockState, stellarBlockState, 3);
            }
            // パラメータをリセット
            isCraftingInFinish = false;
            progressTimer = 0;
            processingRecipe[0] = new ResourceLocation("");
            processingRecipe[1] = new ResourceLocation("");
            processingRecipe = null;
            
            // 更新
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            //効果音を再生
            level.playSound(null,
                pos.getX(), pos.getY() + 1, pos.getZ(),
                SoundEvents.ARMOR_EQUIP_IRON, SoundSource.BLOCKS, 1.5f, 1f);
            return;
        }
        progressTimer++;
    }

    /*private void breakingRecipe(Level level, BlockPos pos, OrreryTransferenceRecipe recipe){

    }*/
    // すべてを中断
    public void breakRecipe(){
        // 完成の途中で中断されたらアイテムは消失
        if(!isCraftingInFinish){
            for (CircumstellarIngredient ingredient : new ArrayList<>(ingredients)) {
                dropIngredient(ingredient, true);
            }
        }
        isCraftingInProgress = false;
        isCraftingInFinish = false;
        progressTimer = 0;
        if(processingRecipe!= null){
            processingRecipe[0] = new ResourceLocation("");
            processingRecipe[1] = new ResourceLocation("");
            processingRecipe = null;
        }
        //ingredients.clear();
        dropCenter();
        this.setChanged();
        if(level!=null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.playSound(null,
                worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 2.0f, 0.8f);
        }
    }
    /*@Override
    public VoxelShape getSuckShape() {
        return SUCK;
    }*/

    private boolean fillStarFluid(Level level, BlockPos pos, ImitationCoreAssemblyRecipe star) {
        boolean drainSucceed = false;
        // 上のブロックエンティティを取得
        BlockEntity aboveBlockEntity = level.getBlockEntity(pos.above());
        if(aboveBlockEntity == null){
            return false;
        }
        // 液体ハンドラを確認
        LazyOptional<IFluidHandler> fluidCapability = aboveBlockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if(fluidCapability.isPresent()){
            IFluidHandler fluidHandler = fluidCapability.orElseThrow(IllegalStateException::new);
            // 星の要求量分取得を試みる
            int starDrain = star.getCoreProperty().usagePerSec();
            Fluid starFluid = star.getCoreProperty().fluid();
            FluidStack drainSim = fluidHandler.drain(starDrain, IFluidHandler.FluidAction.SIMULATE);
            // 量が十分かつ種類が一致するなら実際に消費
            if(drainSim.getAmount() == starDrain && drainSim.getFluid().isSame(starFluid)){
                fluidHandler.drain(starDrain, IFluidHandler.FluidAction.EXECUTE);
                drainSucceed = true;
            }
        }

        return drainSucceed;
    }

    // 中心スタックをその場にドロップ
    private void dropCenter() {
        if(level == null || level.isClientSide()){
            return;
        }
        ItemEntity itemEntity = new ItemEntity(level, worldPosition.getCenter().x(), worldPosition.getCenter().y()+1, worldPosition.getCenter().z(), /*this.centerStack.itemStack()*/centerItemStack.copy());
        level.addFreshEntity(itemEntity);
        //this.centerStack = new CircumstellarIngredient.StellarStack(ItemStack.EMPTY, FluidStack.EMPTY);
        centerItemStack = ItemStack.EMPTY;//shrink(centerItemStack.getCount());
        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // 周転材料の公転
    protected void setIngredientsBehavior(BlockPos pos, RandomSource randomSource) {
        //軌道半径順にソート
        ingredients.sort(Comparator.comparing(CircumstellarIngredient::getOrbitalRadius));
        //材料毎の処理
        for (int i = 0; i < ingredients.size(); i++) {
            CircumstellarIngredient ingredient = ingredients.get(i);
            if(!ingredient.isValid()){
                ingredients.remove(i);
                i--;
                continue;
            }
            float orbitalRadius = ingredient.getOrbitalRadius();
            //オーブ
            if(!ingredient.isDiskShaped()){
                //公転を計算
                ingredient.setRevolutionOffset(
                    (float) PolarCoordinates.revolutionProcess(orbitalRadius, ingredient.getRevolutionOffset(), (float) getCentForce(1), 1));

                // 衝突判定
                if(!isCraftingInFinish){
                    // 位置を取得
                    Vector2d localIngredientPos = PolarCoordinates.getCartesianCoordinates(ingredient.getOrbitalRadius(), ingredient.getRevolutionOffset());
                    Vec3 ingredientPos = worldPosition.getCenter().add(
                        localIngredientPos.x(),
                        OrreryCircleBoardRenderer.getProgressOffsetY(this, 1),
                        localIngredientPos.y()
                    );
                    // 隣を取得
                    CircumstellarIngredient proximityIngredient = null;
                    boolean findProximity = false;
                    for(int j = i+1; j < ingredients.size(); j++){
                        proximityIngredient = ingredients.get(j);
                        double limitDistance = CircumstellarIngredient.getSatRadius(ingredient.getStellarStack()) + CircumstellarIngredient.getSatRadius(proximityIngredient.getStellarStack());
                        // 軌道が離れたら終了
                        if(proximityIngredient.getOrbitalRadius() - ingredient.getOrbitalRadius() > limitDistance){
                            break;
                        }
                        // 衝突するものが見つかったら終了
                        if(getCircumStellarFromPos(
                            List.of(proximityIngredient), ingredient.getOrbitalRadius(), ingredient.getRevolutionOffset(), limitDistance
                        ) != null){
                            findProximity = true;
                            break;
                        }
                    }
                    if(findProximity){
                        if(isCraftingInProgress){
                            // クラフト中なら円盤にして喪失
                            //createDisk(ingredient);
                            //createDisk(proximityIngredient);
                            if(level!=null) {
                                level.playSound(null, ingredientPos.x(), ingredientPos.y(),ingredientPos.z(),
                                    SoundEvents.TRIDENT_HIT_GROUND, SoundSource.BLOCKS, 1.0f, 0.7f);
                            }
                            breakRecipe();
                        }else{
                            // クラフト前ならその場にドロップ
                            dropIngredient(ingredient, true);
                            dropIngredient(proximityIngredient, true);
                        }
                        i--;
                        continue;
                    }
                    if(level!=null) {
                        // ぶつかる可能性のあるブロックを取得
                        BlockPos intersectPos = BlockPos.containing(ingredientPos);
                        BlockState intersectBlockState = level.getBlockState(intersectPos);
                        AABB intersectBox = new AABB(ingredientPos, ingredientPos).inflate(StellarIngredientEntity.SIZE * 0.5);
                        VoxelShape intersectBlockShape = intersectBlockState.getCollisionShape(level, intersectPos);
                        if(!intersectBlockShape.isEmpty()){
                            if(intersectBox.intersects(intersectBlockShape.bounds().move(intersectPos))){
                                if(isCraftingInProgress) {
                                    level.playSound(null, ingredientPos.x(), ingredientPos.y(),ingredientPos.z(),
                                        SoundEvents.TRIDENT_HIT_GROUND, SoundSource.BLOCKS, 1.0f, 0.7f);
                                    breakRecipe();
                                    SoundEvent collideBlockHit = intersectBlockState.getSoundType().getHitSound();
                                    level.playSound(null, ingredientPos.x(), ingredientPos.y(),ingredientPos.z(),
                                        collideBlockHit, SoundSource.BLOCKS, 1.5f, 1.0f);
                                    breakRecipe();
                                }else{
                                    dropIngredient(ingredient, true);
                                }
                                continue;
                            }
                        }

                    }
                }

            }
            /*//円盤
            else {
                float ringRadius = ingredient.getOrbitalRadius();
                if(ingredient.isValid()){
                    float ringWidth = calcRingWidth(max(ingredient.getFluidStack().getAmount(), ingredient.getItemStack().getCount() * 1000), ringRadius);

                    //公転
                    ingredient.setRevolutionOffset(calcRevProcess(orbitalRadius, ingredient.getRevolutionOffset()));

                    //パーティクル生成
                    if(!ingredient.getFluidStack().isEmpty()){
                        createFluidDiskParticle(pos, ingredient.getFluidStack(), ingredient.getRevolutionOffset(), ringRadius, ringWidth, randomSource);
                    }
                    else if(!ingredient.getItemStack().isEmpty()){
                        createItemDiskParticle(pos, ingredient.getItemStack(), ingredient.getRevolutionOffset(), ringRadius, ringWidth, randomSource);
                    }
                }
            }*/
            // 更新内容を適用
            this.ingredients.set(i, ingredient);
            //changed = true;

            //装飾
            /*if(level != null && isCraftingInProgress && progressTimer >= 0 && progressTimer % 5 == 0 && randomSource.nextInt(8) == 0){
                // 材料の位置
                Vector2d cartPos = PolarCoordinates.getCartesianCoordinates(orbitalRadius, ingredient.getRevolutionOffset());
                Vec3 satPos = worldPosition.getCenter().add(cartPos.x(), OrreryCircleBoardRenderer.getProgressOffsetY(this, 0), cartPos.y());
                // 効果音を再生
                level.playSound(null,
                    satPos.x(), satPos.y(), satPos.z(),
                    SoundRegistry.RIPPLE.get(), SoundSource.BLOCKS, 0.5f, 1f);

                // パーティクルを生成
                StarRippleParticles particles = (StarRippleParticles)Minecraft.getInstance().particleEngine.createParticle(
                    ParticleRegistry.STAR_RIPPLE_PARTICLES.get(),
                    satPos.x(), satPos.y(), satPos.z(), 0,0,0
                );
                if(particles!=null) {
                    particles.setScale(randomSource.nextInt(4,6)*0.1f);
                }
            }*/
        }
    }

    private void dropIngredient(CircumstellarIngredient ingredient, boolean asStellarIngredientEntity) {
        if(level == null || level.isClientSide()){
            return;
        }
        ingredients.remove(ingredient);
        ItemStack resultItem = ingredient.getItemStack();

        Vector2d itemLocalPos = PolarCoordinates.getCartesianCoordinates(ingredient.getOrbitalRadius(), ingredient.getRevolutionOffset());
        Vec3 itemPos = worldPosition.getCenter().add(itemLocalPos.x(), 0, itemLocalPos.y());
        if(!asStellarIngredientEntity){
            // 素手のとき液体は落とさない(暫定)
            if(resultItem.isEmpty()){
                return;
            }
            //その場にアイテムを生成
            ItemEntity itemEntity = new ItemEntity(level, itemPos.x(), itemPos.y() + 1, itemPos.z(), resultItem);
            level.addFreshEntity(itemEntity);
        }else{
            // 軌道速度から速度
            double velocity = PolarCoordinates.getCircumSpeed(ingredient.getOrbitalRadius(), getCentForce(1));
            // 軌道位置から速度ベクトル
            double revOffset = ingredient.getRevolutionOffset();
            double velX = Math.sin(revOffset) * velocity;
            double velZ = Math.cos(revOffset) * velocity;
            Vec3 vector = new Vec3(-velX,0, velZ);//+((float) Math.PI * 0.5f)
            double heightOffset = OrreryCircleBoardRenderer.getProgressOffsetY(this, 1) - (StellarIngredientEntity.SIZE * 0.5);
            //その場にエンティティ生成
            StellarIngredientEntity stellarEntity = new StellarIngredientEntity(level, itemPos.add(0,heightOffset,0), vector, ingredient);
            level.addFreshEntity(stellarEntity);
        }
    }

    private void createDisk(CircumstellarIngredient ingredient) {
        if(level == null || level.isClientSide()){
            return;
        }
        ingredients.remove(ingredient);
        ingredient.setDisk_shaped(true);
        ingredients.add(ingredient);
    }

    //公転運行
    /*public static float calcRevProcess(float orbitRadius, float presentRevOffset, float centripetalForce, double partialTicks){
        return presentRevOffset + Mth.sqrt(centripetalForce*0.01f/orbitRadius)/orbitRadius;
    }*/

    public static TextureAtlasSprite getFluidSprite(Fluid fluid) {
        ResourceLocation fluidTexture = IClientFluidTypeExtensions.of(fluid).getStillTexture(new FluidStack(fluid,10));
        //例外処理
        if (fluidTexture == null) {
            Heliopause.LOGGER.debug("Rendering failure on getting Fluid Sprite.");
            // テクスチャが存在しない場合はエラーテクスチャを返す
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation("minecraft", "missing_texture"));
        }
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTexture);
    }
}
