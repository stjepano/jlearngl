package dev.stjepano.math;


import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

public final class Vec3 {
    public float x, y, z;

    /// Default constructor (0, 0, 0)
    public Vec3() {
        this(0, 0, 0);
    }

    /// Value constructor
    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /// Copy into new object and return it.
    public Vec3 copy() {
        return new Vec3(this.x, this.y, this.z);
    }

    /// Allocation free copy.
    public void copy(Vec3 dest) {
        dest.x = x;
        dest.y = y;
        dest.z = z;
    }

    /// Set vector components.
    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /// Set vector components from other vector.
    public Vec3 set(Vec3 other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        return this;
    }

    /// Set from Vec2 and z component.
    public Vec3 set(Vec2 other, float z) {
        this.x = other.x;
        this.y = other.y;
        this.z = z;
        return this;
    }

    /// Add: this += other.
    public Vec3 add(Vec3 other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /// Subtract: this -= other.
    public Vec3 sub(Vec3 other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        return this;
    }

    public Vec3 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /// Scale: this *= s.
    public Vec3 scale(float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        return this;
    }

    /// Negate: this = -this.
    public Vec3 negate() {
        return scale(-1.0f);
    }

    /// Component-wise scale: this.x *= sx, this.y *= sy, this.z *= sz
    public Vec3 scale(float sx, float sy, float sz) {
        this.x *= sx;
        this.y *= sy;
        this.z *= sz;
        return this;
    }

    /// Dot product.
    public float dot(Vec3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    /// Cross product: this = this × other.
    public Vec3 cross(Vec3 other) {
        float nx = this.y * other.z - this.z * other.y;
        float ny = this.z * other.x - this.x * other.z;
        float nz = this.x * other.y - this.y * other.x;
        this.x = nx;
        this.y = ny;
        this.z = nz;
        return this;
    }

    /// Cross product: this = this x Vec3(x, y, z)
    public Vec3 cross(float x, float y, float z) {
        float nx = this.y * z - this.z * y;
        float ny = this.z * x - this.x * z;
        float nz = this.x * y - this.y * x;
        this.x = nx;
        this.y = ny;
        this.z = nz;
        return this;
    }

    /// Cross product: dest = a × b.
    public static void cross(Vec3 a, Vec3 b, Vec3 dest) {
        dest.x = a.y * b.z - a.z * b.y;
        dest.y = a.z * b.x - a.x * b.z;
        dest.z = a.x * b.y - a.y * b.x;
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
    public Vec3 normalize() {
        float len = length();
        if (len < 1e-8f) {
            return this;
        }
        return scale(1.0f / len);
    }

    /// Squared distance between this vector and other vector.
    public float distanceSqr(Vec3 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return dx*dx + dy*dy + dz*dz;
    }

    /// Distance between this vector and other vector.
    public float distance(Vec3 other) {
        return (float) Math.sqrt(distanceSqr(other));
    }

    /// Linearly interpolate between source and target. Store result in dest.
    public static void lerp(Vec3 source, Vec3 target, float t, Vec3 dest) {
        dest.x = source.x + (target.x - source.x) * t;
        dest.y = source.y + (target.y - source.y) * t;
        dest.z = source.z + (target.z - source.z) * t;
    }

    /// Rotate this vector with given quaternion. Make sure that quaternion is _unit length_.
    public Vec3 rotate(Quaternion quaternion) {
        float cqx = -quaternion.x;
        float cqy = -quaternion.y;
        float cqz = -quaternion.z;
        float cqw = quaternion.w;

        float vqx = x;
        float vqy = y;
        float vqz = z;
        float vqw = 0.0f;

        float x = quaternion.w * vqx + quaternion.x * vqw + quaternion.y * vqz - quaternion.z * vqy;
        float y = quaternion.w * vqy + quaternion.y * vqw + quaternion.z * vqx - quaternion.x * vqz;
        float z = quaternion.w * vqz + quaternion.z * vqw + quaternion.x * vqy - quaternion.y * vqx;
        float w = quaternion.w * vqw - quaternion.x * vqx - quaternion.y * vqy - quaternion.z * vqz;

        this.x = w * cqx + x * cqw + y * cqz - z * cqy;
        this.y = w * cqy + y * cqw + z * cqx - x * cqz;
        this.z = w * cqz + z * cqw + x * cqy - y * cqx;
        return this;
    }

    /// Transform this vector with given 3x3 matrix.
    ///
    /// NOTE: Multiplication: this = matrix * this
    public Vec3 transform(Mat3 matrix) {
        float x = matrix.m00*this.x + matrix.m01*this.y + matrix.m02*this.z;
        float y = matrix.m10*this.x + matrix.m11*this.y + matrix.m12*this.z;
        float z = matrix.m20*this.x + matrix.m21*this.y + matrix.m22*this.z;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /// Transform this vector with given 4x4 matrix.
    ///
    /// NOTE: multiplication: this = matrix * Vec4(this, 1)
    public Vec3 transform(Mat4 matrix) {
        float x = matrix.m00*this.x + matrix.m01*this.y + matrix.m02*this.z + matrix.m03;
        float y = matrix.m10*this.x + matrix.m11*this.y + matrix.m12*this.z + matrix.m13;
        float z = matrix.m20*this.x + matrix.m21*this.y + matrix.m22*this.z + matrix.m23;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    /// Copy to float array. First component at dest + offset.
    public void toFloatArray(float[] dest, int offset) {
        dest[offset] = x;
        dest[offset + 1] = y;
        dest[offset + 2] = z;
    }

    /// Copy to float array.
    public void toFloatArray(float[] dest) {
        toFloatArray(dest, 0);
    }

    /// Initialize Vec3 from float array, first component at src + offset.
    public static void fromFloatArray(float[] src, int offset, Vec3 dest) {
        dest.x = src[offset];
        dest.y = src[offset + 1];
        dest.z = src[offset + 2];
    }

    /// Initialize Vec3 from memory segment, first component at src + byteOffset.
    public static void fromMemorySegment(MemorySegment src, long byteOffset, Vec3 dest) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.x = src.get(ValueLayout.JAVA_FLOAT, byteOffset);
        dest.y = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride);
        dest.z = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2);
    }

    /// Copy to memory segment, first element at dest + byteOffset.
    public void toMemorySegment(MemorySegment dest, long byteOffset) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset, x);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride, y);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2, z);
    }

    @Override
    public String toString() {
        return "Vec3(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vec3 vec3 = (Vec3) o;
        return Float.compare(x, vec3.x) == 0 &&
                Float.compare(y, vec3.y) == 0 &&
                Float.compare(z, vec3.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}
