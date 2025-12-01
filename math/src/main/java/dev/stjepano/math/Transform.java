package dev.stjepano.math;

import java.util.Objects;

import static dev.stjepano.math.MathUtil.*;

/// A transform represents position - rotation - scale of some object in 3D space.
///
/// Mathematically it is expressed as T * R * S * v where T is translation matrix, R is rotation matrix, S is scale matrix
/// and v is vector that is transformed. This means that any vector is transformed in following order: Scale -> Rotate -> Translate.
///
/// You can express translation and rotation globally or in local object space or in local mathematical space.
///
/// * local object space is defined by right, up and forward vectors, by convention forward vector in identity transform
///   is in the direction of -Z. This space is used for object movement.
/// * local mathematical space is defined by right, up and backward vectors. The right and up are same as in local object
///   space but backward is in +Z direction (points to camera). This space is used when construction meshes.
///
/// Because of how transformations are applied to objects, the `scale` is always in local space.
///
/// **NOTE** that normals have special transform functions, they can not be transformed in same way as positions.
public final class Transform {
    private final Vec3 position = new Vec3();
    private final Vec3 scale = new Vec3(1, 1, 1);
    private final Quaternion rotation = new Quaternion();

    /// A position of the object in space relative to parent.
    /// @return reference to _position_ vector.
    public Vec3 position() {
        return this.position;
    }

    /// Scale of the object. Always local scale.
    /// @return reference to _scale_ vector.
    public Vec3 scale() {
        return this.scale;
    }

    /// Rotation of the object in space relative to parent.
    /// @return reference to _rotation_ vector.
    public Quaternion rotation() {
        return this.rotation;
    }

    /// Set this transform to identity (position = 0,0,0; scale = 1,1,1; rotation = no rotation).
    public Transform setIdentity() {
        this.position.set(0, 0, 0);
        this.scale.set(1, 1, 1);
        this.rotation.set(0, 0, 0, 1);
        return this;
    }

    /// Set position to 3D vector given by its coordinates.
    /// @param x x component of the 3D vector
    /// @param y y component of the 3D vector
    /// @param z z component of the 3D vector
    /// @return reference to this for chaining
    public Transform setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        return this;
    }

    /// Set position to the specified 3D vector.
    /// @param pos 3D vector which is copied to internal _position_
    /// @return reference to this for chaining
    public Transform setPosition(Vec3 pos) {
        this.position.set(pos);
        return this;
    }

    /// Set rotation to the specified quaternion. Make sure that quaternion represents rotation, otherwise you
    /// will get undefined behaviour.
    /// @param rot rotation expressed with quaternion, must be rotation quaternion
    /// @return reference to this for chaining
    public Transform setRotation(Quaternion rot) {
        this.rotation.set(rot);
        return this;
    }

    /// Set rotation to be rotation of `angleRad` radians about X axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform setRotationX(float angleRad) {
        this.rotation.setRotationX(angleRad);
        return this;
    }

    /// Set rotation to be rotation of `angleRad` radians about Y axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform setRotationY(float angleRad) {
        this.rotation.setRotationY(angleRad);
        return this;
    }

    /// Set rotation to be rotation of `angleRad` radians about Z axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform setRotationZ(float angleRad) {
        this.rotation.setRotationZ(angleRad);
        return this;
    }

    /// Set rotation to be rotation of `angleRad` about the specified `unitAxis`. Make sure that `unitAxis` is of
    /// unit length otherwise you will get undefined behaviour.
    /// @param unitAxis unit vector representing rotation axis
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform setRotation(Vec3 unitAxis, float angleRad) {
        this.rotation.setAxisAngle(unitAxis, angleRad);
        return this;
    }

    /// Set rotation from euler angles (order of rotation Yaw-Pitch-Roll).
    /// @param pitchRad pitch angle in radians
    /// @param yawRad yaw angle in radians
    /// @param rollRad roll angle in radians
    /// @return reference to this for chaining
    public Transform setRotationEulerYXZ(float pitchRad, float yawRad, float rollRad) {
        this.rotation.setEulerYXZ(pitchRad, yawRad, rollRad);
        return this;
    }

    /// Set rotation from euler angles (order of rotation Roll-Pitch-Yaw).
    /// @param pitchRad pitch angle in radians
    /// @param yawRad yaw angle in radians
    /// @param rollRad roll angle in radians
    /// @return reference to this for chaining
    public Transform setRotationEulerZXY(float pitchRad, float yawRad, float rollRad) {
        this.rotation.setEulerZXY(pitchRad, yawRad, rollRad);
        return this;
    }

    /// Set rotation from euler angles (order of rotation Roll-Yaw-Pitch).
    /// @param pitchRad pitch angle in radians
    /// @param yawRad yaw angle in radians
    /// @param rollRad roll angle in radians
    /// @return reference to this for chaining
    public Transform setRotationEulerZYX(float pitchRad, float yawRad, float rollRad) {
        this.rotation.setEulerZYX(pitchRad, yawRad, rollRad);
        return this;
    }

    /// Set scale.
    /// @param x scaling factor on X axis
    /// @param y scaling factor on Y axis
    /// @param z scaling factor on Z axis
    /// @return reference to this for chaining
    public Transform setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
        return this;
    }

    /// Set scale uniformly. All axes have same scale.
    /// @param uniformScale uniform scale
    /// @return reference to this for chaining
    public Transform setScale(float uniformScale) {
        this.scale.set(uniformScale, uniformScale, uniformScale);
        return this;
    }

    /// Global translate the transform with given vector.
    /// @param x the X component of the vector
    /// @param y the Y component of the vector
    /// @param z the Z component of the vector
    /// @return reference to this for chaining
    public Transform translate(float x, float y, float z) {
        this.position.add(x, y, z);
        return this;
    }

    /// Local (object-space) translate the transform with given vector.
    /// @param x the X component of the vector
    /// @param y the Y component of the vector
    /// @param z the Z component of the vector
    /// @return reference to this for chaining
    public Transform translateLocal(float x, float y, float z) {
        translateLocalBasis(x, y, -z);
        return this;
    }

    /// Local (mathematical object-space) translate the transform with given vector.
    /// @param x the X component of the vector
    /// @param y the Y component of the vector
    /// @param z the Z component of the vector
    /// @return reference to this for chaining
    public Transform translateLocalBasis(float x, float y, float z) {
        // 1. conjugate_rotation = conjugate(rotation)
        float cqx = -rotation.x;
        float cqy = -rotation.y;
        float cqz = -rotation.z;
        float cqw = rotation.w;

        // 2. translation_as_quat = quaternion(x, y, z, 0)
        float vqx = x;
        float vqy = y;
        float vqz = z;
        float vqw = 0.0f;

        // 3. temp = rotation * translation_as_quat
        float tx = rotation.w * vqx + rotation.x * vqw + rotation.y * vqz - rotation.z * vqy;
        float ty = rotation.w * vqy + rotation.y * vqw + rotation.z * vqx - rotation.x * vqz;
        float tz = rotation.w * vqz + rotation.z * vqw + rotation.x * vqy - rotation.y * vqx;
        float tw = rotation.w * vqw - rotation.x * vqx - rotation.y * vqy - rotation.z * vqz;

        // 4. rotOffset = temp * conjugate_rotation
        float rotOffsetX = tw * cqx + tx * cqw + ty * cqz - tz * cqy;
        float rotOffsetY = tw * cqy + ty * cqw + tz * cqx - tx * cqz;
        float rotOffsetZ = tw * cqz + tz * cqw + tx * cqy - ty * cqx;


        // 3. position += rotOffset
        this.position.x += rotOffsetX;
        this.position.y += rotOffsetY;
        this.position.z += rotOffsetZ;

        return this;
    }

    /// Global translate the transform with given vector.
    /// @param offset the translation vector
    /// @return reference to this for chaining
    public Transform translate(Vec3 offset) {
        this.position.add(offset);
        return this;
    }

    /// Local (object-space) translate the transform with given vector.
    /// @param offset the translation vector
    /// @return reference to this for chaining
    public Transform translateLocal(Vec3 offset) {
        this.translateLocal(offset.x, offset.y, offset.z);
        return this;
    }

    /// Local (mathematical object-space) translate the transform with given vector.
    /// @param offset the translation vector
    /// @return reference to this for chaining
    public Transform translateLocalBasis(Vec3 offset) {
        this.translateLocalBasis(offset.x, offset.y, offset.z);
        return this;
    }

    /// Global rotate the transform.
    /// @param rot rotation quaternion
    /// @return reference to this for chaining
    public Transform rotate(Quaternion rot) {
        // World rotation: this.rotation = rot * this.rotation (premultiply)
        this.rotation.preMul(rot);
        return this;
    }

    /// Local (object-space) rotate the transform.
    /// @param rot rotation quaternion
    /// @return reference to this for chaining
    public Transform rotateLocal(Quaternion rot) {
        // Convert to axis angle and use that
        this.rotation.mul(rot.x, rot.y, -rot.z, rot.w);
        return this;
    }

    /// Local (mathematical object-space) rotate the transform.
    /// @param rot rotation quaternion
    /// @return reference to this for chaining
    public Transform rotateLocalBasis(Quaternion rot) {
        // Local rotation: this.rotation = this.rotation * rot
        this.rotation.mul(rot);
        return this;
    }

    /// Global rotate the transform.
    /// @param unitAxis unit vector representing the rotation axis
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
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

    /// Local (object-space) rotate the transform.
    /// @param unitAxis unit vector representing the rotation axis
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocal(Vec3 unitAxis, float angleRad) {
        float halfAngle = angleRad * 0.5f;
        float s = (float) Math.sin(halfAngle);

        float qx = s * unitAxis.x;
        float qy = s * unitAxis.y;
        float qz = s * -unitAxis.z;
        float qw = (float) Math.cos(halfAngle);
        this.rotation.mul(qx, qy, qz, qw);
        return this;
    }

    /// Local (mathematical object-space) rotate the transform.
    /// @param unitAxis unit vector representing the rotation axis
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalBasis(Vec3 unitAxis, float angleRad) {
        float halfAngle = angleRad * 0.5f;
        float s = (float) Math.sin(halfAngle);

        float qx = s * unitAxis.x;
        float qy = s * unitAxis.y;
        float qz = s * unitAxis.z;
        float qw = (float) Math.cos(halfAngle);
        this.rotation.mul(qx, qy, qz, qw);
        return this;
    }

    private static final Vec3 UNIT_X = new Vec3(1, 0, 0);
    private static final Vec3 UNIT_Y = new Vec3(0, 1, 0);
    private static final Vec3 UNIT_Z = new Vec3(0, 0, 1);

    /// Global rotate the transform about X axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateX(float angleRad) {
        return rotate(UNIT_X, angleRad);
    }

    /// Global rotate the transform about Y axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateY(float angleRad) {
        return rotate(UNIT_Y, angleRad);
    }

    /// Global rotate the transform about Z axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateZ(float angleRad) {
        return rotate(UNIT_Z, angleRad);
    }

    /// Local (object-space) rotate the transform about X axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalX(float angleRad) {
        return rotateLocal(UNIT_X, angleRad);
    }

    /// Local (mathematical object-space) rotate the transform about X axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalBasisX(float angleRad) {
        return rotateLocalBasis(UNIT_X, angleRad);
    }

    /// Local (object-space) rotate the transform about Y axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalY(float angleRad) {
        return rotateLocal(UNIT_Y, angleRad);
    }

    /// Local (mathematical object-space) rotate the transform about Y axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalBasisY(float angleRad) {
        return rotateLocalBasis(UNIT_Y, angleRad);
    }

    /// Local (object-space) rotate the transform about Z axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalZ(float angleRad) {
        return rotateLocal(UNIT_Z, angleRad);
    }

    /// Local (mathematical object-space) rotate the transform about Z axis.
    /// @param angleRad angle in radians
    /// @return reference to this for chaining
    public Transform rotateLocalBasisZ(float angleRad) {
        return rotateLocalBasis(UNIT_Z, angleRad);
    }

    /// Scale the transform by give scaling factors. Always local space.
    /// @param x scaling factor on X axis
    /// @param y scaling factor on Y axis
    /// @param z scaling factor on Z axis
    /// @return reference to this for chaining
    public Transform scaleBy(float x, float y, float z) {
        this.scale.scale(x, y, z);
        return this;
    }

    /// Scale the transform by give scaling factors. Always local space.
    /// @param scale 3D vector with scaling factors
    /// @return reference to this for chaining
    public Transform scaleBy(Vec3 scale) {
        this.scale.scale(scale.x, scale.y, scale.z);
        return this;
    }

    /// Uniformly scale the transform by give scaling factors. Always local space.
    /// @param uniformScale uniform scaling factor
    /// @return reference to this for chaining
    public Transform scaleBy(float uniformScale) {
        this.scale.scale(uniformScale);
        return this;
    }

    /// Make this transform look at specified target (works in object local space).
    /// @param target the position of the target
    /// @param worldUp global UP axis
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
        if (right.lengthSqr() < EPSILON8) {
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

    /// Get basis vectors (mathematical local space)
    /// @param destX the X vector (right from object's perspective)
    /// @param destY the Y vector (up from object's perspective)
    /// @param destZ the Z vector (backward from object's perspective)
    public void getBasisVectors(Vec3 destX, Vec3 destY, Vec3 destZ) {
        this.rotation.toBasis(destX, destY, destZ);
    }

    /// Get right vector (no allocation). This is right vector from object perspective and will equal to X basis
    /// vector.
    /// @param right the vector where result is stored
    /// @return reference to `right`
    public Vec3 getRightVector(Vec3 right) {
        if (right == null) {
            right = new Vec3();
        }
        this.rotation.toBasis(right, null, null);
        return right;
    }

    /// Get right vector (allocation).
    /// @return reference to new Vec3
    public Vec3 getRightVector() {
        return getRightVector(null);
    }

    /// Get up vector (no allocation). This is up vector from object perspective and will equal to Y basis vector.
    /// @param up the vector where the result is stored
    /// @return reference to `up`
    public Vec3 getUpVector(Vec3 up) {
        if (up == null) {
            up = new Vec3();
        }
        this.rotation.toBasis(null, up, null);
        return up;
    }

    /// Get up vector (allocation).
    /// @return reference to new Vec3
    public Vec3 getUpVector() {
        return getUpVector(null);
    }

    /// Get forward vector (no allocation). This is forward vector from object perspective and will equal
    /// negative Z basis vector.
    /// @param forward the vector where the result is stored
    /// @return reference to `forward`
    public Vec3 getForwardVector(Vec3 forward) {
        if (forward == null) {
            forward = new Vec3();
        }
        this.rotation.toBasis(null, null, forward);
        forward.negate();
        return forward;
    }

    /// Get forward vector (allocation)
    /// @return reference to new Vec3
    public Vec3 getForwardVector() {
        return getForwardVector(null);
    }

    /// Transform a _position_ to space given by this _transform_. Position is transformed in-place.
    ///
    /// **NOTE**: not efficient for arrays
    /// @param pos the position that is transformed in-place.
    /// @return reference to transformed `pos`
    public Vec3 transformPosition(Vec3 pos) {
        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*scale.x;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*scale.y;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*scale.z;
        final float m03 = this.position.x;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*scale.x;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*scale.y;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*scale.z;
        final float m13 = this.position.y;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*scale.x;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*scale.y;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*scale.z;
        final float m23 = this.position.z;

        final float nx = m00 * pos.x + m01 * pos.y + m02 * pos.z + m03;
        final float ny = m10 * pos.x + m11 * pos.y + m12 * pos.z + m13;
        final float nz = m20 * pos.x + m21 * pos.y + m22 * pos.z + m23;

        pos.x = nx;
        pos.y = ny;
        pos.z = nz;
        return pos;
    }

    /// Transform an array of _positions_ to space given by this _transform_. Positions are transformed in-place.
    /// @param positions an array of positions that are transformed in-place.
    /// @return reference to `positions` array.
    public Vec3[] transformPosition(Vec3[] positions) {
        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*scale.x;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*scale.y;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*scale.z;
        final float m03 = position.x;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*scale.x;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*scale.y;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*scale.z;
        final float m13 = position.y;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*scale.x;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*scale.y;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*scale.z;
        final float m23 = position.z;

        for (Vec3 p : positions) {
            final float nx = m00 * p.x + m01 * p.y + m02 * p.z + m03;
            final float ny = m10 * p.x + m11 * p.y + m12 * p.z + m13;
            final float nz = m20 * p.x + m21 * p.y + m22 * p.z + m23;

            p.x = nx;
            p.y = ny;
            p.z = nz;
        }
        return positions;
    }

    /// Transforms positions in vertex data array. Designed for raw data that can be interleaved. Transform is
    /// done in-place.
    ///
    /// #### Example
    /// Lets say you have interleaved vertex data, each vertex 3 component position followed by 2 component texture coordinate.
    /// You want to transform all positions.
    /// ```java
    /// float[] vertexData = ...;
    /// transformer.transformPosition(vertexData, vertexData.length/5, 0, 5);
    /// ```
    ///
    /// @param positionArray reference to vertex data with position data
    /// @param count number of vertices to process
    /// @param offset array offset to first component of first position
    /// @param stride number of components per vertex
    public void transformPosition(float[] positionArray, int count, int offset, int stride) {
        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*scale.x;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*scale.y;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*scale.z;
        final float m03 = position.x;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*scale.x;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*scale.y;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*scale.z;
        final float m13 = position.y;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*scale.x;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*scale.y;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*scale.z;
        final float m23 = position.z;

        for (int index = 0; index < count; index++) {
            float px = positionArray[offset + (index * stride)];
            float py = positionArray[offset + (index * stride) + 1];
            float pz = positionArray[offset + (index * stride) + 2];

            final float nx = m00 * px + m01 * py + m02 * pz + m03;
            final float ny = m10 * px + m11 * py + m12 * pz + m13;
            final float nz = m20 * px + m21 * py + m22 * pz + m23;

            positionArray[offset + (index * stride)]     = nx;
            positionArray[offset + (index * stride) + 1] = ny;
            positionArray[offset + (index * stride) + 2] = nz;
        }
    }

    /// Transform a _normal_ to space given by this _transform_. Normal is transformed in-place.
    ///
    /// **NOTE**: not efficient for arrays
    /// @param normal the normal that is transformed
    /// @return reference to transformed `normal`
    public Vec3 transformNormal(Vec3 normal) {
        // Normals are transformed by inverse transpose of the 3x3 rotation-scale portion of
        // the matrix.
        // We have R * S -> we need inverse transpose Transpose(Inverse(R * S))
        // Inverse(R * S) = Inverse(S) * Inverse(R), since R is orthogonal then Inverse(S) * Transpose(R)
        // So we have Inverse(R * S) = Inverse(S) * Transpose(R)
        // Now we need Transpose(Inverse(R*S)) => Transpose(Inverse(S) * Transpose(R))
        //     => Transpose(Transpose(R)) * Transpose(Inverse(S))
        //     => R * Transpose(Inverse(S))
        // Since S is diagonal, Inverse(S) is diagonal we can
        //     => R * Inverse(S)
        // Which is beautifully simple math

        final float oneOverSx = 1.0f / scale.x;
        final float oneOverSy = 1.0f / scale.y;
        final float oneOverSz = 1.0f / scale.z;

        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*oneOverSx;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*oneOverSy;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*oneOverSz;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*oneOverSx;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*oneOverSy;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*oneOverSz;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*oneOverSx;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*oneOverSy;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*oneOverSz;

        final float nx = m00 * normal.x + m01 * normal.y + m02 * normal.z;
        final float ny = m10 * normal.x + m11 * normal.y + m12 * normal.z;
        final float nz = m20 * normal.x + m21 * normal.y + m22 * normal.z;

        normal.x = nx;
        normal.y = ny;
        normal.z = nz;
        return normal.normalize();
    }

    /// Transform an array of _normals_ to space given by this _transform_. Normals are transformed in-place.
    /// @param normals an array of normals that are transformed in-place.
    /// @return reference to `normals` array.
    public Vec3[] transformNormal(Vec3[] normals) {
        final float oneOverSx = 1.0f / scale.x;
        final float oneOverSy = 1.0f / scale.y;
        final float oneOverSz = 1.0f / scale.z;

        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*oneOverSx;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*oneOverSy;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*oneOverSz;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*oneOverSx;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*oneOverSy;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*oneOverSz;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*oneOverSx;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*oneOverSy;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*oneOverSz;

        for (Vec3 normal : normals) {
            final float nx = m00 * normal.x + m01 * normal.y + m02 * normal.z;
            final float ny = m10 * normal.x + m11 * normal.y + m12 * normal.z;
            final float nz = m20 * normal.x + m21 * normal.y + m22 * normal.z;

            normal.x = nx;
            normal.y = ny;
            normal.z = nz;
            normal.normalize();
        }
        return normals;
    }

    /// Transforms normals in vertex data array. Designed for raw data that can be interleaved. Transform is
    /// done in-place.
    ///
    /// #### Example
    /// Lets say you have interleaved vertex data, each vertex 3 component position followed by 3 component normal.
    /// You want to transform all normals.
    /// ```java
    /// float[] vertexData = ...;
    /// transformer.transformNormal(vertexData, vertexData.length/6, 3, 6);
    /// ```
    ///
    /// @param normalArray reference to vertex data with normal data
    /// @param count number of vertices to process
    /// @param offset array offset to first component of first normal
    /// @param stride number of components per vertex
    public void transformNormal(float[] normalArray, int count, int offset, int stride) {
        final float oneOverSx = 1.0f / scale.x;
        final float oneOverSy = 1.0f / scale.y;
        final float oneOverSz = 1.0f / scale.z;

        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*oneOverSx;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*oneOverSy;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*oneOverSz;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*oneOverSx;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*oneOverSy;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*oneOverSz;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*oneOverSx;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*oneOverSy;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*oneOverSz;

        for (int index = 0; index < count; index++) {
            int baseIndex = offset + (index * stride);
            final float normx = normalArray[baseIndex];
            final float normy = normalArray[baseIndex + 1];
            final float normz = normalArray[baseIndex + 2];

            final float nx = m00 * normx + m01 * normy + m02 * normz;
            final float ny = m10 * normx + m11 * normy + m12 * normz;
            final float nz = m20 * normx + m21 * normy + m22 * normz;

            final float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
            if (len > EPSILON8) {
                normalArray[baseIndex] = nx / len;
                normalArray[baseIndex + 1] = ny / len;
                normalArray[baseIndex + 2] = nz / len;
            }
        }
    }

    /// Same as {@link Transform#transformNormal(Vec3)} but does not normalize the normal.
    /// @param normal reference to normal that is transformed in-place
    /// @return reference to `normal`
    public Vec3 transformNormalUnnormalized(Vec3 normal) {
        // See transformNormal for math explanation.

        final float oneOverSx = 1.0f / scale.x;
        final float oneOverSy = 1.0f / scale.y;
        final float oneOverSz = 1.0f / scale.z;

        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*oneOverSx;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*oneOverSy;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*oneOverSz;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*oneOverSx;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*oneOverSy;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*oneOverSz;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*oneOverSx;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*oneOverSy;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*oneOverSz;

        final float nx = m00 * normal.x + m01 * normal.y + m02 * normal.z;
        final float ny = m10 * normal.x + m11 * normal.y + m12 * normal.z;
        final float nz = m20 * normal.x + m21 * normal.y + m22 * normal.z;

        normal.x = nx;
        normal.y = ny;
        normal.z = nz;
        return normal;
    }

    /// Same as {@link Transform#transformNormal(Vec3[])} but does not normalize the normals.
    /// @param normals an array of normals that are transformed in-place.
    /// @return reference to `normals` array.
    public Vec3[] transformNormalUnnormalized(Vec3[] normals) {
        final float oneOverSx = 1.0f / scale.x;
        final float oneOverSy = 1.0f / scale.y;
        final float oneOverSz = 1.0f / scale.z;

        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*oneOverSx;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*oneOverSy;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*oneOverSz;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*oneOverSx;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*oneOverSy;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*oneOverSz;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*oneOverSx;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*oneOverSy;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*oneOverSz;

        for (Vec3 normal : normals) {
            final float nx = m00 * normal.x + m01 * normal.y + m02 * normal.z;
            final float ny = m10 * normal.x + m11 * normal.y + m12 * normal.z;
            final float nz = m20 * normal.x + m21 * normal.y + m22 * normal.z;

            normal.x = nx;
            normal.y = ny;
            normal.z = nz;
        }
        return normals;
    }

    /// Same as {@link Transform#transformNormal(float[], int, int, int)} but does not normalize the normals.
    /// @param normalArray reference to vertex data with normal data
    /// @param count number of vertices to process
    /// @param offset array offset to first component of first normal
    /// @param stride number of components per vertex
    public void transformNormalUnnormalized(float[] normalArray, int count, int offset, int stride) {
        final float oneOverSx = 1.0f / scale.x;
        final float oneOverSy = 1.0f / scale.y;
        final float oneOverSz = 1.0f / scale.z;

        final float m00 = (1 - 2*(rotation.y * rotation.y + rotation.z * rotation.z))*oneOverSx;
        final float m01 = 2*(rotation.x*rotation.y - rotation.w*rotation.z)*oneOverSy;
        final float m02 = 2*(rotation.w*rotation.y + rotation.x*rotation.z)*oneOverSz;

        final float m10 = 2*(rotation.x*rotation.y + rotation.w*rotation.z)*oneOverSx;
        final float m11 = (1 - 2*(rotation.x*rotation.x + rotation.z*rotation.z))*oneOverSy;
        final float m12 = 2*((-rotation.w)*rotation.x + rotation.y*rotation.z)*oneOverSz;

        final float m20 = 2*((-rotation.w)*rotation.y + rotation.x*rotation.z)*oneOverSx;
        final float m21 = 2*(rotation.w*rotation.x + rotation.y*rotation.z)*oneOverSy;
        final float m22 = (1 - 2*(rotation.x*rotation.x + rotation.y*rotation.y))*oneOverSz;

        for (int index = 0; index < count; index++) {
            final float normx = normalArray[offset + (index * stride)];
            final float normy = normalArray[offset + (index * stride) + 1];
            final float normz = normalArray[offset + (index * stride) + 2];

            final float nx = m00 * normx + m01 * normy + m02 * normz;
            final float ny = m10 * normx + m11 * normy + m12 * normz;
            final float nz = m20 * normx + m21 * normy + m22 * normz;

            normalArray[offset + (index * stride)] = nx;
            normalArray[offset + (index * stride) + 1] = ny;
            normalArray[offset + (index * stride) + 2] = nz;
        }
    }

    /// Convert the transform into transform matrix (4x4).
    /// @param dest Mat4 where result is stored
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

    /// Convert the transform into transform matrix and store its coefficients into an array.
    /// @param dest destination array
    /// @param offset offset in array where first element is stored
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

    /// Convert the transform into inverse transform matrix (4x4).
    /// @param dest Mat4 where result is stored
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

        float oneOverX = safeOneOver(scale.x, EPSILON6);
        dest.m00 = (1 - 2 * (qy*qy + qz*qz)) * oneOverX;
        dest.m01 = (2 * (qx * qy + qw * qz)) * oneOverX;
        dest.m02 = (2 * (-qw * qy + qx * qz)) * oneOverX;
        dest.m03 = ((-1 + 2*qy*qy + 2*qz*qz)*tx - 2*(qx*qy*ty + qw*qz*ty - qw*qy*tz + qx*qz*tz)) * oneOverX;

        float oneOverY = safeOneOver(scale.y, EPSILON6);
        dest.m10 = (2*qx*qy - 2*qw*qz) * oneOverY;
        dest.m11 = (1 - 2*(qx*qx + qz*qz)) * oneOverY;
        dest.m12 = (2*(qw*qx + qy*qz)) * oneOverY;
        dest.m13 = -(2*qx*qy*tx - 2*qw*qz*tx + ty - 2*qx*qx*ty - 2*qz*qz*ty + 2*qw*qx*tz + 2*qy*qz*tz) * oneOverY;

        float oneOverZ = safeOneOver(scale.z, EPSILON6);
        dest.m20 = 2*(qw*qy + qx*qz) * oneOverZ;
        dest.m21 = 2*(-qw*qx + qy*qz) * oneOverZ;
        dest.m22 = (1 - 2*(qx*qx + qy*qy)) * oneOverZ;
        dest.m23 = -(2*qw*qy*tx + 2*qx*qz*tx - 2*qw*qx*ty + 2*qy*qz*ty + tz - 2*qx*qx*tz - 2*qy*qy*tz) * oneOverZ;

        dest.m30 = 0;
        dest.m31 = 0;
        dest.m32 = 0;
        dest.m33 = 1;
    }

    /// Convert the transform into inverse transform matrix and store its coefficients into an array.
    /// @param dest destination array
    /// @param offset offset in array where first element is stored
    public void toInverseMatrixFloatArray(float[] dest, int offset) {
        float qx = rotation.x;
        float qy = rotation.y;
        float qz = rotation.z;
        float qw = rotation.w;
        float tx = position.x;
        float ty = position.y;
        float tz = position.z;

        float oneOverX = safeOneOver(scale.x, EPSILON6);
        //noinspection PointlessArithmeticExpression
        dest[offset + 0] = (1 - 2 * (qy*qy + qz*qz)) * oneOverX;
        dest[offset + 1] = (2 * (qx * qy + qw * qz)) * oneOverX;
        dest[offset + 2] = (2 * (-qw * qy + qx * qz)) * oneOverX;
        dest[offset + 3] = ((-1 + 2*qy*qy + 2*qz*qz)*tx - 2*(qx*qy*ty + qw*qz*ty - qw*qy*tz + qx*qz*tz)) * oneOverX;

        float oneOverY = safeOneOver(scale.y, EPSILON6);
        dest[offset + 4] = (2*qx*qy - 2*qw*qz) * oneOverY;
        dest[offset + 5] = (1 - 2*(qx*qx + qz*qz)) * oneOverY;
        dest[offset + 6] = (2*(qw*qx + qy*qz)) * oneOverY;
        dest[offset + 7] = -(2*qx*qy*tx - 2*qw*qz*tx + ty - 2*qx*qx*ty - 2*qz*qz*ty + 2*qw*qx*tz + 2*qy*qz*tz) * oneOverY;

        float oneOverZ = safeOneOver(scale.z, EPSILON6);
        dest[offset + 8] = 2*(qw*qy + qx*qz) * oneOverZ;
        dest[offset + 9] = 2*(-qw*qx + qy*qz) * oneOverZ;
        dest[offset + 10] = (1 - 2*(qx*qx + qy*qy)) * oneOverZ;
        dest[offset + 11] = -(2*qw*qy*tx + 2*qx*qz*tx - 2*qw*qx*ty + 2*qy*qz*ty + tz - 2*qx*qx*tz - 2*qy*qy*tz) * oneOverZ;

        dest[offset + 12] = 0;
        dest[offset + 13] = 0;
        dest[offset + 14] = 0;
        dest[offset + 15] = 1;
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
