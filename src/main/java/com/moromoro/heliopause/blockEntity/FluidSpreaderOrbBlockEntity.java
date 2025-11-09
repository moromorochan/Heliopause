package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

import static com.moromoro.heliopause.generic.StackControl.checkTransferAmount;

public class FluidSpreaderOrbBlockEntity extends CentralStarBlockEntity{

    //1tick毎の混合量
    private static final int mixSpeedAmount = 10;

    private final List<CircumstellarIngredient> ingredients = new ArrayList<>();

    public FluidSpreaderOrbBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, FluidSpreaderOrbBlockEntity blockEntity) {
        boolean active = hasFluid() && !redStonePowered();
        RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
        //星周天体の混合
        combineIngredients(pos, randomSource);
        //地上への影響処理
        setEcoSynthesis(pos, randomSource);
        //星周天体の生成
        //setIngredientsBehavior(pos, randomSource);
        //インタラクト用の動き
        /*if(!level.isClientSide()){
            ServerLevel serverLevel = (ServerLevel) this.level;
            if (serverLevel != null) {
                List<ServerPlayer> players = serverLevel.players();
                for(Player player : players){
                    if (isInRange(player,10)) {
                        setOrreryInteractionOperator(player, serverLevel);
                    }
                }
            }
        }*/
    }

    private void combineIngredients(BlockPos pos, RandomSource randomSource) {
        //軌道半径の小さい順に並び替え
        //ingredients.sort(Comparator.comparing(CircumstellarIngredient::getOrbitalRadius));
        //星周天体の合成
        for (int i = 0; i < ingredients.size(); i++) {
            CircumstellarIngredient ingredient = ingredients.get(i);
            if(ingredient==null) {
                continue;
            }
            //円盤の処理
            if(ingredient.getDisk_shaped()){
                for (int j = i+1; j < ingredients.size(); j++) {
                    //外側と合成
                    if(ingredients.get(j)!=null){
                        CircumstellarIngredient outerIngredient = ingredients.get(j);
                        if(outerIngredient.getDisk_shaped()){
                            //それぞれの環の影響圏を取得
                            float innerWidth =
                                calcRingWidth(
                                    ingredient.getItemStack().getCount()==0? ingredient.getFluidStack().getAmount(): ingredient.getItemStack().getCount() * 1000,
                                    ingredient.getOrbitalRadius()
                                )/2f;
                            float outerWidth =
                                calcRingWidth(
                                    outerIngredient.getItemStack().getCount()==0? outerIngredient.getFluidStack().getAmount(): outerIngredient.getItemStack().getCount() * 1000,
                                    outerIngredient.getOrbitalRadius()
                                )/2f;
                            //重なっているなら合成し、処理を終了
                            if(innerWidth+outerWidth >= outerIngredient.getOrbitalRadius() - ingredient.getOrbitalRadius()){
                                //一度の移送量を用意
                                int transferAmount = checkTransferAmount(ingredient.getAmount(),30000, outerIngredient.getAmount(),mixSpeedAmount);
                                FluidStack resultFluidStack = FluidStack.EMPTY;
                                ItemStack resultItemStack = ItemStack.EMPTY;
                                int resultItemAmount = 0;
                                //レシピ確認


                                /*//同じものなら瞬時に合成
                                if(!ingredient.getFluidStack().isEmpty() && !outerIngredient.getFluidStack().isEmpty() && ingredient.getFluidStack().isFluidEqual(outerIngredient.getFluidStack())){
                                    float innerRadius = ingredient.getOrbitalSlotId();
                                    float outerRadius = outerIngredient.getOrbitalSlotId();
                                    ingredient.setOrbitalSlotId(Mth.lerp(innerRadius,outerRadius,outerRadius/(ingredient.getAmount()+outerIngredient.getAmount())));
                                    ingredient.getFluidStack().grow(outerIngredient.getFluidStack().getAmount());
                                    outerIngredient.setFluidStack(FluidStack.EMPTY);
                                    ingredients.set(i,ingredient);
                                    ingredients.set(j,outerIngredient);
                                    break;
                                }
                                else if(ingredient.getItemStack().getCount()!=0 && outerIngredient.getItemStack().getCount()!=0 && ingredient.getItemStack().is(outerIngredient.getItemStack().getItem())){
                                    float innerRadius = ingredient.getOrbitalSlotId()-innerWidth;
                                    float outerRadius = outerIngredient.getOrbitalSlotId()+outerWidth;

                                    ingredient.setOrbitalSlotId(Mth.lerp(innerRadius,outerRadius,outerRadius/(ingredient.getAmount()+outerIngredient.getAmount())));
                                    ingredient.setFractableItemAmount(ingredient.getFractableItemAmount()+ outerIngredient.getFractableItemAmount());
                                    outerIngredient.setFractableItemAmount(0);
                                    ingredients.set(i,ingredient);
                                    ingredients.set(j,outerIngredient);
                                    break;
                                }*/
                                //合成と収縮
                                /*ingredients.add(
                                    new CircumstellarIngredient(
                                        ingredient.getOrbitalSlotId()+(innerWidth+outerWidth)/2,
                                        0,
                                        0,
                                        resultFluidStack,
                                        resultItemStack,
                                        resultItemAmount,
                                        true
                                        ));*/
                            }
                        }
                        /*else if (*//*羊飼い衛星の場合の処理を書く*//*false) {
                            *//*重なっているなら、円盤を分離*//*
                            break;
                        }*/
                    }else {break;}
                }
            }
            //オーブの処理
            else{
                //位置を取得
                Vec3 ingredientPos = this.getBlockPos().getCenter().add(getCartesianCoordinates(ingredient.getOrbitalRadius(),ingredient.getRevolutionOffset()));
                //質量(量)を取得
                int ingredientAmount = ingredient.getFluidStack().getAmount();
                //重力圏の半径を取得
                float gravityAreaRadius = ingredientAmount*0.0006f;
                if(ingredientAmount > MAX_INGREDIENT_AMOUNT) {
                    //漏れた分は環に
                    /*ingredients.add(new CircumstellarIngredient(
                        ingredient.getOrbitalSlotId(),
                        ingredient.getRevolutionOffset(),
                        0,
                        new FluidStack(ingredient.getFluidStack().getFluid(), mixSpeedAmount),
                        true
                    ));*/
                    ingredient.getFluidStack().shrink(mixSpeedAmount);
                    continue;
                }
                //差が重力圏を出るまで内側へ検索
                for (int j = i-1; j >= 0; j--) {
                   //boolean continue_stat = attractBetweenSatellites(j, ingredient, gravityAreaRadius, ingredientPos, ingredientAmount, i);
                   //if(!continue_stat){break;}
                }
                //差が重力圏を出るまで外側へ検索
                for (int j = i+1; j < ingredients.size(); j++) {
                    //boolean continue_stat = attractBetweenSatellites(j, ingredient, gravityAreaRadius, ingredientPos, ingredientAmount, i);
                    //if(!continue_stat){break;}
                    /*if(ingredients.get(j)!=null){
                        CircumstellarIngredient outerIngredient = ingredients.get(j);
                        //形状確認
                        if(!outerIngredient.getDisk_shaped()){
                            //半径の差を確認
                            if(outerIngredient.getOrbitalSlotId() - ingredient.getOrbitalSlotId() > gravityAreaRadius){
                                break;
                            }
                            //距離を確認
                            Vec3 outerIngredientPos = getCartesianCoordinates(outerIngredient.getOrbitalSlotId(), outerIngredient.getRevolutionOffset());
                            double ingredientDist = ingredientPos.distanceTo(outerIngredientPos);
                            if(ingredientDist > gravityAreaRadius){
                                continue;
                            }
                            //重力を掛ける
                            int outerAmount = outerIngredient.getFluidStack().getAmount();
                            double attractLength = ((double) ingredientAmount /outerAmount) * GRAVITATIONAL_CONST * Math.pow(gravityAreaRadius-ingredientDist,2);
                            //引き寄せる距離が接触する距離を越えるなら結合
                            if(attractLength >= ingredientDist - (ingredient.getSatRadius(MAX_INGREDIENT_AMOUNT)+outerIngredient.getSatRadius(MAX_INGREDIENT_AMOUNT))){
                                //先に結果が大きすぎないか確認
                                int transferAmount = outerAmount;
                                if(ingredientAmount + transferAmount > MAX_INGREDIENT_AMOUNT) {
                                    int spillAmount = Math.min(mixSpeedAmount, transferAmount - (MAX_INGREDIENT_AMOUNT - ingredientAmount));
                                    //漏れた分は環に
                                    ingredients.add(new CircumstellarIngredient(
                                        ingredient.getOrbitalSlotId(),
                                        ingredient.getRevolutionOffset(),
                                        0,
                                        new FluidStack(ingredient.getFluidStack().getFluid(),spillAmount),
                                        true
                                    ));
                                    transferAmount -= spillAmount;
                                }
                                ingredient.getFluidStack().grow(transferAmount);
                                outerIngredient.setFluidStack(FluidStack.EMPTY);
                                ingredients.set(i,ingredient);
                                ingredients.set(j,outerIngredient);
                                Pair<Float, Float> combinePos = getPolarCoordinates(new Vec3(ingredientPos.toVector3f()).lerp(outerIngredientPos, (double) outerAmount /(ingredientAmount+outerAmount)));
                                ingredient.setOrbitalSlotId(combinePos.getA());
                                ingredient.setRevolutionOffset(combinePos.getB());
                                continue;
                            }
                            Vec3 attractVec = ingredientPos.subtract(outerIngredientPos).normalize().scale(attractLength);
                            Pair<Float, Float> innerPolarPos = getPolarCoordinates(outerIngredientPos.add(attractVec));
                            outerIngredient.setOrbitalSlotId(innerPolarPos.getA());
                            outerIngredient.setRevolutionOffset(innerPolarPos.getB());
                            ingredients.set(j,outerIngredient);
                        }
                    }*/
                }
            }
        }
    }

    /*private boolean attractBetweenSatellites(int j, CircumstellarIngredient ingredient, float gravityAreaRadius, Vec3 ingredientPos, int ingredientAmount, int i) {
        if(ingredients.get(j)!=null){
            CircumstellarIngredient targetIngredient = ingredients.get(j);
            //形状確認
            if(!targetIngredient.getDisk_shaped()){
                //半径の差を確認
                if(Math.abs(ingredient.getOrbitalSlotId() - targetIngredient.getOrbitalSlotId()) > gravityAreaRadius){
                    return false;//break
                }
                //距離を確認
                Vec3 targetIngredientPos = getCartesianCoordinates(targetIngredient.getOrbitalSlotId(), targetIngredient.getRevolutionOffset());
                double ingredientDist = ingredientPos.distanceTo(targetIngredientPos);
                if(ingredientDist > gravityAreaRadius){
                    return true;//continue
                }
                //重力を掛ける
                int targetIngredientAmount = targetIngredient.getFluidStack().getAmount();
                double attractLength = ((double) ingredientAmount /targetIngredientAmount) * GRAVITATIONAL_CONST * Math.pow(gravityAreaRadius -ingredientDist,2);
                //引き寄せる距離が接触する距離を越えるなら結合
                if(attractLength >= ingredientDist - (ingredient.getSatRadius(MAX_INGREDIENT_AMOUNT)+targetIngredient.getSatRadius(MAX_INGREDIENT_AMOUNT))){
                    //位置を設定
                    Pair<Float, Float> combinePos = getPolarCoordinates(new Vec3(ingredientPos.toVector3f()).lerp(targetIngredientPos, (double) targetIngredientAmount /(ingredientAmount +targetIngredientAmount)));
                    ingredient.setOrbitalSlotFromRadius(combinePos.getA());
                    ingredient.setRevolutionOffset(combinePos.getB());

                    int transferAmount = targetIngredientAmount;
                    //量が多いと弾けて崩れる
                    if(transferAmount > mixSpeedAmount * 5) {
                        int spillAmount = *//*mixSpeedAmount;*//*transferAmount - (MAX_INGREDIENT_AMOUNT - ingredientAmount);
                        //漏れた分は環に
                        ingredients.add(new CircumstellarIngredient(
                            ingredient.getOrbitalSlotId(),
                            ingredient.getRevolutionOffset(),
                            0,
                            new FluidStack(ingredient.getFluidStack().getFluid(),spillAmount),
                            true
                        ));
                        transferAmount -= spillAmount;
                    }
                        //量を設定
                        ingredient.getFluidStack().grow(transferAmount);
                        targetIngredient.setFluidStack(FluidStack.EMPTY);
                        //書き込み
                        ingredients.set(i, ingredient);
                        ingredients.set(j, targetIngredient);
                        return true;//continue
                }
                Vec3 attractVec = ingredientPos.subtract(targetIngredientPos).normalize().scale(attractLength);
                Pair<Float, Float> innerPolarPos = getPolarCoordinates(targetIngredientPos.add(attractVec));
                targetIngredient.setOrbitalSlotFromRadius(innerPolarPos.getA());
                targetIngredient.setRevolutionOffset(innerPolarPos.getB());
                ingredients.set(j,targetIngredient);
            }
        }
        return true;//continue
    }
*/
    private void setEcoSynthesis(BlockPos pos, RandomSource randomSource){

    }
}