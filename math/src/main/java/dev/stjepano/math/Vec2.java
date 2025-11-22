package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

public final class Vec2 {
    public float x, y;

    /// Default constructor (0, 0)
    public Vec2() {
        this(0, 0);
    }

    /// Value constructor
    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /// Copy into new object and return it.
    public Vec2 copy() {
        return new Vec2(this.x, this.y);
    }

    /// Allocation free copy.
    public void copy(Vec2 dest) {
        dest.x = x;
        dest.y = y;
    }

    /// Set vector components.
    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /// Set vector components from other vector.
    public Vec2 set(Vec2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    /// Add: this += other.
    public Vec2 add(Vec2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    /// Subtract: this -= other.
    public Vec2 sub(Vec2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    /// Scale: this *= s.
    public Vec2 scale(float s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    /// Negate: this = -this.
    public Vec2 negate() {
        return scale(-1.0f);
    }

    /// Component-wise scale: this.x *= sx, this.y *= sy
    public Vec2 scale(float sx, float sy) {
        this.x *= sx;
        this.y *= sy;
        return this;
    }

    /// Dot product.
    public float dot(Vec2 other) {
        return this.x * other.x + this.y * other.y;
    }

    /// Return squared length.
    public float lengthSqr() {
        return this.dot(this);
    }

    /// Return vector length.
    public float length() {
        return (float) Math.sqrt(lengthSqr());
    }

    /// Normalize the vector.
    public Vec2 normalize() {
        float len = length();
        if (len < 1e-8f) {
            return this;
        }
        return scale(1.0f / len);
    }

    /// 2D cross product.
    public float cross2D(Vec2 other) {
        return this.x * other.y - this.y * other.x;
    }

    /// Squared distance between this vector and other vector.
    public float distanceSqr(Vec2 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        return dx*dx + dy*dy;
    }

    /// Distance between this vector and other vector.
    public float distance(Vec2 other) {
        return (float) Math.sqrt(distanceSqr(other));
    }

    /// Linearly interpolate between source and target. Store result in dest.
    public static void lerp(Vec2 source, Vec2 target, float t, Vec2 dest) {
        dest.x = source.x + (target.x - source.x) * t;
        dest.y = source.y + (target.y - source.y) * t;
    }

    /// Copy to float array. First component at dest + offset.
    public void toFloatArray(float[] dest, int offset) {
        dest[offset] = x;
        dest[offset + 1] = y;
    }

    /// Copy to float array.
    public void toFloatArray(float[] dest) {
        toFloatArray(dest, 0);
    }

    /// Initialize Vec2 from float array, first component at src + offset.
    public static void fromFloatArray(float[] src, int offset, Vec2 dest) {
        dest.x = src[offset];
        dest.y = src[offset + 1];
    }

    /// Initialize Vec2 from memory segment, first component at src + byteOffset.
    public static void fromMemorySegment(MemorySegment src, long byteOffset, Vec2 dest) {
        dest.x = src.get(ValueLayout.JAVA_FLOAT, byteOffset);
        dest.y = src.get(ValueLayout.JAVA_FLOAT, byteOffset + ValueLayout.JAVA_FLOAT.byteSize());
    }

    /// Copy to memory segment, first element at dest + byteOffset.
    public void toMemorySegment(MemorySegment dest, long byteOffset) {
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset, x);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + ValueLayout.JAVA_FLOAT.byteSize(), y);
    }

    @Override
    public String toString() {
        return "Vec2(" + this.x + ", " + this.y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vec2 vec2 = (Vec2) o;
        return Float.compare(x, vec2.x) == 0 && Float.compare(y, vec2.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
