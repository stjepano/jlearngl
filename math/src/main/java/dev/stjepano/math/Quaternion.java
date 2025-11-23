package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

import static dev.stjepano.math.MathUtil.EPSILON8;

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
        return setAxisAngle(normalizedAxis.x, normalizedAxis.y, normalizedAxis.z, angleRad);
    }

    /// Set quaternion from axis coordinates and angle. Make sure that axis is normalized!
    public Quaternion setAxisAngle(float axisX, float axisY, float axisZ, float angleRad) {
        float halfAngle = angleRad * 0.5f;
        float s = (float) Math.sin(halfAngle);
        this.x = axisX * s;
        this.y = axisY * s;
        this.z = axisZ * s;
        this.w = (float) Math.cos(halfAngle);
        return this;
    }

    public Quaternion setRotationX(float angleRad) {
        return setAxisAngle(1, 0, 0, angleRad);
    }

    public Quaternion setRotationY(float angleRad) {
        return setAxisAngle(0, 1, 0, angleRad);
    }

    public Quaternion setRotationZ(float angleRad) {
        return setAxisAngle(0, 0, 1, angleRad);
    }

    /// Set quaternion from rotation matrix (using Shepperd's algorithm)
    public Quaternion setFromRotationMatrix(Mat3 m) {
        return setFromRotationMatrix(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
    }

    /// Set quaternion from rotation matrix (using Shepperd's algorithm)
    public Quaternion setFromRotationMatrix(Mat4 m) {
        return setFromRotationMatrix(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
    }

    /// Set from rotation matrix coefficients (using Shepperd's algorithm)
    public Quaternion setFromRotationMatrix(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float trace = m00 + m11 + m22;
        if (trace > 0.0f) {
            float s = (float) Math.sqrt(trace + 1.0f) * 2.0f;
            this.w = 0.25f * s;
            this.x = (m21 - m12) / s;
            this.y = (m02 - m20) / s;
            this.z = (m10 - m01) / s;
        } else if ((m00 > m11) && (m00 > m22)) {
            float s = (float)Math.sqrt(1.0f + m00 - m11 - m22) * 2.0f; // s = 4 * qx
            this.w = (m21 - m12) / s;
            this.x = 0.25f * s;
            this.y = (m01 + m10) / s;
            this.z = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = (float)Math.sqrt(1.0f + m11 - m00 - m22) * 2.0f; // s = 4 * qy
            this.w = (m02 - m20) / s;
            this.x = (m01 + m10) / s;
            this.y = 0.25f * s;
            this.z = (m12 + m21) / s;
        } else {
            float s = (float)Math.sqrt(1.0f + m22 - m00 - m11) * 2.0f; // s = 4 * qz
            this.w = (m10 - m01) / s;
            this.x = (m02 + m20) / s;
            this.y = (m12 + m21) / s;
            this.z = 0.25f * s;
        }

        return this;
    }

    /// Set quaternion to represent euler angles rotation in YXZ order (Yaw-Pitch-Roll).
    ///
    /// NOTE: this is the most common rotation in games.
    /// @param pitchRad pitch angle in radians (about X axis)
    /// @param yawRad yaw angle in radians (about Y axis)
    /// @param rollRad roll angle in radians (about Z axis)
    /// @return reference to this
    public Quaternion setEulerYXZ(float pitchRad, float yawRad, float rollRad) {
        // NOTE: qRoll * qPitch * qYaw

        float halfPitch = pitchRad * 0.5f;
        float halfYaw = yawRad * 0.5f;
        float halfRoll = rollRad * 0.5f;

        float px = (float) Math.sin(halfPitch);
        float pw = (float) Math.cos(halfPitch);
        float yy = (float) Math.sin(halfYaw);
        float yw = (float) Math.cos(halfYaw);
        float ry = (float) Math.sin(halfRoll);
        float rw = (float) Math.cos(halfRoll);

        this.x = px*rw*yw - pw*ry*yy;
        this.y = px*ry*yw + pw*rw*yy;
        this.z = pw*ry*yw + px*rw*yy;
        this.w = pw*rw*yw - px*ry*yy;
        return this;
    }

    /// Set quaternion to represent euler angles rotation in ZXY order (Roll-Pitch-Yaw)
    ///
    /// NOTE: Very common, used by Unity.
    /// @param pitchRad pitch angle in radians (about X axis)
    /// @param yawRad yaw angle in radians (about Y axis)
    /// @param rollRad roll angle in radians (about Z axis)
    /// @return reference to this
    public Quaternion setEulerZXY(float pitchRad, float yawRad, float rollRad) {
        // NOTE: qYaw * qPitch * qRoll
        float halfPitch = pitchRad * 0.5f;
        float halfYaw = yawRad * 0.5f;
        float halfRoll = rollRad * 0.5f;

        float px = (float) Math.sin(halfPitch);
        float pw = (float) Math.cos(halfPitch);
        float yy = (float) Math.sin(halfYaw);
        float yw = (float) Math.cos(halfYaw);
        float ry = (float) Math.sin(halfRoll);
        float rw = (float) Math.cos(halfRoll);

        this.x = px*rw*yw + pw*ry*yy;
        this.y = -px*ry*yw + pw*rw*yy;
        this.z = pw*ry*yw - px*rw*yy;
        this.w = pw*rw*yw + px*ry*yy;
        return this;
    }

    /// Set quaternion to represent euler angles rotation in ZYX order (Roll-Yaw-Pitch).
    ///
    /// NOTE: Mostly used in space and aero simulations.
    /// @param pitchRad pitch angle in radians (about X axis)
    /// @param yawRad yaw angle in radians (about Y axis)
    /// @param rollRad roll angle in radians (about Z axis)
    /// @return reference to this
    public Quaternion setEulerZYX(float pitchRad, float yawRad, float rollRad) {
        // NOTE: qPitch * qYaw * qRoll
        float halfPitch = pitchRad * 0.5f;
        float halfYaw = yawRad * 0.5f;
        float halfRoll = rollRad * 0.5f;

        float px = (float) Math.sin(halfPitch);
        float pw = (float) Math.cos(halfPitch);
        float yy = (float) Math.sin(halfYaw);
        float yw = (float) Math.cos(halfYaw);
        float ry = (float) Math.sin(halfRoll);
        float rw = (float) Math.cos(halfRoll);

        this.x = px*rw*yw + pw*ry*yy;
        this.y = -px*ry*yw + pw*rw*yy;
        this.z = pw*ry*yw + px*rw*yy;
        this.w = pw*rw*yw - px*ry*yy;
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
        // ox tw + ow tx + oz ty - oy tz
        // oy tw - oz tx + ow ty + ox tz
        // oz tw + oy tx - ox ty + ow tz
        // ow tw - ox tx - oy ty - oz tz
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

    /// Multiplication: this = this * Quaternion(qx, qy, qz, qw)
    public Quaternion mul(float qx, float qy, float qz, float qw) {
        float x = this.w * qx + this.x * qw + this.y * qz - this.z * qy;
        float y = this.w * qy + this.y * qw + this.z * qx - this.x * qz;
        float z = this.w * qz + this.z * qw + this.x * qy - this.y * qx;
        float w = this.w * qw - this.x * qx - this.y * qy - this.z * qz;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /// Right side multiplication: this = other * this
    public Quaternion preMul(Quaternion other) {
        float x = other.x*this.w + other.w*this.x - other.z*this.y + other.y*this.z;
        float y = other.y*this.w + other.z*this.x + other.w*this.y - other.x*this.z;
        float z = other.z*this.w - other.y*this.x + other.x*this.y + other.w*this.z;
        float w = other.w*this.w - other.x*this.x - other.y*this.y - other.z*this.z;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /// Right side multiplication: this = Quaternion(qx, qy, qz, qw) * this
    public Quaternion preMul(float qx, float qy, float qz, float qw) {
        float x = qx*this.w + qw*this.x - qz*this.y + qy*this.z;
        float y = qy*this.w + qz*this.x + qw*this.y - qx*this.z;
        float z = qz*this.w - qy*this.x + qx*this.y + qw*this.z;
        float w = qw*this.w - qx*this.x - qy*this.y - qz*this.z;
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
    /// @param destX store the X vector (right vector in OpenGL convention). Can be null.
    /// @param destY store the Y vector (up vector in OpenGL convention). Can be null.
    /// @param destZ store the Z vector (_backward_ (negated forward) vector in OpenGL convention). Can be null.
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

        if (destX != null) {
            destX.x = 1 - 2 * (yy + zz);
            destX.y = 2 * (xy + wz);
            destX.z = 2 * (xz - wy);
        }

        if (destY != null) {
            destY.x = 2 * (xy - wz);
            destY.y = 1 - 2 * (xx + zz);
            destY.z = 2 * (yz + wx);
        }

        if (destZ != null) {
            destZ.x = 2 * (xz + wy);
            destZ.y = 2 * (yz - wx);
            destZ.z = 1 - 2 * (xx + yy);
        }
    }

    /// Convert quaternion to rotation matrix (3x3)
    public void toRotationMatrix(Mat3 m) {
        m.m00 = 1 - 2*(this.y*this.y + this.z*this.z);
        m.m01 = 2*(this.x*this.y - this.w*this.z);
        m.m02 = 2*(this.w*this.y + this.x*this.z);

        m.m10 = 2*(this.x*this.y + this.w*this.z);
        m.m11 = 1 - 2*(this.x*this.x + this.z*this.z);
        m.m12 = 2*(-this.w*this.x + this.y*this.z);

        m.m20 = 2*(-this.w*this.y + this.x*this.z);
        m.m21 = 2*(this.w*this.x + this.y*this.z);
        m.m22 = 1 - 2*(this.x*this.x + this.y*this.y);
    }

    /// Convert quaternion to rotation matrix (3x3)
    public void toRotationMatrix(Mat4 m) {
        m.m00 = 1 - 2*(this.y*this.y + this.z*this.z);
        m.m01 = 2*(this.x*this.y - this.w*this.z);
        m.m02 = 2*(this.w*this.y + this.x*this.z);
        m.m03 = 0;

        m.m10 = 2*(this.x*this.y + this.w*this.z);
        m.m11 = 1 - 2*(this.x*this.x + this.z*this.z);
        m.m12 = 2*(-this.w*this.x + this.y*this.z);
        m.m13 = 0;

        m.m20 = 2*(-this.w*this.y + this.x*this.z);
        m.m21 = 2*(this.w*this.x + this.y*this.z);
        m.m22 = 1 - 2*(this.x*this.x + this.y*this.y);
        m.m23 = 0;

        m.m30 = 0;
        m.m31 = 0;
        m.m32 = 0;
        m.m33 = 1;
    }

    /// Calculates axis and angle of rotation.
    /// @param destAxis this is where axis is stored
    /// @return angle of rotation (in radians)
    public float toAxisAngle(Vec3 destAxis) {
        float len = length();
        if (len < EPSILON8) {
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
        if (s < EPSILON8) {
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
