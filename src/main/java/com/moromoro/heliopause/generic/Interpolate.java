package com.moromoro.heliopause.generic;

import org.joml.Math;

public class Interpolate {
    
    public static double sigmoidInterpolate(double partialValue, double scale, double sigmoidStart, double sigmoidEnd) {
        // 範囲を反映
        double localPartialValue = Math.lerp(sigmoidStart, sigmoidEnd, partialValue);
        // 端のサイズを取得
        double minSize = 1.0 / (1.0 + Math.exp((0.5 - sigmoidStart) * scale));
        double maxSize = 1.0 / (1.0 + Math.exp((0.5 - sigmoidEnd) * scale));
        // シグモイド関数
        double localSigmoid = 1.0 / (1.0 + Math.exp((0.5 - localPartialValue) * scale));
        // 範囲を0~1にした補間を返す
        return (localSigmoid - minSize) / (maxSize - minSize);
    }
    
}
