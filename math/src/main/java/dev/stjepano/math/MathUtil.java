package dev.stjepano.math;

public final class MathUtil {

    public static final float EPSILON = 1e-8f;

    /// Return true if `a` approximately equals `b`.
    ///
    /// NOTE: abs(a - b) < epsilon
    public static boolean approxEqual(float a, float b, float epsilon) {
        float dist = (float) Math.abs(a - b);
        return dist < epsilon;
    }

}
