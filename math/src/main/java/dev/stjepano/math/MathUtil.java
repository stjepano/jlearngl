package dev.stjepano.math;

public final class MathUtil {

    public static final float EPSILON4 = 1e-4f;
    public static final float EPSILON6 = 1e-6f;
    public static final float EPSILON8 = 1e-8f;

    /// Return true if `a` approximately equals `b`.
    ///
    /// NOTE: abs(a - b) < epsilon
    public static boolean approxEqual(float a, float b, float epsilon) {
        float dist = (float) Math.abs(a - b);
        return dist < epsilon;
    }

    /// Return safe 1.0 / value. If value is smaller than epsilon in absolute value,
    /// return 1.0 / epsilon (with sign of value). This avoids division by zero or
    /// extremely large results.
    public static float safeOneOver(float value, float epsilon) {
        if (Math.abs(value) < epsilon) {
            return 1.0f / (value >= 0.0f ? epsilon : -epsilon);
        }
        return 1.0f / value;
    }

}
