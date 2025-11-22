package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

/// _Rotation_ quaternion.
///
/// For quaternion to represent valid rotation it must be normalized. Non-normalized quaternions do not
/// represent valid rotations.
///
/// If you create a quaternion from _normalized_ axis and angle you will get _unit length_ quaternion, and you
/// should not need to normalize it.
///
/// However, because of floating point errors it is good to normalize the quaternion now and then.
public final class Quaternion {
    public float x, y, z, w;

    /// Create a no-rotation quaternion.
    public Quaternion() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
        this.w = 1.0f;
    }

    /// Construct a quaternion. Use only if you know your maths.
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /// Create a quaternion from _normalized_ axis and angle in radians.
    public Quaternion(Vec3 normalizedAxis, float angleRad) {
        setAxisAngle(normalizedAxis, angleRad);
    }

    /// Set quaternion from individual components (use if you know your quaternion maths).
    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /// Set quaternion to be same as provided quaternion.
    public Quaternion set(Quaternion rot) {
        return set(rot.x, rot.y, rot.z, rot.w);
    }

    /// Set quaternion from axis and angle.
    public Quaternion setAxisAngle(Vec3 normalizedAxis, float angleRad) {
        float halfAngle = angleRad * 0.5f;
        float s = (float) Math.sin(halfAngle);
        this.x = normalizedAxis.x * s;
        this.y = normalizedAxis.y * s;
        this.z = normalizedAxis.z * s;
        this.w = (float) Math.cos(halfAngle);
        return this;
    }


    /// Copy quaternion into new quaternion (does heap allocation).
    public Quaternion copy() {
        return new Quaternion(this.x, this.y, this.z, this.w);
    }

    /// No-alloc copy.
    public void copy(Quaternion dest) {
        dest.x = this.x;
        dest.y = this.y;
        dest.z = this.z;
        dest.w = this.w;
    }

    /// Squared length.
    public float lengthSqr() {
        return x*x + y*y + z*z + w*w;
    }

    /// Quaternion length.
    public float length() {
        return (float) Math.sqrt(lengthSqr());
    }

    /// Normalize the quaternion so it has unit length.
    public Quaternion normalize() {
        float len = length();
        if (len < 1e-8) {
            return this;
        }
        float invLen = 1.0f/len;
        x *= invLen;
        y *= invLen;
        z *= invLen;
        w *= invLen;
        return this;
    }

    /// Conjugate this quaternion. Note that when quaternion is unit length, conjugate equals inverse.
    public Quaternion conjugate() {
        this.x *= -1;
        this.y *= -1;
        this.z *= -1;
        return this;
    }

    /// Multiplication: this = this * other
    public Quaternion mul(Quaternion other) {
        float x = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
        float y = this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z;
        float z = this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x;
        float w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /// Multiplies N-1 quaternions in specified order and store the result in last quaternion.
    /// When doing combine(a, b, c, dest) -> dest = a * b * c
    public static void combine(Quaternion... quaternions) {
        if (quaternions.length <= 2) {
            throw new IllegalArgumentException("At least 3 quaternions expected.");
        }

        float accX = quaternions[0].x;
        float accY = quaternions[0].y;
        float accZ = quaternions[0].z;
        float accW = quaternions[0].w;

        for (int i = 1; i < quaternions.length - 1; i++) {
            Quaternion other = quaternions[i];
            float x = accW * other.x + accX * other.w + accY * other.z - accZ * other.y;
            float y = accW * other.y + accY * other.w + accZ * other.x - accX * other.z;
            float z = accW * other.z + accZ * other.w + accX * other.y - accY * other.x;
            float w = accW * other.w - accX * other.x - accY * other.y - accZ * other.z;
            accX = x;
            accY = y;
            accZ = z;
            accW = w;
        }

        Quaternion dest = quaternions[quaternions.length - 1];
        dest.x = accX;
        dest.y = accY;
        dest.z = accZ;
        dest.w = accW;
    }

    /// Calculate basis vectors from quaternion ...
    public void toBasis(Vec3 destX, Vec3 destY, Vec3 destZ) {
        float xx = this.x * this.x;
        float yy = this.y * this.y;
        float zz = this.z * this.z;

        float xy = this.x * this.y;
        float xz = this.x * this.z;
        float yz = this.y * this.z;

        float wx = this.w * this.x;
        float wy = this.w * this.y;
        float wz = this.w * this.z;

        destX.x = 1 - 2 * (yy + zz);
        destX.y = 2 * (xy + wz);
        destX.z = 2 * (xz - wy);

        destY.x = 2 * (xy - wz);
        destY.y = 1 - 2 * (xx + zz);
        destY.z = 2 * (yz + wx);

        destZ.x = 2 * (xz + wy);
        destZ.y = 2 * (yz - wx);
        destZ.z = 1 - 2 * (xx + yy);
    }

    /// Calculates axis and angle of rotation.
    /// @param destAxis this is where axis is stored
    /// @return angle of rotation (in radians)
    public float toAxisAngle(Vec3 destAxis) {
        float len = length();
        if (len < 1e-8f) {
            destAxis.x = 1.0f;
            destAxis.y = 0.0f;
            destAxis.z = 0.0f;
            return 0.0f;
        }

        float x = this.x / len;
        float y = this.y / len;
        float z = this.z / len;
        float w = this.w / len;


        float s = (float)Math.sqrt(1.0f - w*w);
        if (s < 1e-8f) {
            destAxis.x = 1.0f;
            destAxis.y = 0.0f;
            destAxis.z = 0.0f;
            return 0.0f;
        }

        destAxis.x = x/s;
        destAxis.y = y/s;
        destAxis.z = z/s;
        return (float) Math.acos(w) * 2.0f;
    }

    /// Copy to float array. First component at dest + offset.
    public void toFloatArray(float[] dest, int offset) {
        dest[offset] = x;
        dest[offset + 1] = y;
        dest[offset + 2] = z;
        dest[offset + 3] = w;
    }

    /// Copy to float array.
    public void toFloatArray(float[] dest) {
        toFloatArray(dest, 0);
    }

    /// Initialize Quaternion from float array, first component at src + offset.
    public static void fromFloatArray(float[] src, int offset, Quaternion dest) {
        dest.x = src[offset];
        dest.y = src[offset + 1];
        dest.z = src[offset + 2];
        dest.w = src[offset + 3];
    }

    /// Initialize Quaternion from memory segment, first component at src + byteOffset.
    public static void fromMemorySegment(MemorySegment src, long byteOffset, Quaternion dest) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.x = src.get(ValueLayout.JAVA_FLOAT, byteOffset);
        dest.y = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride);
        dest.z = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2);
        dest.w = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 3);
    }

    /// Copy to memory segment, first element at dest + byteOffset.
    public void toMemorySegment(MemorySegment dest, long byteOffset) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset, x);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride, y);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2, z);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 3, w);
    }

    /// Spherical linear interpolation between two quaternions.
    /// Both quaternions should be unit length.
    public static void slerp(Quaternion a, Quaternion b, float t, Quaternion dest) {
        float dot = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;

        float bx = b.x;
        float by = b.y;
        float bz = b.z;
        float bw = b.w;
        if (dot < 0.0f) {
            // if dot product negative, negate one quaternion to take shorter path
            bx *= -1;
            by *= -1;
            bz *= -1;
            bw *= -1;
            dot = -dot;
        }

        if (dot > 0.9995f) {
            // if very close, interpolate linearly
            dest.x = a.x + t * (bx - a.x);
            dest.y = a.y + t * (by - a.y);
            dest.z = a.z + t * (bz - a.z);
            dest.w = a.w + t * (bw - a.w);
            dest.normalize();
            return;
        }

        float theta = (float) Math.acos(dot);
        float sinTheta = (float) Math.sin(theta);
        float wa = (float) Math.sin((1.0f - t) * theta) / sinTheta;
        float wb = (float) Math.sin(t * theta) / sinTheta;

        dest.x = a.x * wa + bx * wb;
        dest.y = a.y * wa + by * wb;
        dest.z = a.z * wa + bz * wb;
        dest.w = a.w * wa + bw * wb;
    }

    @Override
    public String toString() {
        return "Quaternion(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Quaternion that = (Quaternion) o;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0 && Float.compare(z, that.z) == 0 && Float.compare(w, that.w) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

}
