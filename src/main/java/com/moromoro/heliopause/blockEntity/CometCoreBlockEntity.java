package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.ItemRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.render.CometCoreBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.List;

public class CometCoreBlockEntity extends AbstractCoreBlockEntity{
    public CometCoreBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.COMET_CORE_BE.get(), pos, state, 1000);
    }

    @Override
    protected void updateRenderData() {
        CometCoreBlockRenderer.updateData(this.getBlockPos(),this.getFluidInTank(0));
    }

    @Override
    protected void removeRenderData() {
        CometCoreBlockRenderer.removeData(this.getBlockPos());
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, CometCoreBlockEntity blockEntity) {
        boolean active = hasFluid() && !redStonePowered();
        if(active){
            RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
            createParticle(blockState,level,pos,randomSource);
            //降水量25mmイメージ 20平方メートル・マイクラ内2時間で1B消費する確率
            if(randomSource.nextDouble()<10/100f){
                this.mainTank.drain(5,FluidAction.EXECUTE);
            }
            //有効範囲を検索 1は無効な座標
            int[][] map = new int[][]{
                    { 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 1 }
            };
            //Heliopause.LOGGER.debug("------------------");
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    //値が1ならスキップする
                    if (map[i][j]==1) {continue;}

                    int temp = getBelow(level,
                            new BlockPos(
                            pos.getX()+(i-2),
                            pos.getY()+map[i][j],
                            pos.getZ()+(j-2))
                            ,-12);
                    map[i][j] = temp;
                }
                //Heliopause.LOGGER.debug(Arrays.toString(map[i]));
            }
        }
    }

    private int getBelow(Level level,BlockPos blockPos, int maxDepth) {
        for (int i = 0; i > maxDepth; i--) {
            if(level.getBlockState(blockPos.below(-i)).isAir()){
                continue;
            }
            tryRecipe(level, blockPos.below(-i), blockPos);
            return i;
        }
        return 1;
    }

    private void tryRecipe(Level level,BlockPos pos, BlockPos topPos) {
        for (int i = pos.getY(); i <topPos.getY() ; i++) {
            BlockPos checkPos = pos.atY(i);

            BlockState blockState = level.getBlockState(checkPos);
            //テスト用 ガラスに溶岩をかけると確率で遮光ガラスに(100ミリバケツあたり80%)
            if(RandomSource.create().nextDouble()<(80d/100d)*(0.5d/100d)){
                if(blockState.is(Blocks.GLASS) && this.getFluidInTank(0).getFluid().isSame(Fluids.LAVA)){
                    level.destroyBlock(pos,false);
                    level.setBlock(pos,Blocks.TINTED_GLASS.defaultBlockState(), 3);
                }
            }
            /*
            //テスト用 銅インゴットに溶岩をかけると確率で錬金赤銅に(100ミリバケツあたり95%)
            List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AABB(checkPos));
            for (ItemEntity itemEntity : itemEntities) {
                    if(itemEntity.getItem().is(Items.COPPER_INGOT) && this.getFluidInTank(0).getFluid().isSame(Fluids.LAVA)){
                        int itemCount = itemEntity.getItem().getCount();
                        int changeCount = 0;
                        for (int j = 0; j < itemCount; j++) {
                            if(RandomSource.create().nextDouble()<(95d/100d)*(0.5d/100d)*(1d/itemEntities.size())){
                                changeCount++;
                            }
                        }
                        if(changeCount>0) {
                            ItemEntity popEntity = itemEntity.copy();
                            popEntity.setItem(new ItemStack(ItemRegistry.ALCHEMY_BIRON_INGOT_ITEM.get(), changeCount));

                            level.addFreshEntity(popEntity);
                            itemEntity.getItem().shrink(changeCount);
                        }
                }
            }
             */
        }
    }

    private boolean redStonePowered() {
        return false;
    }

    private boolean hasFluid() {
        return !this.getFluidInTank(0).isEmpty();
    }

    //パーティクルの発生
    public void createParticle(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
        //ブロックエンティティを取得
        BlockEntity blockEntity=level.getBlockEntity(pos);
        if(blockEntity instanceof CometCoreBlockEntity){

            //パーティクルの数を生成
            int particleAmount = randomSource.nextInt(1,4);

            for (int i = 0; i < particleAmount; i++) {
                double orbRadius = calcOrbRadius();
                //位置を用意
                float posX =(float) pos.getCenter().x;
                float posY = (float) pos.getCenter().y;//( pos.getY() +(8f/16f)-orbRadius + (Math.pow(randomSource.nextFloat(),0.25f)*2*orbRadius));
                float posZ =(float) pos.getCenter().z;
                //パーティクルを発生させるベクトルを用意
                double randAngle = Math.toRadians(randomSource.nextFloat()*360);
                //最終的に到達する半径
                double maxReach = 2f;
                //ねじれ角度を宣言
                float screwAngle = -30f;
                //パーティクルの性質から初速を逆算
                double randRange = Math.pow(randomSource.nextFloat(),0.35f) * 0.145d* (maxReach+0.5d- orbRadius)/Math.cos(Math.toRadians(screwAngle));
                //ピッチを設定
                double randPitch = randomSource.nextFloat() * 0.125d+0.1f;
                //直交座標に変換
                double randSin = Math.sin(randAngle);
                double randCos = Math.cos(randAngle);
                //ベクトルを用意
                Vector3d randVec = new Vector3d(randRange * randSin, randPitch, randRange * randCos).rotateY(screwAngle);

                //パーティクルを生成
                //パーティクルに適用する色を液体から用意
                int[] color = getFluidColor(getFluidInTank(0).getFluid());

                Particle particle = Minecraft.getInstance().particleEngine.createParticle(getSpreaderParticle(),
                        posX + (randSin*orbRadius), posY, posZ + (randCos*orbRadius),
                        randVec.x, randVec.y, randVec.z);
                particle.setColor(color[0],color[1],color[2]);
            }
        }
    }

    public double calcOrbRadius() {
        //サイズの限界値を計算
        double sizeMin = (5/16f) * Math.sqrt(3) * 0.8f;
        double sizeMax = 1f;
        //タンクの割合を計算
        double fillRatio = Math.min(1.0f,this.getSmoothedTankAmount() / this.getTankCapacity(0));
        float power = 2f;
        double size = (sizeMin + (sizeMax-sizeMin) * (Math.pow(Math.max(0,Math.min(1,fillRatio)),1f/power)));
        if(size == 0){
            Heliopause.LOGGER.error("Rendering failure on orb size calculation.");
        }
        return size/2f;
    }

    private ParticleOptions getSpreaderParticle() {
        return ParticleRegistry.FLUID_SPREAD_PARTICLES.get();
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
}
