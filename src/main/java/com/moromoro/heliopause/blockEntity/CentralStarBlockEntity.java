package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.particle.WhirlRingParticles;
import com.moromoro.heliopause.recipe.OrreryIngredient;
import com.moromoro.heliopause.recipe.orreryWhirling.OrreryWhirlingRecipe;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import com.moromoro.heliopause.render.CentralStarBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;

import static java.lang.Math.max;

public class CentralStarBlockEntity extends AbstractFluidConcealBlockEntity {

    //直前の描画時刻を格納
    private long lastFrameTime;
    //回転の進捗を格納
    protected float rotationOffset;
    //浮き沈みの進捗を格納
    private float waveOffset;
    //各材料の最大量
    public final static int MAX_INGREDIENT_AMOUNT = 2000;
    //星周天体へのインタラクト判定
    protected boolean circumstellarInteractionFlag = false;
    //重力定数
    protected static final float GRAVITATIONAL_CONST = 0.03f;
    //パーティクルやレンダリングに使用する向心力
    protected float centripetalForce = 5;
    //リングの径 (幅の中央)
    protected float ringRadius=1.0f;
    //リングの幅
    protected float ringWidth = 0.0f;
    //周転する天体の配列
    private final List<CircumstellarIngredient> ingredients = new ArrayList<>();

    //中心星の見た目
    private BlockState centerBlockState = Blocks.AIR.defaultBlockState();
    //中心星の内容
    private OrreryIngredient centerIngredient = OrreryIngredient.empty();

    private final static int CHECK_COUNT = 20;
    //レシピチェックタイマー
    private int tickCounter = 0;
    //行程進捗タイマー
    private int processTimer = 0;

    public CentralStarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CENTRAL_STAR_BE.get(), pos, state, MAX_INGREDIENT_AMOUNT);
    }

    public CentralStarBlockEntity(BlockEntityType blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, MAX_INGREDIENT_AMOUNT);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if(!centerBlockState.isAir()){
            nbt.put("CenterBlockState", NbtUtils.writeBlockState(centerBlockState));
            //nbt.put("CenterIngredient", new OrreryIngredient(centerBlockState.getBlock()).toNBT());
        }else{
            nbt.put("CenterIngredient",centerIngredient.toNBT());
        }
        nbt.put("Ingredients",circumStellarToNbt());
    }

    private ListTag circumStellarToNbt(){
        //IngredientsをNBTに書き込む
        ListTag ingredientsList = new ListTag();
        for (int i = 0; i < ingredients.size(); i++) {
            CircumstellarIngredient ingredient = ingredients.get(i);
            if(!ingredient.isValid()) {
                ingredients.remove(ingredient);
                continue;
            }
            CompoundTag ingredientsNbt = new CompoundTag();
            ingredientsList.add(CircumstellarIngredient.writeToNBT(ingredientsNbt, ingredient));
        }
        return ingredientsList;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        lastFrameTime = System.nanoTime();
        if (level != null) {
            if (!level.isClientSide) {
                updateRenderData();
            }
            // centerBlockStateをNBTから読み込む
            if (nbt.contains("CenterBlockState")) {
                centerBlockState = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK),nbt.getCompound("CenterBlockState"));
            }
        }
        // centerIngredientをNBTから読み込む
        //centerIngredient = ;
        if (nbt.contains("CenterIngredient")) {
            setCenterIngredient(OrreryIngredient.fromNBT(nbt.getCompound("CenterIngredient")));
        }else{
            //setCenterIngredient(new OrreryIngredient(centerBlockState.getBlock()));
        }
        // IngredientsをNBTから読み込む
        ingredients.clear();
        ListTag ingredientsList = nbt.getList("Ingredients",10/*nbtのID*/);
        for (int i = 0; i < ingredientsList.size(); i++) {
            CompoundTag ingredientNbt = ingredientsList.getCompound(i);
            CircumstellarIngredient ingredient = CircumstellarIngredient.readFromNbt(ingredientNbt);
            if(!ingredient.isValid()) {
                ingredients.remove(ingredient);
                continue;
            }
            ingredients.add(ingredient);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("CenterBlockState", NbtUtils.writeBlockState(centerBlockState));
        tag.put("CenterIngredient", centerIngredient.toNBT());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void updateRenderData() {
        CentralStarBlockRenderer.updateData(this.getBlockPos(), ingredients.stream().filter(CircumstellarIngredient::isValid).toList());
    }

    protected void removeRenderData() {
        CentralStarBlockRenderer.removeData(this.getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            removeRenderData();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            removeRenderData();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            updateRenderData();
        }
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }

    //レンダリング時のオフセットを設定
    public Vec3 centerOffset() {
        return new Vec3(0,0,0);
    }

    public float getRotationOffset() {
        return rotationOffset;
    }

    public void setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset%360;
    }

    public float getWaveOffset() {
        return waveOffset;
    }

    public void setWaveOffset(float waveOffset) {
        this.waveOffset = waveOffset%360;
    }

    //見た目ブロックのgetterとsetter
    public BlockState getCenterBlockState() {
        return centerBlockState;
    }

    public void setCenterBlockState(BlockState centerBlockState) {
        this.centerBlockState = centerBlockState;
        setChanged();
    }

    public OrreryIngredient getCenterIngredient() {
        return this.centerIngredient;
    }

    public void setCenterIngredient(OrreryIngredient centerIngredient) {
        this.centerIngredient = centerIngredient;
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, CentralStarBlockEntity blockEntity) {
        boolean active = !redStonePowered();
        RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
        if(level==null){
            return;
        }

        //星周天体の生成
        setIngredientsBehavior(level, pos, randomSource);

        if(!level.isClientSide()){
            //インタラクト用の動き
            /*ServerLevel serverLevel = (ServerLevel) this.level;
            if (serverLevel != null) {
                //インタラクト用エンティティを生成
                List<ServerPlayer> players = serverLevel.players();
                for(Player player : players){
                    if (isInRange(player,10)) {
                        setOrreryInteractionOperator(player, serverLevel);
                    }
                }
                //インタラクト用ブロックを生成
                setOrreryInteractionSpaceBlock(serverLevel,pos,10);
            }*/

            if(active){
                tickCounter++;
                if(tickCounter >= CHECK_COUNT){
                    //レシピチェック
                    //仮想コンテナの作成
                    SimpleContainer virtualContainer = new SimpleContainer(1);
                    //nbtを送るための容れ物として仮想アイテムを用意
                    ItemStack stack = new ItemStack(BlockRegistry.CENTRAL_STAR.get());
                    CompoundTag nbt = stack.getOrCreateTag();
                    nbt.put("CenterIngredient", getCenterIngredient().toNBT());
                    nbt.put("Ingredients",circumStellarToNbt());
                    virtualContainer.setItem(0, stack);

                    Optional<OrreryWhirlingRecipe> optionalRecipe =
                        level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.ORRERY_WHIRLING.get(), virtualContainer, level);

                    if (optionalRecipe.isPresent()) {
                        OrreryWhirlingRecipe recipe = optionalRecipe.get();
                        int processingTime = recipe.getTime();
                        if(processTimer >= processingTime) {
                            OrreryIngredient resultIngredient = recipe.getResult();
                            applyResult(resultIngredient);
                            //consumeIngredients(recipe);
                        }
                        processTimer += tickCounter;
                        tickCounter = CHECK_COUNT - Math.min( CHECK_COUNT, processingTime - processTimer);
                    }else{
                        tickCounter = 0;
                        processTimer = 0;
                    }
                }
            }
        }
    }

    private void applyResult(OrreryIngredient resultIngredient) {
        switch (resultIngredient.getType()){
            case "block" :{
                Block block = ForgeRegistries.BLOCKS.getValue(resultIngredient.getId());
                if(block == null){
                    setCenterIngredient(OrreryIngredient.empty());
                    //TODO: エラー
                    break;
                }
                setCenterBlockState(block.defaultBlockState());
                //setCenterIngredient(new OrreryIngredient(block));
                break;
            }
            case "fluid" :{
                setCenterBlockState(Blocks.AIR.defaultBlockState());
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(resultIngredient.getId());
                if(fluid == null)
                {
                    setCenterIngredient(OrreryIngredient.empty());
                    //TODO: エラー
                    break;
                }
                setCenterIngredient(new OrreryIngredient(new FluidStack(fluid,resultIngredient.getAmount())));
                break;
            }
            case "item" :{
                setCenterBlockState(Blocks.AIR.defaultBlockState());
                Item item = ForgeRegistries.ITEMS.getValue(resultIngredient.getId());
                if(item == null)
                {
                    setCenterIngredient(OrreryIngredient.empty());
                    //TODO: エラー
                    break;
                }
                setCenterIngredient(new OrreryIngredient(new ItemStack(item,resultIngredient.getAmount())));
                break;
            }
            default:{
                setCenterBlockState(Blocks.AIR.defaultBlockState());
                setCenterIngredient(OrreryIngredient.empty());
                Heliopause.LOGGER.error("Error unknown result type");
                //TODO: エラー
                break;
            }
        }
        setChanged();
    }

    /*private void consumeIngredients(OrreryWhirlingRecipe recipe) {
        // レシピ側の要求材料リストを、CircumstellarIngredient型から OrreryIngredient 型に変換して取得
        List<OrreryIngredient> requiredIngredients = recipe.getOrreryIngredients();

        // 利用可能なスロット（CircumstellarIngredient型）のコピーを作成し、数量が少ない順にソートする
        List<CircumstellarIngredient> availableSlots = new ArrayList<>(this.ingredients);
        availableSlots.sort(Comparator.comparingInt(a -> a.getIngredient().getAmount()));

        // 各要求材料に対して、単一スロットのみで消費を試みる
        for (OrreryIngredient req : requiredIngredients) {
            boolean slotFound = false;
            Iterator<CircumstellarIngredient> iterator = availableSlots.iterator();
            while (iterator.hasNext()) {
                CircumstellarIngredient slot = iterator.next();
                OrreryIngredient available = slot.getIngredient();
                // 材料と要求量の確認
                if (OrreryWhirlingRecipe.matchesComponent(req, available)) {
                    //消費
                    slot.consume(req.getAmount());
                    //availableSlotsから除く
                    iterator.remove();
                    slotFound = true;
                    //消費後が0以下なら削除
                    if (slot.getIngredient().getAmount() <= 0) {
                        this.ingredients.remove(slot);
                    }
                }
            }

            //レシピチェックすり抜け時の処理
            if (!slotFound) {
                Heliopause.LOGGER.error("Error lack of ingredient");
            }
        }
        // 材料消費後、ブロックエンティティの状態更新を反映
        setChanged();
    }*/

    protected void setIngredientsBehavior(Level level, BlockPos pos, RandomSource randomSource) {
        //星周天体の生成
        for (CircumstellarIngredient ingredient : ingredients) {
            float orbitalRadius = ingredient.getOrbitalRadius();
            //オーブ
            if(!ingredient.isDiskShaped()){
                //公転を計算
                ingredient.setRevolutionOffset(calcRevProcess(orbitalRadius, ingredient.getRevolutionOffset()));
                setChanged();
            }
            //円盤
            else {
                this.ringRadius = ingredient.getOrbitalRadius();
                if(ingredient.isValid()){
                    this.ringWidth = calcRingWidth(Math.max(ingredient.getFluidStack().getAmount(), ingredient.getItemStack().getCount() * 1000), ringRadius);
                    //内側の公転速度と外側の公転速度を用意
                    float innerRevProcess = calcRevProcess(orbitalRadius-(this.ringWidth/2),0);
                    float outerRevProcess = calcRevProcess(orbitalRadius-(this.ringWidth/2),0);
                    //広がりを設定(Degree)
                    /*if(ingredient.getRotationRatio()<360){
                        //差を用意
                        ingredient.setRotationRatio(ingredient.getRotationRatio()+1+Math.round(Math.abs(innerRevProcess-outerRevProcess)));
                    }else if(ingredient.getRotationRatio()>360){
                        ingredient.setRotationRatio(360);
                    }*/

                    //公転
                    ingredient.setRevolutionOffset(calcRevProcess(orbitalRadius, ingredient.getRevolutionOffset()));
                    setChanged();

                    //パーティクル生成
                    if(!ingredient.getFluidStack().isEmpty()){
                        createFluidDiskParticle(pos, ingredient.getFluidStack(), ingredient.getRevolutionOffset(), /*ingredient.getRotationRatio()*/360, randomSource);
                    }
                    else if(!ingredient.getItemStack().isEmpty()/* && ingredient.getFractableItemAmount()!=0*/){
                        createItemDiskParticle(pos, ingredient.getItemStack(), ingredient.getRevolutionOffset(), /*ingredient.getRotationRatio()*/360, randomSource);
                    }
                }
            }
        }
        updateRenderData();
    }

    //ブロックからの距離判定
    protected boolean isInRange(Player player, float range) {
        return true;//player.getEyePosition().distanceTo(getBlockPos().getCenter()) <= range;
    }

    protected boolean redStonePowered() {
        return false;
    }

    public static float calcRingWidth(int ingredientAmount, float ringRadius){
        return (float) max((1f/16f), Math.sqrt(ingredientAmount*0.005f/Math.PI + ringRadius*ringRadius) - ringRadius);
    }

//    public void setCircumstellars(Player player, InteractionHand hand, int operatorPosX, int operatorPosZ) {
//        if(level== null){return;}
//        if(player==null){return;}
//        //操作位置を取得
//        Vec3 operatorPos = this.getBlockPos().offset(operatorPosX,0,operatorPosZ).getCenter();//getOperatorPos(player);
//        //if(operatorPos == null){return;}
//        //操作位置を極座標に変換
//        Pair<Float,Float> polarPos = getPolarCoordinates(operatorPos);
//        ItemStack inputStack = player.getItemInHand(hand);
//        //対象スロットを取得
//        int SlotId = CircumstellarIngredient.getSlotFromRadius(polarPos.getA());
//
//        // 既存の素材をスロットから取得
//        Optional<CircumstellarIngredient> existingIngredientOpt = ingredients.stream()
//            .filter(i -> i.getSlotId() == SlotId)
//            .findFirst();
//        CircumstellarIngredient newIngredient = CircumstellarIngredient.empty(SlotId);//new OrreryIngredient(inputStack.copyWithCount(1));
//
//        /*switch (operator){
//            //右クリック時
//            case 0:*/
//                //瓶による操作(液体)
//                /*if(inputStack.getItem() instanceof FluidBottle fluidBottle){
//                    circumstellarInteractionFlag = true;
//                    ItemStack fluidBottleStack = player.getItemInHand(hand);
//                    UseOnContext context = new UseOnContext(level,player,InteractionHand.MAIN_HAND,fluidBottleStack,new BlockHitResult(operatorPos, Direction.UP,this.getBlockPos(),true));
//                    fluidBottleStack.useOn(context);
//                    //ブロックエンティティ内の液体を星周天体に渡す
//                    if(!this.tank.isEmpty()){
//                        FluidStack drainStack = this.tank.drain(MAX_INGREDIENT_AMOUNT,FluidAction.EXECUTE);
//                        //jsonから球/円盤を取得
//                        boolean isDiskShape = false;//(仮)
//                        if(!isDiskShape){
//                            int rotationRatio = -(int)( (MAX_INGREDIENT_AMOUNT/(float) drainStack.getAmount()) + RandomSource.create().nextInt((int) 4,(int) 7));
//                            newIngredient = (new CircumstellarIngredient(SlotId, polarPos.getB(), rotationRatio, new OrreryIngredient(drainStack), false));
//                        }else {
//                            newIngredient = (new CircumstellarIngredient(SlotId, polarPos.getB(), 0, new OrreryIngredient(drainStack), true));
//                        }
//                    }
//                    setChanged();
//                    circumstellarInteractionFlag = false;
//                }*/
//                //アイテムによる操作(粉末/その他)
//                /*else */if(/*jsonから適合性を取得*/true){
//                    //jsonからアイテムの流体としての量を決定
//                    //int itemVolume = 900;//(仮)
//                    boolean stellarShape = false;//(仮)
//                    newIngredient = (new CircumstellarIngredient(SlotId, polarPos.getB(), new OrreryIngredient(inputStack.copyWithCount(1)), stellarShape));
//                    //効果音を再生
//                    playItemSound(level,new BlockPos((int) operatorPos.x, (int) operatorPos.y, (int) operatorPos.z), inputStack);
//                    //アイテムを消費
//                    inputStack.shrink(1);
//                    setChanged();
//                }
//
//                // スロットに既に素材がある場合、種類をチェック
//                if (existingIngredientOpt.isPresent()&& newIngredient.isValid()) {
//                    CircumstellarIngredient existingIngredient = existingIngredientOpt.get();
//
//                    // 種類が違う場合、操作をキャンセル
//                    if (!existingIngredient.getIngredient().isSame(newIngredient.getIngredient())) {
//                        return;
//                    }
//                    // 種類が同じなら数量を追加
//                    existingIngredient.addAmount(newIngredient.getAmount());
//                    setChanged();
//                    return;
//                }
//                //スロットが空の場合、新たに追加
//                else{
//                    ingredients.add(newIngredient);
//                }
//            /*    break;
//            //左クリック時
//            case 1:
//                break;
//            //例外
//            default:
//                break;
//        }*/
//        //スロット順にソート
//        ingredients.sort(Comparator.comparing(CircumstellarIngredient::getOrbitalRadius));
//    }

    public List<CircumstellarIngredient> getCircumstellars(){
        return ingredients;
    }
    //向心力
    public float getCentForce(){
        return centripetalForce;
    }
    //公転運行
    private float calcRevProcess(float orbitRadius, float presentRevOffset){
        return presentRevOffset + Mth.sqrt(centripetalForce/orbitRadius)/orbitRadius;
    }

    //直接的なインタラクトを禁止する

    @Override
    public int getTanks() {
        //if(circumstellarInteractionFlag)
        return super.getTanks();
        //return 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if(circumstellarInteractionFlag)
            return super.getFluidInTank(tank);
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        //if(circumstellarInteractionFlag)
        return super.getTankCapacity(tank);
        //return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if(circumstellarInteractionFlag)
            return super.isFluidValid(tank,stack);
        return false;
    }


    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if(circumstellarInteractionFlag)
            return super.fill(resource,action);
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if(circumstellarInteractionFlag)
            return super.drain(resource,action);
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if(circumstellarInteractionFlag)
            return super.drain(maxDrain,action);
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && !circumstellarInteractionFlag) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && !circumstellarInteractionFlag) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }


    /*private Vec3 getOperatorPos(Player player) {
        Vec3 playerLookVec = player.getLookAngle();
        Vec3 playerEyePos = player.getEyePosition();

        //軌道平面との交点を計算
        double playerToCursorDist = (getBlockPos().getCenter().y - playerEyePos.y) / playerLookVec.y;
        //カーソルが2ブロック以上離れているか、頭の反対側ならキャンセル
        if(playerToCursorDist < 0d || playerToCursorDist > 2d){return null;}
        Vec3 intersection = playerEyePos.add(playerLookVec.scale(playerToCursorDist));
        return intersection;
    }*/

    protected Pair<Float,Float> getPolarCoordinates(Vec3 operatorPos) {
        Vec3 originPos = this.getBlockPos().getCenter();
        float r = (float) operatorPos.distanceTo(originPos);
        float theta = 180 + (float) Math.toDegrees(Math.atan2(operatorPos.x - originPos.x, operatorPos.z - originPos.z));

        return new Pair<>(r, theta);
    }

    public static Vec3 getCartesianCoordinates(float radius, float theta){
        //Vec3 originPos = this.getBlockPos().getCenter();
        float x = (float) (radius * Math.cos(Math.toRadians(90 - theta)));
        float z = (float) (radius * Math.sin(Math.toRadians(90 - theta)));

        return new Vec3(x,0,z);
    }

    protected boolean hasFluid() {
        return !this.getFluidInTank(0).isEmpty();
    }

    private void createFluidDiskParticle(BlockPos pos, FluidStack fluidStack, float revolution , int spread, RandomSource randomSource) {
        //パーティクルの数を生成
        int particleAmount = max(2,(int) (ringWidth*4f));//(int) (Math.pow(ringRadius,0.0));

        for (int i = 0; i < particleAmount; i++) {
            //位置を用意
            float centerPosX =(float) pos.getCenter().x;
            float centerPosY = (float) pos.getCenter().y + 1f/16f;
            float centerPosZ =(float) pos.getCenter().z;

            //パーティクルに適用する回転オフセットをランダムから用意
            float spreadRandom = (float) ((randomSource.nextFloat()-0.5f) * spread);
            float randAngle = revolution + 180 + spreadRandom;
            //パーティクルに適用する半径をランダムから用意
            float orbitWidth = ringWidth * (1 + (2 - Math.abs(spreadRandom/90f)) * (1-(float) spread /360));
            float orbitError =
                ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                    *(1)
                    * orbitWidth;
            float orbitRadius = max(ringRadius + orbitError, 0);

            //パーティクルに適用する色を液体から用意
            int[] color = getFluidRingColor(fluidStack.getFluid(),orbitRadius,pos);

            //パーティクルを生成
            WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                centerPosX , centerPosY, centerPosZ , 0, 0, 0
            );

            //パーティクルに値を入れる
            if (ringParticle != null) {

                if(this.level!=null&&!this.level.isClientSide())
                {
                    ringParticle.setLightIntensity(fluidStack.getFluid().getFluidType().getLightLevel());
                }

                ringParticle.setOrbitalElements(new Vec3(centerPosX,centerPosY,centerPosZ),(float) Math.toRadians(-90),0,randAngle,orbitRadius,centripetalForce);
                //パーティクルの大きさをリングの端からの距離に合わせる
                ringParticle.setPixelBasedSize(orbitWidth*0.5f-Math.abs(orbitError));

                ringParticle.setColor(color[0],color[1],color[2]);
            }

            //信号が入っていない場合、散布パーティクルを生成する
                /*if(!redStonePowered()){
                    //軌道上の位置を取得
                    float orbitPosX = (float) (centerPosX + Math.cos(randAngle) * orbitRadius);
                    float orbitPosZ = (float) (centerPosZ + Math.sin(randAngle) * orbitRadius);
                    //軌道速度(パーティクルの初速度にするもの)を取得
                    float DEFAULT_CENT_FORCE = 0.1f;
                    float orbitSpeed = Mth.sqrt(DEFAULT_CENT_FORCE/orbitRadius)/orbitRadius;
                    float orbitVelX = (float) (Math.cos(randAngle-Math.toRadians(90)) * orbitSpeed);
                    float orbitVelZ = (float) (Math.sin(randAngle-Math.toRadians(90)) * orbitSpeed);
                    FluidSpreadParticles spreadParticle = (FluidSpreadParticles) Minecraft.getInstance().particleEngine.createParticle(
                            ParticleRegistry.FLUID_SPREAD_PARTICLES.get(),
                            orbitPosX,centerPosY-0.1f,orbitPosZ,orbitVelX,0,orbitVelZ
                    );
                    //パーティクルに値を入れる
                    if (spreadParticle != null) {
                        spreadParticle.setColor(color[0],color[1],color[2]);
                    }
                }*/
        }
    }

    private void createItemDiskParticle(BlockPos pos, ItemStack itemStack, float revolution , int spread, RandomSource randomSource) {
        //パーティクルの数を生成
        int particleAmount = max(2,(int) (ringWidth * ((float) spread/360) * 4f));//(int) Math.pow(ringRadius,0.9);

        for (int i = 0; i < particleAmount; i++) {
            //位置を用意
            float centerPosX =(float) pos.getCenter().x;
            float centerPosY = (float) pos.getCenter().y + 1f/16f;
            float centerPosZ =(float) pos.getCenter().z;

            //パーティクルに適用する回転オフセットをランダムから用意
            float spreadRandom = (float) ((randomSource.nextFloat()-0.5f) * spread);
            float randAngle = revolution + 180 + spreadRandom;
            //パーティクルに適用する半径をランダムから用意
            float orbitWidth = ringWidth * (1 + (2 - Math.abs(spreadRandom/90f)) * (1-(float) spread /360));
            float orbitError =
                ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                    *(1)
                    * orbitWidth;
            float orbitRadius = max(ringRadius + orbitError, 0);

            //パーティクルに適用する色を液体から用意
            int[] color = getItemRingColor(itemStack,orbitRadius,pos);
            //透明部分は飛ばす
            if (Arrays.equals(color, new int[]{-1, -1, -1})){
                continue;
            }else{

                //パーティクルを生成
                WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                    ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                    centerPosX , centerPosY, centerPosZ , 0, 0, 0
                );

                //パーティクルに値を入れる
                if (ringParticle != null) {

                    if(this.level!=null&&!this.level.isClientSide())
                    {
                        if(itemStack.getItem() instanceof BlockItem blockItem){
                            ringParticle.setLightIntensity(blockItem.getBlock().defaultBlockState().getLightEmission());
                        }else {
                            ringParticle.setLightIntensity(itemStack.getItem().isFoil(itemStack)?15:0);
                        }
                    }

                    ringParticle.setOrbitalElements(new Vec3(centerPosX,centerPosY,centerPosZ),(float) Math.toRadians(-90),0,randAngle,orbitRadius,1);
                    //パーティクルの大きさをリングの端からの距離に合わせる
                    ringParticle.setPixelBasedSize(orbitWidth*0.5f-Math.abs(orbitError));

                    ringParticle.setColor(color[0],color[1],color[2]);
                }
            }
        }
    }

    private void createFluidDiskParticle(BlockPos pos, FluidStack fluidStack, RandomSource randomSource){
        createFluidDiskParticle(pos, fluidStack, 0, 360, randomSource);
    }

    private void createItemDiskParticle(BlockPos pos, ItemStack itemStack, RandomSource randomSource){
        createItemDiskParticle(pos, itemStack, 0, 360, randomSource);
    }

    private int[] getFluidRingColor(Fluid fluid, float orbitRadius, BlockPos pos) {

        //位置からシード固定のランダムソースを作成
        RandomSource randomSource = RandomSource.create(pos.asLong());

        //液体からスプライトを取得
        TextureAtlasSprite sprite = getFluidSprite(fluid);

        //テクスチャのピクセルの色を取得
        int texColor = sprite.getPixelRGBA(0,randomSource.nextInt(0,16),(int)Math.floor(orbitRadius*19f)%16);

        int tintColor = IClientFluidTypeExtensions.of(fluid).getTintColor();
        if(tintColor == -1){
            tintColor = 0xFFFFFF;
        }
        int tintAlpha= FastColor.ABGR32.alpha(texColor);
        float halfTint = (tintAlpha/255f);
        texColor = FastColor.ARGB32.lerp(halfTint,0xFFFFFF,texColor);
        //ティントカラーを適用
        int color = FastColor.ARGB32.multiply(
            FastColor.ABGR32.color(
                255,
                FastColor.ABGR32.red(texColor),
                FastColor.ABGR32.green(texColor),
                FastColor.ABGR32.blue(texColor)
            ),
            tintColor
        );

        int red = FastColor.ABGR32.red(color);
        int green = FastColor.ABGR32.green(color);
        int blue = FastColor.ABGR32.blue(color);

        return new int[]{red,green,blue};
    }

    private int[] getItemRingColor(ItemStack itemStack, float orbitRadius, BlockPos pos) {
        //位置からシード固定のランダムソースを作成
        RandomSource randomSource = RandomSource.create(pos.asLong());

        //アイテムからスプライトを取得
        TextureAtlasSprite sprite = getItemSprite(itemStack);

        //テクスチャのピクセルの色を取得
        int texColor = sprite.getPixelRGBA(0, 4+(int)Math.floor(orbitRadius*19)%8, randomSource.nextInt(4,12));
        //ティントを用意
        int tintColor = Minecraft.getInstance().getItemColors().getColor(itemStack,0);
        if(tintColor == -1){
            tintColor = 0xFFFFFF;
        }
        int tintAlpha=FastColor.ABGR32.alpha(texColor);
        //透明度の高い部分は-1を返す(スキップ)
        if(tintAlpha <= 64){
            return new int[]{-1,-1,-1};
        }
        float halfTint = (tintAlpha/255f);
        texColor = FastColor.ARGB32.lerp(halfTint,0x808080,texColor);
        //ティントカラーを適用
        int color = FastColor.ARGB32.multiply(
            texColor,
            FastColor.ABGR32.color(
                255,
                FastColor.ABGR32.red(tintColor),
                FastColor.ABGR32.green(tintColor),
                FastColor.ABGR32.blue(tintColor)
            )
        );

        int red = FastColor.ABGR32.red(color);
        int green = FastColor.ABGR32.green(color);
        int blue = FastColor.ABGR32.blue(color);

        return new int[]{255-red,255-green,255-blue};
    }

    //液体の色を取得
    private int[] getFluidColor(Fluid fluid) {
        //液体のidを取得
        String fluidName = ForgeRegistries.FLUIDS.getKey(fluid).toString();
        if(ConfigHolder.FLUID_COLORS.containsKey(fluidName)){
            int cfgColor = ConfigHolder.FLUID_COLORS.get(fluidName).get();
            float[] cfgRGB =
                new float[]{
                    FastColor.ARGB32.red(cfgColor),
                    FastColor.ARGB32.green(cfgColor),
                    FastColor.ARGB32.blue(cfgColor)
                };
            return new int[]{(int)(cfgRGB[0]*255),(int)(cfgRGB[1]*255),(int)(cfgRGB[2]*255)};
        }
        //configに無ければ、テクスチャから生成

        //液体からスプライトを取得
        TextureAtlasSprite sprite = getFluidSprite(fluid);

        //テクスチャのピクセルの色とティントカラーを取得
        int texColor = sprite.getPixelRGBA(0, 7, 7);
        float[] texRGB = new float[]{FastColor.ABGR32.red(texColor),FastColor.ABGR32.green(texColor),FastColor.ABGR32.blue(texColor)};

        //乗算
        float red = texRGB[0];
        float green = texRGB[1];
        float blue = texRGB[2];

        return new int[]{(int)(red*255),(int)(green*255),(int)(blue*255)};
    }

    private TextureAtlasSprite getFluidSprite(Fluid fluid) {
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

    private TextureAtlasSprite getItemSprite(ItemStack itemStack){
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(itemStack).getParticleIcon();
    }

    public void playItemSound(Level level,BlockPos blockPos,ItemStack itemStack){
        SoundEvent placeSound = SoundEvents.SAND_STEP;
        //タグがある場合
        if (/*!itemStack.is(カスタムタグ) && */itemStack.getItem() instanceof BlockItem blockItem) {
            placeSound = blockItem.getBlock().getSoundType(blockItem.getBlock().defaultBlockState()).getStepSound();
        }
        level.playSound(null,blockPos, placeSound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

   /* protected void setOrreryInteractionOperator(Player player, ServerLevel level) {
        //視線を取得
        BlockPos blockPos = this.getBlockPos();
        Vec3 playerLookVec = player.getLookAngle();
        Vec3 playerEyePos = player.getEyePosition();

        //軌道平面との交点を計算
        double playerToCursorDist = (blockPos.getCenter().y - playerEyePos.y) / playerLookVec.y;
        //カーソルが2ブロック以上離れているか、頭の反対側ならキャンセル
        if(playerToCursorDist < 0d || playerToCursorDist > 2d){return;}
        Vec3 intersection = playerEyePos.add(playerLookVec.scale(playerToCursorDist));

        //距離を確認
        double operator2BlockDistance = intersection.distanceTo(blockPos.getCenter());
        if(operator2BlockDistance >= 1 && operator2BlockDistance < 5){
            //既存のブロックエンティティを探す
            boolean entityExists = level.getEntitiesOfClass(OrreryInteractionOperatorEntity.class, new AABB(intersection,intersection).inflate(1))
                .stream().anyMatch(entity -> entity.getPlayerUUID().equals(player.getUUID()));
            if(!entityExists){
                //無ければ作成
                OrreryInteractionOperatorEntity newEntity = new OrreryInteractionOperatorEntity(level, blockPos, player.getUUID());
                newEntity.setPos(intersection.add(0,-0.1f,0));
                level.addFreshEntity(newEntity);
            }else{
                //あれば延命
                level.getEntitiesOfClass(OrreryInteractionOperatorEntity.class, new AABB(intersection,intersection).inflate(1))
                    .stream().filter(entity -> entity.getPlayerUUID().equals(player.getUUID()))
                    .forEach(OrreryInteractionOperatorEntity::notifyUpdate);
            }
        }
    }*/
}
