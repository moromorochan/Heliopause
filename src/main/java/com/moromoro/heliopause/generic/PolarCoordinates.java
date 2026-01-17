package com.moromoro.heliopause.generic;

import net.minecraft.util.Mth;
import org.joml.Vector2d;
import oshi.util.tuples.Pair;

// 極座標系の処理
public class PolarCoordinates {
    public static Pair<Double,Double> getPolarCoordinates(double operatorPosX, double operatorPosZ) {
        double radius = Vector2d.length(operatorPosX, operatorPosZ);
        double theta = Math.atan2(operatorPosZ, operatorPosX);

        return new Pair<>(radius, theta);
    }

    public static Vector2d getCartesianCoordinates(double radius, double theta){
        double x = radius * Math.cos(theta);
        double z = radius * Math.sin(theta);
        return new Vector2d(x,z);
    }

    public static double revolutionProcess(double orbitRadius, double presentRevOffset, double centripetalForce, double partialTicks){
        return presentRevOffset + Mth.lerp(Mth.sqrt((float) (centripetalForce/orbitRadius))/orbitRadius, 0, partialTicks);
    }

    public static double getCircumSpeed(double orbitRadius, double centripetalForce){
        // 角速度
        double angularVel = revolutionProcess(orbitRadius, 0, centripetalForce, 1);
        return angularVel * orbitRadius;
    }
}
