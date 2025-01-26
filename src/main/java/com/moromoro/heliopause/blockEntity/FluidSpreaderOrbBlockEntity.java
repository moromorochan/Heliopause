package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.entity.OrreryInteractionOperatorEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.item.FluidBottle;
import com.moromoro.heliopause.particle.WhirlRingParticles;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.render.FluidSpreaderOrbBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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

public class FluidSpreaderOrbBlockEntity extends AbstractFluidOrbBlockEntity{

    //タンクおよび各材料の最大量
    public final static int MAX_INGREDIENT_AMOUNT = 2000;
    //星周天体へのインタラクト判定
    protected boolean circumstellarInteractionFlag = false;
    //パーティクルやレンダリングに使用する向心力
    protected float centripetalForce = 1;
    //アニメーションの滑らかな描画用変数
    //private float smoothedRingDensity;
    //リングの径 (幅の中央)
    private float ringRadius=1.0f;
    //リングの幅
    private float ringWidth = 0.0f;
    //内側どこまで寄せるか
    //protected final float innerRadius=Math.max(0.6f,ringRadius-1.8f);

    private final List<CircumstellarIngredient> ingredients = new ArrayList<>();

    //中心星にするアイテム
    public ItemStack centerItem = ItemStack.EMPTY;

    public FluidSpreaderOrbBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(), pos, state, MAX_INGREDIENT_AMOUNT);
    }

    public ItemStack getCenterItem() {
        return Objects.requireNonNullElse(centerItem, ItemStack.EMPTY);
    }

    public void setCenterItem(ItemStack item){
        centerItem = item;
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        // centerItemをNBTに書き込む
        CompoundTag itemNbt = new CompoundTag();
        centerItem.save(itemNbt);
        nbt.put("CenterItem", itemNbt);
        //IngredientsをNBTに書き込む
        ListTag ingredientsList = new ListTag();
        for (CircumstellarIngredient ingredient : ingredients) {
            CompoundTag ingredientsNbt = new CompoundTag();
            ingredient.writeToNBT(ingredientsNbt);
            ingredientsList.add(ingredientsNbt);
        }
        nbt.put("Ingredients",ingredientsList);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        // centerItemをNBTから読み込む
        if (nbt.contains("CenterItem")) {
            centerItem = ItemStack.of(nbt.getCompound("CenterItem"));
        }
        //IngredientsをNBTから読み込む
        ingredients.clear();
        ListTag ingredientsList = nbt.getList("Ingredients",10/*nbtのID*/);
        for (int i = 0; i < ingredientsList.size(); i++) {
            CompoundTag ingredientNbt = ingredientsList.getCompound(i);
            CircumstellarIngredient ingredient = CircumstellarIngredient.readFromNbt(ingredientNbt);
            ingredients.add(ingredient);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, FluidSpreaderOrbBlockEntity blockEntity) {
        boolean active = hasFluid() && !redStonePowered();
        RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
        //星周天体の生成
        for (CircumstellarIngredient ingredient : ingredients) {
            //オーブ
            if(!ingredient.getDisk_shaped()){
                ingredient.setRevolutionOffset(calcRevProcess(ingredient.getOrbitalRadius(),ingredient.getRevolutionOffset()));
                setChanged();
            }
            //円盤
            else {
                this.ringRadius = ingredient.getOrbitalRadius();
                if(!ingredient.getFluidStack().isEmpty()){
                    this.ringWidth = calcRingWidth(ingredient.getFluidStack().getAmount(), ringRadius);
                    createFluidDiskParticle(pos, ingredient.getFluidStack(), randomSource);
                    setChanged();
                }
                else if(!ingredient.getItemStack().isEmpty()){
                    this.ringWidth = calcRingWidth(ingredient.getFractableItemAmount(), ringRadius);
                    createItemDiskParticle(pos, ingredient.getItemStack(), randomSource);
                    setChanged();
                }
            }
        }
        updateRenderData();
        //レシピの動き
        if (active) {
            //smoothedRingDensity+=0.1f;
            //createParticle(level, pos, randomSource);
            //降水量25mmイメージ 20平方メートル・マイクラ内2時間で1B消費する確率
            if (randomSource.nextDouble() < 10 / 100f) {
                //this.mainTank.drain(5, FluidAction.EXECUTE);
            }
        }else {
            //smoothedRingDensity-=0.1f;
        }
        //インタラクト用の動き
        if(!level.isClientSide()){
            ServerLevel serverLevel = (ServerLevel) this.level;
            if (serverLevel != null) {
                List<ServerPlayer> players = serverLevel.players();
                for(Player player : players){
                    if (isInRange(player,10)) {
                        setOrreryInteractionOperator(player, serverLevel);
                    }
                }
            }
        }
    }
    //ブロックからの距離判定
    private boolean isInRange(Player player, float range) {
        return player.getEyePosition().distanceTo(getBlockPos().getCenter()) <= range;
    }

    private boolean redStonePowered() {
        return false;
    }

    private float calcRingWidth(int ingredientAmount, float ringRadius){
        return (float) Math.max((1f/16f), Math.sqrt(ingredientAmount*0.01f/Math.PI + ringRadius*ringRadius) - ringRadius);
    }

    public void setCircumstellars(Player player, InteractionHand hand, int operator) {
        if(level== null){return;}
        if(player==null){return;}
        //操作位置を取得
        Vec3 operatorPos = getOperatorPos(player);
        if(operatorPos == null){return;}
        //操作位置を極座標に変換
        Pair<Float,Float> polarPos = getPolarCoordinates(operatorPos);
        ItemStack inputStack = player.getItemInHand(hand);
        switch (operator){
            //右クリック時
            case 0:
                //瓶による操作(液体)
                if(inputStack.getItem() instanceof FluidBottle fluidBottle){
                    circumstellarInteractionFlag = true;
                    ItemStack fluidBottleStack = player.getItemInHand(hand);
                    UseOnContext context = new UseOnContext(level,player,InteractionHand.MAIN_HAND,fluidBottleStack,new BlockHitResult(operatorPos, Direction.UP,this.getBlockPos(),true));
                    fluidBottleStack.useOn(context);
                    //ブロックエンティティ内の液体を星周天体に渡す
                    if(!this.tank.isEmpty()){
                        FluidStack drainStack = this.tank.drain(MAX_INGREDIENT_AMOUNT,FluidAction.EXECUTE);
                        //jsonから球/円盤を取得
                        boolean isDiskShape = false;//(仮)
                        if(!isDiskShape){
                            int rotationRatio = (int)( (1000f/(float) drainStack.getAmount()) * Math.pow(polarPos.getA(), 1.5) * RandomSource.create().nextInt((int) 4,(int) 7));
                            ingredients.add(new CircumstellarIngredient(polarPos.getA(), polarPos.getB(), rotationRatio, drainStack.copy()));
                        }else {
                            ingredients.add(new CircumstellarIngredient(polarPos.getA(), polarPos.getB(), 0, drainStack.copy(), isDiskShape));
                        }
                    }
                    setChanged();
                    circumstellarInteractionFlag = false;
                }
                //アイテムによる操作(粉末/その他)
                else if(/*jsonから適合性を取得*/true){
                    //jsonからアイテムの流体としての量を決定
                    int itemVolume = 900;//(仮)
                    boolean stellarShape = true;//(仮)
                    ingredients.add(new CircumstellarIngredient(polarPos.getA(), polarPos.getB(), 0, inputStack.copyWithCount(1), itemVolume, stellarShape));
                    //効果音を再生
                    playItemSound(level,new BlockPos((int) operatorPos.x, (int) operatorPos.y, (int) operatorPos.z), inputStack);
                    //アイテムを消費
                    inputStack.shrink(1);
                    setChanged();
                }
                break;
            //左クリック時
            case 1:
                break;
            //例外
            default:
                break;
        }
    }

    public List<CircumstellarIngredient> getCircumstellars(){
        return ingredients;
    }

    public float getCentForce(){
        return centripetalForce;
    }

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

    private Vec3 getOperatorPos(Player player) {
        Vec3 playerLookVec = player.getLookAngle();
        Vec3 playerEyePos = player.getEyePosition();

        //軌道平面との交点を計算
        double playerToCursorDist = (getBlockPos().getCenter().y - playerEyePos.y) / playerLookVec.y;
        //カーソルが2ブロック以上離れているか、頭の反対側ならキャンセル
        if(playerToCursorDist < 0d || playerToCursorDist > 2d){return null;}
        Vec3 intersection = playerEyePos.add(playerLookVec.scale(playerToCursorDist));
        return intersection;
    }

    private Pair<Float,Float> getPolarCoordinates(Vec3 operatorPos) {
        Vec3 originPos = this.getBlockPos().getCenter();
        float r = (float) operatorPos.distanceTo(originPos);
        float theta = (float) Math.atan2(operatorPos.z - originPos.z, operatorPos.x - originPos.x);

        return new Pair<>(r, theta);
    }

    private boolean hasFluid() {
        return !this.getFluidInTank(0).isEmpty();
    }

    private void createFluidDiskParticle(BlockPos pos, FluidStack fluidStack, RandomSource randomSource) {
            //パーティクルの数を生成
            int particleAmount = Math.max(2,(int) (ringWidth*4f));//(int) (Math.pow(ringRadius,0.0));

            for (int i = 0; i < particleAmount; i++) {
                //位置を用意
                float centerPosX =(float) pos.getCenter().x;
                float centerPosY = (float) pos.getCenter().y + 1f/16f;
                float centerPosZ =(float) pos.getCenter().z;

                //パーティクルに適用する回転オフセットをランダムから用意
                float randAngle = (float) Math.toRadians(randomSource.nextFloat()*360);
                //パーティクルに適用する半径をランダムから用意
                float orbitWidth = ringWidth;
                float orbitError =
                        ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                                *(1)
                                * orbitWidth;
                float orbitRadius = Math.max(ringRadius + orbitError, 0);

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
                    float centripetalForce = 0.1f; //TODO パーティクルと向心力の値を揃える仕組みを用意する
                    float orbitSpeed = Mth.sqrt(centripetalForce/orbitRadius)/orbitRadius;
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

    private void createItemDiskParticle(BlockPos pos, ItemStack itemStack, RandomSource randomSource) {
        //パーティクルの数を生成
        int particleAmount = Math.max(2,(int) (ringWidth*4f));//(int) Math.pow(ringRadius,0.9);

        for (int i = 0; i < particleAmount; i++) {
            //位置を用意
            float centerPosX =(float) pos.getCenter().x;
            float centerPosY = (float) pos.getCenter().y + 1f/16f;
            float centerPosZ =(float) pos.getCenter().z;

            //パーティクルに適用する回転オフセットをランダムから用意
            float randAngle = (float) Math.toRadians(randomSource.nextFloat()*360);
            //パーティクルに適用する半径をランダムから用意
            float orbitWidth = ringWidth;
            float orbitError =
                ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                    *(1)
                    * orbitWidth;
            float orbitRadius = Math.max(ringRadius + orbitError, 0);

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
        int tintAlpha=FastColor.ABGR32.alpha(texColor);
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

    @Override
    protected void updateRenderData() {
        FluidSpreaderOrbBlockRenderer.updateData(this.getBlockPos(), this.getCircumstellars());
    }
    @Override
    protected void removeRenderData() {
        FluidSpreaderOrbBlockRenderer.removeData(this.getBlockPos());
    }

    public void playItemSound(Level level,BlockPos blockPos,ItemStack itemStack){
        SoundEvent placeSound = SoundEvents.SAND_STEP;
        //タグがある場合
        if (/*!itemStack.is(カスタムタグ) && */itemStack.getItem() instanceof BlockItem blockItem) {
            placeSound = blockItem.getBlock().getSoundType(blockItem.getBlock().defaultBlockState()).getStepSound();
        }
        level.playSound(null,blockPos, placeSound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void setOrreryInteractionOperator(Player player, ServerLevel level) {
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
        if(intersection.distanceTo(blockPos.getCenter())<=5){
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
    }
}