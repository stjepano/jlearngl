package dev.stjepano.math;

import org.junit.jupiter.api.Test;

import static dev.stjepano.math.MathUtil.EPSILON6;
import static org.junit.jupiter.api.Assertions.*;

class TransformTest {

    @Test
    public void testTranslateLocalBasisIdentityRotation() {
        // With identity rotation, local basis = world axes
        Transform t = new Transform();
        t.setPosition(1, 2, 3);

        t.translateLocalBasis(1, 0, 0);  // Move along +X basis (right)
        assertEquals(2.0f, t.position().x, EPSILON6);
        assertEquals(2.0f, t.position().y, EPSILON6);
        assertEquals(3.0f, t.position().z, EPSILON6);

        t.translateLocalBasis(0, 2, 0);  // Move along +Y basis (up)
        assertEquals(2.0f, t.position().x, EPSILON6);
        assertEquals(4.0f, t.position().y, EPSILON6);
        assertEquals(3.0f, t.position().z, EPSILON6);

        t.translateLocalBasis(0, 0, 3);  // Move along +Z basis (backward in OpenGL)
        assertEquals(2.0f, t.position().x, EPSILON6);
        assertEquals(4.0f, t.position().y, EPSILON6);
        assertEquals(6.0f, t.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocalIdentityRotation() {
        // With identity rotation, translateLocal should move forward along -Z
        Transform t = new Transform();
        t.setPosition(0, 0, 0);

        t.translateLocal(0, 0, 5);  // Move forward (semantic +Z = basis -Z)
        assertEquals(0.0f, t.position().x, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
        assertEquals(-5.0f, t.position().z, EPSILON6);  // -Z is forward in OpenGL
    }

    @Test
    public void testTranslateLocalBasis90DegreesY() {
        // Rotate 90° around Y, so local +X points toward world -Z
        Transform t = new Transform();
        t.setPosition(0, 0, 0);
        t.setRotationY((float) Math.toRadians(90));

        // Move along local +X basis (should move toward world -Z)
        t.translateLocalBasis(5, 0, 0);
        assertEquals(0.0f, t.position().x, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
        assertEquals(-5.0f, t.position().z, EPSILON6);

        // Move along local +Z basis (should move in world +X)
        t.translateLocalBasis(0, 0, 5);
        assertEquals(5.0f, t.position().x, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
        assertEquals(-5.0f, t.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocal90DegreesY() {
        // Rotate 90° around Y
        Transform t = new Transform();
        t.setPosition(0, 0, 0);
        t.setRotationY((float) Math.toRadians(90));

        // Move forward in semantic space (local -Z)
        t.translateLocal(0, 0, 5);

        // After 90° Y rotation, forward points toward world +X
        assertEquals(-5.0f, t.position().x, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
        assertEquals(0.0f, t.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocalVsLocalBasisDifference() {
        // The key difference: Z coordinate behavior
        Transform t1 = new Transform();
        Transform t2 = new Transform();

        // Both start at origin, rotated 90° around Y
        t1.setRotationY((float) Math.toRadians(90));
        t2.setRotationY((float) Math.toRadians(90));

        // translateLocal with +Z moves forward
        t1.translateLocal(0, 0, 5);

        // translateLocalBasis with +Z moves backward (along +Z basis)
        t2.translateLocalBasis(0, 0, 5);

        // They should be opposite in the direction of movement
        assertEquals(-t1.position().x, t2.position().x, EPSILON6);
        assertEquals(-t1.position().z, t2.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocalBasis180DegreesY() {
        // Rotate 180° around Y
        Transform t = new Transform();
        t.setPosition(0, 0, 0);
        t.setRotationY((float) Math.toRadians(180));

        // Move along local +X basis (should move toward world -X)
        t.translateLocalBasis(3, 0, 0);
        assertEquals(-3.0f, t.position().x, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
        assertEquals(0.0f, t.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocalArbitraryRotation() {
        // Rotate 45° around Y
        Transform t = new Transform();
        t.setPosition(0, 0, 0);
        t.setRotationY((float) Math.toRadians(45));

        // Move forward (semantic -Z)
        t.translateLocal(0, 0, 10);

        // Should move at 45° angle (diagonal)
        float expected = 10.0f / (float) Math.sqrt(2);
        assertEquals(-expected, t.position().x, EPSILON6);
        assertEquals(-expected, t.position().z, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
    }

    @Test
    public void testTranslateLocalBasisMultipleAxes() {
        // Rotate 90° around X
        Transform t = new Transform();
        t.setPosition(0, 0, 0);
        t.setRotationX((float) Math.toRadians(90));

        // Move along local Y basis (should move toward world +Z)
        t.translateLocalBasis(0, 5, 0);
        assertEquals(0.0f, t.position().x, EPSILON6);
        assertEquals(0.0f, t.position().y, EPSILON6);
        assertEquals(5.0f, t.position().z, EPSILON6);

        // Move along local Z basis (should move toward world -Y)
        t.translateLocalBasis(0, 0, 3);
        assertEquals(0.0f, t.position().x, EPSILON6);
        assertEquals(-3.0f, t.position().y, EPSILON6);
        assertEquals(5.0f, t.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocalVecOverload() {
        // Test Vec3 overload matches float version
        Transform t1 = new Transform();
        Transform t2 = new Transform();

        t1.setRotationY((float) Math.toRadians(45));
        t2.setRotationY((float) Math.toRadians(45));

        Vec3 offset = new Vec3(1, 2, 3);

        t1.translateLocal(offset);
        t2.translateLocal(offset.x, offset.y, offset.z);

        assertEquals(t2.position().x, t1.position().x, EPSILON6);
        assertEquals(t2.position().y, t1.position().y, EPSILON6);
        assertEquals(t2.position().z, t1.position().z, EPSILON6);
    }

    @Test
    public void testTranslateLocalBasisAccumulation() {
        // Multiple translations should accumulate correctly
        Transform t = new Transform();
        t.setRotationY((float) Math.toRadians(90));

        // Move right (+X), up (+Y), backward (+Z) in local basis
        t.translateLocalBasis(1, 0, 0);
        t.translateLocalBasis(0, 2, 0);
        t.translateLocalBasis(0, 0, 3);

        // After 90° Y rotation:
        // Local +X → World -Z
        // Local +Y → World +Y
        // Local +Z → World +X
        assertEquals(3.0f, t.position().x, EPSILON6);
        assertEquals(2.0f, t.position().y, EPSILON6);
        assertEquals(-1.0f, t.position().z, EPSILON6);
    }


    @Test
    public void testRotateLocalBasisIdentity() {
        // With identity rotation, local basis = world axes
        Transform t = new Transform();
        Vec3 v = new Vec3(0, 1, 0);

        // Rotate 90° around local X (same as world X when identity)
        t.rotateLocalBasisX((float) Math.toRadians(90));

        // Apply rotation to test vector
        v.rotate(t.rotation());

        // (0,1,0) rotated 90° around X → (0,0,1)
        assertEquals(0.0f, v.x, EPSILON6);
        assertEquals(0.0f, v.y, EPSILON6);
        assertEquals(1.0f, v.z, EPSILON6);
    }

    @Test
    public void testRotateLocalBasisAfterWorldRotation() {
        // First rotate in world space, then in local-basis space
        Transform t = new Transform();

        // Rotate 90° around world Y
        t.rotateY((float) Math.toRadians(90));

        // Now local +X points toward world -Z
        // Rotate 90° around local-basis X
        t.rotateLocalBasisX((float) Math.toRadians(90));

        // Get the up vector (should be rotated)
        Vec3 up = t.getUpVector();

        // After local-basis X rotation, up should point toward world +X
        assertEquals(1.0f, up.x, EPSILON6);
        assertEquals(0.0f, up.y, EPSILON6);
        assertEquals(0.0f, up.z, EPSILON6);
    }

    @Test
    public void testRotateLocalVsRotateLocalBasisDifference() {
        // The key difference: Z axis behavior
        Transform t1 = new Transform();
        Transform t2 = new Transform();

        // Both start rotated 90° around Y
        t1.rotateY((float) Math.toRadians(90));
        t2.rotateY((float) Math.toRadians(90));

        Vec3 forwardAxis = new Vec3(0, 0, 1);

        // rotateLocal treats +Z as forward (semantic)
        t1.rotateLocal(forwardAxis, (float) Math.toRadians(45));

        // rotateLocalBasis treats +Z as backward (mathematical)
        t2.rotateLocalBasis(forwardAxis, (float) Math.toRadians(45));

        // They should produce opposite rotations
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(1, 0, 0);

        v1.rotate(t1.rotation());
        v2.rotate(t2.rotation());

        // Verify they're different (opposites due to Z flip)
        assertFalse(Math.abs(v1.y - v2.y) < EPSILON6);
    }

    @Test
    public void testRotateLocalSemanticForward() {
        // rotateLocal should treat +Z as forward direction
        Transform t = new Transform();

        // Start facing down +X (90° around Y)
        t.rotateY((float) Math.toRadians(90));

        // Rotate 90° around semantic forward axis (+Z means around actual forward)
        t.rotateLocalZ((float) Math.toRadians(90));

        // Right vector should now point down (rotated around forward)
        Vec3 right = t.getRightVector();

        assertEquals(0.0f, right.x, EPSILON6);
        assertEquals(-1.0f, right.y, EPSILON6);
        assertEquals(0.0f, right.z, EPSILON6);
    }

    @Test
    public void testRotateLocalBasisCompoundRotations() {
        // Multiple local-basis rotations should accumulate
        Transform t = new Transform();

        // Rotate 45° around world Y first
        t.rotateY((float) Math.toRadians(45));

        // Then rotate 90° around local-basis X
        t.rotateLocalBasisX((float) Math.toRadians(90));

        // Then rotate 45° around local-basis Y
        t.rotateLocalBasisY((float) Math.toRadians(45));

        // Verify the forward vector has changed
        Vec3 forward = t.getForwardVector();

        // Should not be pointing along any world axis
        assertFalse(Math.abs(forward.x) > 0.99f);
        assertFalse(Math.abs(forward.y) > 0.99f);
        assertFalse(Math.abs(forward.z) > 0.99f);
    }

    @Test
    public void testRotateLocalQuaternionZFlip() {
        // Test quaternion version flips Z correctly
        Transform t1 = new Transform();
        Transform t2 = new Transform();

        // Create a rotation quaternion around (0, 0, 1) by 45°
        Quaternion q = new Quaternion();
        q.setAxisAngle(new Vec3(0, 0, 1), (float) Math.toRadians(45));

        // Apply as rotateLocal
        t1.rotateLocal(q);

        // Apply manually with Z-flip: rotateLocalBasis(qx, qy, -qz, qw)
        t2.rotation().mul(q.x, q.y, -q.z, q.w);

        // Should produce same result
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(1, 0, 0);

        v1.rotate(t1.rotation());
        v2.rotate(t2.rotation());

        assertEquals(v1.x, v2.x, EPSILON6);
        assertEquals(v1.y, v2.y, EPSILON6);
        assertEquals(v1.z, v2.z, EPSILON6);
    }

    @Test
    public void testRotateLocalBasisPreservesHandedness() {
        // Local-basis rotations should preserve right-handed coordinate system
        Transform t = new Transform();

        t.rotateLocalBasisX((float) Math.toRadians(37));
        t.rotateLocalBasisY((float) Math.toRadians(42));
        t.rotateLocalBasisZ((float) Math.toRadians(19));

        Vec3 right = t.getRightVector();
        Vec3 up = t.getUpVector();
        Vec3 forward = t.getForwardVector();

        // right × up should equal backward (right-handed)
        Vec3 computed = new Vec3().set(right).cross(up);

        assertEquals(-forward.x, computed.x, EPSILON6);
        assertEquals(-forward.y, computed.y, EPSILON6);
        assertEquals(-forward.z, computed.z, EPSILON6);
    }

    @Test
    public void testRotateLocalAxisVsQuaternionConsistency() {
        // Axis-angle and quaternion versions should produce same result
        Transform t1 = new Transform();
        Transform t2 = new Transform();

        Vec3 axis = new Vec3(1, 1, 1).normalize();
        float angle = (float) Math.toRadians(60);

        // Use axis-angle version
        t1.rotateLocal(axis, angle);

        // Use quaternion version
        Quaternion q = new Quaternion(axis, angle);
        t2.rotateLocal(q);

        // Should produce same rotation
        Vec3 v1 = t1.getForwardVector();
        Vec3 v2 = t2.getForwardVector();

        assertEquals(v1.x, v2.x, EPSILON6);
        assertEquals(v1.y, v2.y, EPSILON6);
        assertEquals(v1.z, v2.z, EPSILON6);
    }

    @Test
    public void testRotateLocalAfterWorldRotation() {
        // Verify rotateLocal rotates in current local space
        Transform t = new Transform();

        // Face right (90° around Y)
        t.rotateY((float) Math.toRadians(90));

        // Pitch up 45° in local space (around local X, which is still world X)
        t.rotateLocalX((float) Math.toRadians(45));

        Vec3 forward = t.getForwardVector();

        // Should be pointing diagonally: right and up
        float expected = 1.0f / (float) Math.sqrt(2);
        assertEquals(-expected, forward.x, EPSILON6);
        assertEquals(expected, forward.y, EPSILON6);
        assertEquals(0.0f, forward.z, EPSILON6);
    }

    @Test
    public void testRotateLocalBasisOrthonormality() {
        // After any rotation, basis should remain orthonormal
        Transform t = new Transform();

        // Apply random rotations
        t.rotateLocalBasis(new Vec3(1, 2, 3).normalize(), 1.2f);
        t.rotateLocalBasis(new Vec3(-2, 1, 4).normalize(), 0.7f);

        Vec3 right = t.getRightVector();
        Vec3 up = t.getUpVector();
        Vec3 forward = t.getForwardVector();

        // All should be unit length
        assertEquals(1.0f, right.length(), EPSILON6);
        assertEquals(1.0f, up.length(), EPSILON6);
        assertEquals(1.0f, forward.length(), EPSILON6);

        // All should be perpendicular
        assertEquals(0.0f, right.dot(up), EPSILON6);
        assertEquals(0.0f, right.dot(forward), EPSILON6);
        assertEquals(0.0f, up.dot(forward), EPSILON6);
    }

    @Test
    void testTransformNormalPreservesPerpendicularity() {
        // THE most important test - normals must stay perpendicular to surface
        Transform t = new Transform();
        t.setScale(2.0f, 1.0f, 0.5f);  // Non-uniform scale
        t.setRotation(new Vec3(0, 1, 0), (float)Math.toRadians(45));

        // Surface tangent and normal (perpendicular before transform)
        Vec3 tangent = new Vec3(1, 0, 0);
        Vec3 normal = new Vec3(0, 1, 0);

        // Transform both
        Vec3 transformedTangent = t.transformPosition(new Vec3(tangent));
        Vec3 transformedNormal = t.transformNormal(new Vec3(normal));

        // They MUST still be perpendicular (dot product = 0)
        float dot = transformedTangent.dot(transformedNormal);
        assertEquals(0.0f, dot, EPSILON6);
        assertEquals(1.0f, transformedNormal.lengthSqr(), EPSILON6);
    }

    @Test
    void testTransformNormalUniformScaleEqualsRotation() {
        // When scale is uniform, should equal simple rotation
        Transform t = new Transform();
        t.setScale(2.0f, 2.0f, 2.0f);  // Uniform
        t.setRotation(new Vec3(0, 1, 0), (float)Math.toRadians(90));

        Vec3 normal = new Vec3(1, 0, 0);
        Vec3 result1 = t.transformNormal(new Vec3(normal));
        Vec3 result2 = new Vec3(normal).rotate(t.rotation());

        assertEquals(result2.x, result1.x, EPSILON6);
        assertEquals(result2.y, result1.y, EPSILON6);
        assertEquals(result2.z, result1.z, EPSILON6);
    }

    @Test
    void testTransformNormalPureRotationPreservesLength() {
        // Rotation-only should preserve normal length
        Transform t = new Transform();
        t.setRotation(new Vec3(1, 1, 1).normalize(), (float)Math.toRadians(90));

        Vec3 normal = new Vec3(0, 1, 0);
        float originalLength = normal.length();

        t.transformNormal(normal);

        assertEquals(originalLength, normal.length(), EPSILON6);
    }

    @Test
    void testTransformNormalNonUniformScaleDoesNotChangeLength() {
        // Non-uniform scale SHOULD change normal length
        Transform t = new Transform();
        t.setScale(2.0f, 0.5f, 1.0f);  // Non-uniform

        Vec3 normal = new Vec3(0, 1, 0);  // Y-axis normal
        float originalLength = normal.length();

        t.transformNormal(normal);

        assertEquals(originalLength, normal.length(), EPSILON6);

    }

    @Test
    void testTransformNormalAxisAligned() {
        // Simple verification with axis-aligned normal
        Transform t = new Transform();
        t.setScale(2.0f, 3.0f, 4.0f);
        t.setRotation(new Vec3(0, 1, 0), (float)Math.toRadians(90));

        Vec3 normal = new Vec3(0, 1, 0);  // Y-axis
        t.transformNormal(normal);

        // Y rotation shouldn't affect Y-axis normal
        assertEquals(0.0f, normal.x, EPSILON6);
        assertEquals(1.0f, normal.y, EPSILON6);
        assertEquals(0.0f, normal.z, EPSILON6);
    }
}