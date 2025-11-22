package dev.stjepano.math;

import java.util.Objects;

/// Represents object's transformation in 3D space using:
/// - position (Vec3) - translation
/// - rotation (Quaternion) - orientation
/// - Scale (Vec3) - size
public final class Transform {
    private final Vec3 position = new Vec3();
    private final Vec3 scale = new Vec3(1, 1, 1);
    private final Quaternion rotation = new Quaternion();

    /// Get the current position.
    public Vec3 position() {
        return this.position;
    }

    /// Get the current scale.
    public Vec3 scale() {
        return this.scale;
    }

    /// Get the current rotation.
    public Quaternion rotation() {
        return this.rotation;
    }

    /// Set position to given coordinates.
    public Transform setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        return this;
    }

    /// Set position to given vector.
    public Transform setPosition(Vec3 pos) {
        this.position.set(pos);
        return this;
    }

    /// Set rotation to given quaternion.
    public Transform setRotation(Quaternion rot) {
        this.rotation.set(rot);
        return this;
    }

    public Transform setRotationX(float angleRad) {
        this.rotation.setRotationX(angleRad);
        return this;
    }

    public Transform setRotationY(float angleRad) {
        this.rotation.setRotationY(angleRad);
        return this;
    }

    public Transform setRotationZ(float angleRad) {
        this.rotation.setRotationZ(angleRad);
        return this;
    }

    /// Set rotation to rotation of `angleRad` radians about given `unitAxis`.
    public Transform setRotation(Vec3 unitAxis, float angleRad) {
        this.rotation.setAxisAngle(unitAxis, angleRad);
        return this;
    }

    /// Set rotation to specified euler angles.
    /// Uses YXZ order (Yaw - Pitch - Roll).
    ///
    /// NOTE: typical rotation in game engines.
    /// @param pitchRad pitch angle in radians (rotation about X)
    /// @param yawRad yaw angle in radians (rotation about Y)
    /// @param rollRad roll angle in radians (rotation about Z)
    public Transform setRotationEulerYXZ(float pitchRad, float yawRad, float rollRad) {
        this.rotation.setEulerYXZ(pitchRad, yawRad, rollRad);
        return this;
    }

    /// Set rotation to specified euler angles.
    /// Uses ZXY order (Roll-Pitch-Yaw).
    ///
    /// NOTE: also commonly used in game engines.
    /// @param pitchRad pitch angle in radians (rotation about X)
    /// @param yawRad yaw angle in radians (rotation about Y)
    /// @param rollRad roll angle in radians (rotation about Z)
    public Transform setRotationEulerZXY(float pitchRad, float yawRad, float rollRad) {
        this.rotation.setEulerZXY(pitchRad, yawRad, rollRad);
        return this;
    }

    /// Set rotation to specified euler angles.
    /// Uses ZYX order (Roll-Yaw-Pitch).
    ///
    /// NOTE: for space and aero simulation.
    /// @param pitchRad pitch angle in radians (rotation about X)
    /// @param yawRad yaw angle in radians (rotation about Y)
    /// @param rollRad roll angle in radians (rotation about Z)
    public Transform setRotationEulerZYX(float pitchRad, float yawRad, float rollRad) {
        this.rotation.setEulerZYX(pitchRad, yawRad, rollRad);
        return this;
    }

    /// Set the transform scale.
    public Transform setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
        return this;
    }

    /// Set the transform scale uniformly.
    public Transform setScale(float uniformScale) {
        this.scale.set(uniformScale, uniformScale, uniformScale);
        return this;
    }

    /// Translate the transform current position by given coordinates.
    public Transform translate(float x, float y, float z) {
        this.position.add(x, y, z);
        return this;
    }

    /// Translate the transform current position by given vector.
    public Transform translate(Vec3 offset) {
        this.position.add(offset);
        return this;
    }

    /// Rotate the transform in world space rotation by given quaternion.
    public Transform rotate(Quaternion rot) {
        // World rotation: this.rotation = rot * this.rotation (premultiply)
        this.rotation.preMul(rot);
        return this;
    }

    /// Rotate the transform in world space by given angle about given _unit length_ axis.
    public Transform rotate(Vec3 unitAxis, float angleRad) {
        // World rotation: this.rotation = rot * this.rotation (premultiply)
        float halfAngle = angleRad * 0.5f;
        float s = (float) Math.sin(halfAngle);

        float qx = s * unitAxis.x;
        float qy = s * unitAxis.y;
        float qz = s * unitAxis.z;
        float qw = (float) Math.cos(halfAngle);
        this.rotation.preMul(qx, qy, qz, qw);
        return this;
    }

    private static final Vec3 UNIT_X = new Vec3(1, 0, 0);
    private static final Vec3 UNIT_Y = new Vec3(0, 1, 0);
    private static final Vec3 UNIT_Z = new Vec3(0, 0, 1);

    /// Rotate by `angleRad` about world X axis.
    public Transform rotateX(float angleRad) {
        return rotate(UNIT_X, angleRad);
    }

    /// Rotate by `angleRad` about world Y axis.
    public Transform rotateY(float angleRad) {
        return rotate(UNIT_Y, angleRad);
    }

    /// Rotate by `angleRad` about world Z axis.
    public Transform rotateZ(float angleRad) {
        return rotate(UNIT_Z, angleRad);
    }

    /// Scale by `x`, `y` and `z`.
    public Transform scaleBy(float x, float y, float z) {
        this.scale.scale(x, y, z);
        return this;
    }

    /// Scale by given vector's components.
    public Transform scaleBy(Vec3 scale) {
        this.scale.scale(scale.x, scale.y, scale.z);
        return this;
    }

    /// Scale uniformly by given factor.
    public Transform scaleBy(float uniformScale) {
        this.scale.scale(uniformScale);
        return this;
    }

    /// Makes this transform look at specified target.
    /// @param target the position of target
    /// @param worldUp the world up axis (typically 0,1,0)
    public Transform lookAt(Vec3 target, Vec3 worldUp) {
        // 1. Calculate direction to target
        //    direction = normalize(target - position)
        Vec3 direction = new Vec3()
                .set(target)
                .sub(position)
                .normalize();

        // 2. Compute the right vector
        //    right = normalize(direction x worldUp)
        Vec3 right = new Vec3()
                .set(direction)
                .cross(worldUp)
                .normalize();

        // 3. Handle case when direction is parallel to world up by picking perpendicular up vector
        if (right.lengthSqr() < MathUtil.EPSILON) {
            if (Math.abs(worldUp.y) < 0.9f) {
                // worldUp is not Y dominant use Y
                right.set(direction).cross(0, 1, 0).normalize();
            } else {
                // worldUp is Y dominant, use X
                right.set(direction).cross(1, 0, 0).normalize();
            }
        }

        // 4. Compute actual up vector
        Vec3 up = new Vec3()
                .set(right)
                .cross(direction)
                .normalize();

        //noinspection UnnecessaryLocalVariable
        Vec3 xBasis = right;
        //noinspection UnnecessaryLocalVariable
        Vec3 yBasis = up;
        Vec3 zBasis = direction.negate();   // negates the direction itself and returns reference to it

        float m00 = xBasis.x; float m01 = yBasis.x; float m02 = zBasis.x;
        float m10 = xBasis.y; float m11 = yBasis.y; float m12 = zBasis.y;
        float m20 = xBasis.z; float m21 = yBasis.z; float m22 = zBasis.z;

        rotation.setFromRotationMatrix(m00, m01, m02, m10, m11, m12, m20, m21, m22);

        return this;
    }

    /// Get mathematical basis vectors.
    /// @param destX - store the X vector (right). Can be null.
    /// @param destY - store the Y vector (up). Can be null.
    /// @param destZ - store the Z vector (backward, negated forward) Can be null.
    public void getBasisVectors(Vec3 destX, Vec3 destY, Vec3 destZ) {
        this.rotation.toBasis(destX, destY, destZ);
    }

    /// Get the right vector.
    /// @param right  store for right vector. If null new object is allocated.
    /// @return _right_ vector or, if _right_ is null, new Vec3
    public Vec3 getRightVector(Vec3 right) {
        if (right == null) {
            right = new Vec3();
        }
        this.rotation.toBasis(right, null, null);
        return right;
    }

    /// Get right vector. Always allocate.
    public Vec3 getRightVector() {
        return getRightVector(null);
    }

    /// Get the up vector.
    /// @param up store for up vector. If null new object is allocated.
    /// @return _up_ vector or, if _up_ is null, new Vec3
    public Vec3 getUpVector(Vec3 up) {
        if (up == null) {
            up = new Vec3();
        }
        this.rotation.toBasis(null, up, null);
        return up;
    }

    /// Get up vector. Always allocate.
    public Vec3 getUpVector() {
        return getUpVector(null);
    }

    /// Get the forward vector.
    /// @param forward store for forward vector. If null new object is allocated.
    /// @return _forward_ vector or, if _forward_ is null, new Vec3
    public Vec3 getForwardVector(Vec3 forward) {
        if (forward == null) {
            forward = new Vec3();
        }
        this.rotation.toBasis(null, null, forward);
        forward.negate();
        return forward;
    }

    /// Get the forward vector - always allocate.
    public Vec3 getForwardVector() {
        return getForwardVector(null);
    }

    public void toMatrix(Mat4 dest) {
        // Matrix transform order Scale -> Rotation -> Translation
        // Matrix = Translation * Rotation * Scale
        dest.m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*scale.x;
        dest.m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*scale.y;
        dest.m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*scale.z;
        dest.m03 = position.x;

        dest.m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*scale.x;
        dest.m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*scale.y;
        dest.m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*scale.z;
        dest.m13 = position.y;

        dest.m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*scale.x;
        dest.m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*scale.y;
        dest.m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*scale.z;
        dest.m23 = position.z;

        dest.m30 = 0;
        dest.m31 = 0;
        dest.m32 = 0;
        dest.m33 = 1;
    }

    public void toMatrixFloatArray(float[] dest, int offset) {
        //noinspection PointlessArithmeticExpression
        dest[offset + 0] = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*scale.x;
        dest[offset + 1] = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*scale.y;
        dest[offset + 2] = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*scale.z;
        dest[offset + 3] = position.x;

        dest[offset + 4] = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*scale.x;
        dest[offset + 5] = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*scale.y;
        dest[offset + 6] = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*scale.z;
        dest[offset + 7] = position.y;

        dest[offset + 8] = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*scale.x;
        dest[offset + 9] = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*scale.y;
        dest[offset + 10] = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*scale.z;
        dest[offset + 11] = position.z;

        dest[offset + 12] = 0;
        dest[offset + 13] = 0;
        dest[offset + 14] = 0;
        dest[offset + 15] = 1;
    }

    public void toInverseMatrix(Mat4 dest) {
        // Matrix transform order Scale -> Rotation -> Translation
        // Matrix = Translation * Rotation * Scale
        // Inverse = Inverse(Scale) * Inverse(Rotation) * Inverse(Translation)

        // Inverse(Scale) = ScaleM(1/sx, 1/sy, 1/sz)
        // Inverse(Rotation) = Transpose(Inner3x3(Rotation))
        // Inverse(Translation) = TranslationM(-x, -y, -z)

        float qx = rotation.x;
        float qy = rotation.y;
        float qz = rotation.z;
        float qw = rotation.w;
        float tx = position.x;
        float ty = position.y;
        float tz = position.z;

        float oneOverX = 1.0f / scale.x;
        dest.m00 = (1 - 2 * (qy*qy + qz*qz)) * oneOverX;
        dest.m01 = (2 * (qx * qy + qw * qz)) * oneOverX;
        dest.m02 = (2 * (-qw * qy + qx * qz)) * oneOverX;
        dest.m03 = ((-1 + 2*qy*qy + 2*qz*qz)*tx - 2*(qx*qy*ty + qw*qz*ty - qw*qy*tz + qx*qz*tz)) * oneOverX;

        float oneOverY = 1.0f / scale.y;
        dest.m10 = (2*qx*qy - 2*qw*qz) * oneOverY;
        dest.m11 = (1 - 2*(qx*qx + qz*qz)) * oneOverY;
        dest.m12 = (2*(qw*qx + qy*qz)) * oneOverY;
        dest.m13 = -(2*qx*qy*tx - 2*qw*qz*tx + ty - 2*qx*qx*ty - 2*qz*qz*ty + 2*qw*qx*tz + 2*qy*qz*tz) * oneOverY;

        float oneOverZ = 1.0f / scale.z;
        dest.m20 = 2*(qw*qy + qx*qz) * oneOverZ;
        dest.m21 = 2*(-qw*qx + qy*qz) * oneOverZ;
        dest.m22 = 1 - 2*(qx*qx + qy*qy) * oneOverZ;
        dest.m23 = -(2*qw*qy*tx + 2*qx*qz*tx - 2*qw*qx*ty + 2*qy*qz*ty + tz - 2*qx*qx*tz - 2*qy*qy*tz) * oneOverZ;

        dest.m30 = 0;
        dest.m31 = 0;
        dest.m32 = 0;
        dest.m33 = 1;
    }

    public void toInverseMatrixFloatArray(float[] dest, int offset) {
        float qx = rotation.x;
        float qy = rotation.y;
        float qz = rotation.z;
        float qw = rotation.w;
        float tx = position.x;
        float ty = position.y;
        float tz = position.z;

        float oneOverX = 1.0f / scale.x;
        //noinspection PointlessArithmeticExpression
        dest[offset + 0] = (1 - 2 * (qy*qy + qz*qz)) * oneOverX;
        dest[offset + 1] = (2 * (qx * qy + qw * qz)) * oneOverX;
        dest[offset + 2] = (2 * (-qw * qy + qx * qz)) * oneOverX;
        dest[offset + 3] = ((-1 + 2*qy*qy + 2*qz*qz)*tx - 2*(qx*qy*ty + qw*qz*ty - qw*qy*tz + qx*qz*tz)) * oneOverX;

        float oneOverY = 1.0f / scale.y;
        dest[offset + 4] = (2*qx*qy - 2*qw*qz) * oneOverY;
        dest[offset + 5] = (1 - 2*(qx*qx + qz*qz)) * oneOverY;
        dest[offset + 6] = (2*(qw*qx + qy*qz)) * oneOverY;
        dest[offset + 7] = -(2*qx*qy*tx - 2*qw*qz*tx + ty - 2*qx*qx*ty - 2*qz*qz*ty + 2*qw*qx*tz + 2*qy*qz*tz) * oneOverY;

        float oneOverZ = 1.0f / scale.z;
        dest[offset + 8] = 2*(qw*qy + qx*qz) * oneOverZ;
        dest[offset + 9] = 2*(-qw*qx + qy*qz) * oneOverZ;
        dest[offset + 10] = 1 - 2*(qx*qx + qy*qy) * oneOverZ;
        dest[offset + 11] = -(2*qw*qy*tx + 2*qx*qz*tx - 2*qw*qx*ty + 2*qy*qz*ty + tz - 2*qx*qx*tz - 2*qy*qy*tz) * oneOverZ;

        dest[offset + 12] = 0;
        dest[offset + 13] = 0;
        dest[offset + 14] = 0;
        dest[offset + 15] = 1;
    }


    /// Interpolate between two transforms
    public static void interpolate(Transform a, Transform b, float t, Transform dest) {
        Vec3.lerp(a.position, b.position, t, dest.position);
        Vec3.lerp(a.scale, b.scale, t, dest.scale);
        Quaternion.slerp(a.rotation, b.rotation, t, dest.rotation);
    }


    @Override
    public String toString() {
        return "Transform( position = " + position + ", scale = " + scale + ", rotation = " + rotation + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transform transform = (Transform) o;
        return Objects.equals(position, transform.position) && Objects.equals(scale, transform.scale) && Objects.equals(rotation, transform.rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, scale, rotation);
    }
}
