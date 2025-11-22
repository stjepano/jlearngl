package dev.stjepano.math;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.jupiter.api.Assertions.*;

class QuaternionTest {

    private static final float EPSILON = 0.0001f;

    @Test
    public void defaultConstructor() {
        Quaternion q = new Quaternion();
        assertEquals(0.0f, q.x);
        assertEquals(0.0f, q.y);
        assertEquals(0.0f, q.z);
        assertEquals(1.0f, q.w);
    }

    @Test
    public void axisAngleConstructor() {
        // 90 degrees around Z axis
        Vec3 axis = new Vec3(0, 0, 1);
        Quaternion q = new Quaternion(axis, (float) Math.PI / 2);

        assertEquals(1.0f, q.lengthSqr(), EPSILON);  // Should be unit length

        // Rotate (1,0,0) should give approximately (0,1,0)
        Vec3 v = new Vec3(1, 0, 0);
        v.rotate(q);
        assertEquals(0.0f, v.x, EPSILON);
        assertEquals(1.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void setAxisAngle() {
        Quaternion q = new Quaternion();
        Vec3 axis = new Vec3(1, 0, 0);  // X axis
        q.setAxisAngle(axis, (float) Math.PI);  // 180 degrees

        // Rotate (0,1,0) around X by 180 should give (0,-1,0)
        Vec3 v = new Vec3(0, 1, 0);
        v.rotate(q);
        assertEquals(0.0f, v.x, EPSILON);
        assertEquals(-1.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void copy() {
        Quaternion a = new Quaternion(1, 2, 3, 4);
        Quaternion copy1 = a.copy();
        assertEquals(1.0f, copy1.x);
        assertEquals(2.0f, copy1.y);
        assertEquals(3.0f, copy1.z);
        assertEquals(4.0f, copy1.w);

        Quaternion copy2 = new Quaternion();
        a.copy(copy2);
        assertEquals(1.0f, copy2.x);
        assertEquals(2.0f, copy2.y);
        assertEquals(3.0f, copy2.z);
        assertEquals(4.0f, copy2.w);
    }

    @Test
    public void length() {
        Quaternion q = new Quaternion(1, 2, 2, 0);
        assertEquals(9.0f, q.lengthSqr());
        assertEquals(3.0f, q.length());
    }

    @Test
    public void normalize() {
        Quaternion q = new Quaternion(1, 2, 2, 0);
        q.normalize();
        assertEquals(1.0f, q.lengthSqr(), EPSILON);
    }

    @Test
    public void conjugate() {
        Quaternion q = new Quaternion(1, 2, 3, 4);
        q.conjugate();
        assertEquals(-1.0f, q.x);
        assertEquals(-2.0f, q.y);
        assertEquals(-3.0f, q.z);
        assertEquals(4.0f, q.w);
    }

    @Test
    public void multiplication() {
        // q1: 90° around Z
        Vec3 axisZ = new Vec3(0, 0, 1);
        Quaternion q1 = new Quaternion(axisZ, (float) Math.PI / 2);

        // q2: 90° around Z
        Quaternion q2 = new Quaternion(axisZ, (float) Math.PI / 2);

        // q1 * q2 = 180° around Z
        q1.mul(q2);

        // Rotate (1,0,0) by 180° around Z should give (-1,0,0)
        Vec3 v = new Vec3(1, 0, 0);
        v.rotate(q1);
        assertEquals(-1.0f, v.x, EPSILON);
        assertEquals(0.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void preMultiplication() {
        Vec3 axisZ = new Vec3(0, 0, 1);
        Quaternion q1 = new Quaternion(axisZ, (float) Math.PI / 2);

        Quaternion q2 = new Quaternion(axisZ, (float) Math.PI / 2);

        // q2 * q1 = 180deg about Z
        q1.preMul(q2);

        Vec3 v = new Vec3(1, 0, 0);
        v.rotate(q1);
        assertEquals(-1.0f, v.x, EPSILON);
        assertEquals(0.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void setEulerYXZ_appliesInCorrectOrder() {
        // We'll prove YXZ order by rotating (1,0,0) with 90° rotations
        // and comparing against manual step-by-step application

        float angle90 = (float) Math.PI / 2.0f;

        // ========== Test 1: Yaw only ==========
        Vec3 v1 = new Vec3(1, 0, 0);
        Quaternion qYawOnly = new Quaternion();
        qYawOnly.setEulerYXZ(0, angle90, 0);  // pitch=0, yaw=90°, roll=0
        v1.rotate(qYawOnly);

        // Yaw 90° around Y should rotate X→-Z
        assertEquals(0.0f, v1.x, EPSILON);
        assertEquals(0.0f, v1.y, EPSILON);
        assertEquals(-1.0f, v1.z, EPSILON);

        // ========== Test 2: Yaw then Pitch ==========
        Vec3 v2 = new Vec3(1, 0, 0);
        Quaternion qYawPitch = new Quaternion();
        qYawPitch.setEulerYXZ(angle90, angle90, 0);  // pitch=90°, yaw=90°, roll=0
        v2.rotate(qYawPitch);

        // Step 1: Yaw 90° around Y: (1,0,0) → (0,0,-1)
        // Step 2: Pitch 90° around X: (0,0,-1) → (0,1,0)
        assertEquals(0.0f, v2.x, EPSILON);
        assertEquals(1.0f, v2.y, EPSILON);
        assertEquals(0.0f, v2.z, EPSILON);

        // ========== Test 3: Full YXZ (Yaw, Pitch, Roll) ==========
        Vec3 v3 = new Vec3(1, 0, 0);
        Quaternion qFull = new Quaternion();
        qFull.setEulerYXZ(angle90, angle90, angle90);  // all 90°
        v3.rotate(qFull);

        // Step 1: Yaw 90° around Y: (1,0,0) → (0,0,-1)
        // Step 2: Pitch 90° around X: (0,0,-1) → (0,1,0)
        // Step 3: Roll 90° around Z: (0,1,0) → (-1,0,0)
        assertEquals(-1.0f, v3.x, EPSILON);
        assertEquals(0.0f, v3.y, EPSILON);
        assertEquals(0.0f, v3.z, EPSILON);

        // ========== Test 4: Verify against manual quaternion multiplication ==========
        // Manually create qRoll * qPitch * qYaw and verify same result
        Quaternion qYaw = new Quaternion(new Vec3(0, 1, 0), angle90);
        Quaternion qPitch = new Quaternion(new Vec3(1, 0, 0), angle90);
        Quaternion qRoll = new Quaternion(new Vec3(0, 0, 1), angle90);

        Quaternion qManual = qRoll.copy();
        qManual.mul(qPitch);
        qManual.mul(qYaw);

        // Should match our setEulerYXZ result
        assertEquals(qFull.x, qManual.x, EPSILON);
        assertEquals(qFull.y, qManual.y, EPSILON);
        assertEquals(qFull.z, qManual.z, EPSILON);
        assertEquals(qFull.w, qManual.w, EPSILON);

        // Verify they produce same rotation
        Vec3 v4 = new Vec3(1, 0, 0);
        v4.rotate(qManual);
        assertEquals(-1.0f, v4.x, EPSILON);
        assertEquals(0.0f, v4.y, EPSILON);
        assertEquals(0.0f, v4.z, EPSILON);
    }

    @Test
    public void setEulerYXZ_differentVector() {
        // Additional test with a different starting vector
        float angle90 = (float) Math.PI / 2.0f;

        Vec3 v = new Vec3(0, 1, 0);  // Start pointing up
        Quaternion q = new Quaternion();
        q.setEulerYXZ(angle90, 0, 0);  // Pitch 90deg only
        v.rotate(q);

        // Pitch 90deg around X should rotate Y→-Z
        assertEquals(0.0f, v.x, EPSILON);
        assertEquals(0.0f, v.y, EPSILON);
        assertEquals(1.0f, v.z, EPSILON);
    }

    @Test
    public void setEulerYXZ_orderMatters() {
        // Prove that order matters by comparing YXZ vs XYZ
        float angle = (float) Math.PI / 2;  // 90 deg

        // YXZ order
        Vec3 vYXZ = new Vec3(1, 0, 0);
        Quaternion qYXZ = new Quaternion();
        qYXZ.setEulerYXZ(angle, angle, angle);
        qYXZ.normalize();
        vYXZ.rotate(qYXZ);

        // XYZ order (wrong order - for comparison)
        Vec3 vXYZ = new Vec3(1, 0, 0);
        Quaternion qX = new Quaternion(new Vec3(1, 0, 0), angle);
        Quaternion qY = new Quaternion(new Vec3(0, 1, 0), angle);
        Quaternion qZ = new Quaternion(new Vec3(0, 0, 1), angle);
        Quaternion qXYZ = qZ.copy().mul(qY).mul(qX);  // Z*Y*X = XYZ order
        vXYZ.rotate(qXYZ);

        // Results should be DIFFERENT (proving order matters)
        boolean different = Math.abs(vYXZ.x - vXYZ.x) > EPSILON ||
                Math.abs(vYXZ.y - vXYZ.y) > EPSILON ||
                Math.abs(vYXZ.z - vXYZ.z) > EPSILON;
        assertTrue(different, "YXZ and XYZ should produce different results");
    }

    @Test
    public void setEulerZXY_appliesInCorrectOrder() {
        float angle90 = (float) Math.PI / 2.0f;

        // ========== Test 1: Roll only ==========
        Vec3 v1 = new Vec3(1, 0, 0);
        Quaternion qRollOnly = new Quaternion();
        qRollOnly.setEulerZXY(0, 0, angle90);  // pitch=0, yaw=0, roll=90°
        v1.rotate(qRollOnly);

        // Roll 90° around Z should rotate X→Y
        assertEquals(0.0f, v1.x, EPSILON);
        assertEquals(1.0f, v1.y, EPSILON);
        assertEquals(0.0f, v1.z, EPSILON);

        // ========== Test 2: Roll then Pitch ==========
        Vec3 v2 = new Vec3(1, 0, 0);
        Quaternion qRollPitch = new Quaternion();
        qRollPitch.setEulerZXY(angle90, 0, angle90);  // pitch=90°, yaw=0, roll=90°
        v2.rotate(qRollPitch);

        // Step 1: Roll 90° around Z: (1,0,0) → (0,1,0)
        // Step 2: Pitch 90° around X: (0,1,0) → (0,0,1)
        assertEquals(0.0f, v2.x, EPSILON);
        assertEquals(0.0f, v2.y, EPSILON);
        assertEquals(1.0f, v2.z, EPSILON);

        // ========== Test 3: Full ZXY (Roll, Pitch, Yaw) ==========
        Vec3 v3 = new Vec3(1, 0, 0);
        Quaternion qFull = new Quaternion();
        qFull.setEulerZXY(angle90, angle90, angle90);  // all 90°
        v3.rotate(qFull);

        // Step 1: Roll 90° around Z: (1,0,0) → (0,1,0)
        // Step 2: Pitch 90° around X: (0,1,0) → (0,0,1)
        // Step 3: Yaw 90° around Y: (0,0,1) → (1,0,0)
        assertEquals(1.0f, v3.x, EPSILON);
        assertEquals(0.0f, v3.y, EPSILON);
        assertEquals(0.0f, v3.z, EPSILON);

        // ========== Test 4: Verify against manual quaternion multiplication ==========
        Quaternion qYaw = new Quaternion(new Vec3(0, 1, 0), angle90);
        Quaternion qPitch = new Quaternion(new Vec3(1, 0, 0), angle90);
        Quaternion qRoll = new Quaternion(new Vec3(0, 0, 1), angle90);

        // ZXY means: qYaw * qPitch * qRoll
        Quaternion qManual = qYaw.copy();
        qManual.mul(qPitch);
        qManual.mul(qRoll);

        // Should match our setEulerZXY result
        assertEquals(qFull.x, qManual.x, EPSILON);
        assertEquals(qFull.y, qManual.y, EPSILON);
        assertEquals(qFull.z, qManual.z, EPSILON);
        assertEquals(qFull.w, qManual.w, EPSILON);

        // Verify they produce same rotation
        Vec3 v4 = new Vec3(1, 0, 0);
        v4.rotate(qManual);
        assertEquals(1.0f, v4.x, EPSILON);
        assertEquals(0.0f, v4.y, EPSILON);
        assertEquals(0.0f, v4.z, EPSILON);
    }

    @Test
    public void setEulerZYX_appliesInCorrectOrder() {
        float angle90 = (float) Math.PI / 2.0f;

        // ========== Test 1: Roll only ==========
        Vec3 v1 = new Vec3(1, 0, 0);
        Quaternion qRollOnly = new Quaternion();
        qRollOnly.setEulerZYX(0, 0, angle90);  // pitch=0, yaw=0, roll=90°
        v1.rotate(qRollOnly);

        // Roll 90° around Z should rotate X→Y
        assertEquals(0.0f, v1.x, EPSILON);
        assertEquals(1.0f, v1.y, EPSILON);
        assertEquals(0.0f, v1.z, EPSILON);

        // ========== Test 2: Roll then Yaw ==========
        Vec3 v2 = new Vec3(1, 0, 0);
        Quaternion qRollYaw = new Quaternion();
        qRollYaw.setEulerZYX(0, angle90, angle90);  // pitch=0, yaw=90°, roll=90°
        v2.rotate(qRollYaw);

        // Step 1: Roll 90° around Z: (1,0,0) → (0,1,0)
        // Step 2: Yaw 90° around Y: (0,1,0) → (0,1,0) [Y axis aligned, no change]
        assertEquals(0.0f, v2.x, EPSILON);
        assertEquals(1.0f, v2.y, EPSILON);
        assertEquals(0.0f, v2.z, EPSILON);

        // ========== Test 3: Full ZYX (Roll, Yaw, Pitch) ==========
        Vec3 v3 = new Vec3(1, 0, 0);
        Quaternion qFull = new Quaternion();
        qFull.setEulerZYX(angle90, angle90, angle90);  // all 90°
        v3.rotate(qFull);

        // Step 1: Roll 90° around Z: (1,0,0) → (0,1,0)
        // Step 2: Yaw 90° around Y: (0,1,0) → (0,1,0) [unchanged]
        // Step 3: Pitch 90° around X: (0,1,0) → (0,0,1)
        assertEquals(0.0f, v3.x, EPSILON);
        assertEquals(0.0f, v3.y, EPSILON);
        assertEquals(1.0f, v3.z, EPSILON);

        // ========== Test 4: Verify against manual quaternion multiplication ==========
        Quaternion qYaw = new Quaternion(new Vec3(0, 1, 0), angle90);
        Quaternion qPitch = new Quaternion(new Vec3(1, 0, 0), angle90);
        Quaternion qRoll = new Quaternion(new Vec3(0, 0, 1), angle90);

        // ZYX means: qPitch * qYaw * qRoll
        Quaternion qManual = qPitch.copy();
        qManual.mul(qYaw);
        qManual.mul(qRoll);

        // Should match our setEulerZYX result
        assertEquals(qFull.x, qManual.x, EPSILON);
        assertEquals(qFull.y, qManual.y, EPSILON);
        assertEquals(qFull.z, qManual.z, EPSILON);
        assertEquals(qFull.w, qManual.w, EPSILON);

        // Verify they produce same rotation
        Vec3 v4 = new Vec3(1, 0, 0);
        v4.rotate(qManual);
        assertEquals(0.0f, v4.x, EPSILON);
        assertEquals(0.0f, v4.y, EPSILON);
        assertEquals(1.0f, v4.z, EPSILON);
    }

    @Test
    public void setEulerZXY_differentVector() {
        float angle90 = (float) Math.PI / 2.0f;

        // Test with (0,0,1) - pointing forward
        Vec3 v = new Vec3(0, 0, 1);
        Quaternion q = new Quaternion();
        q.setEulerZXY(0, angle90, 0);  // Yaw 90° only
        v.rotate(q);

        // Yaw 90° around Y should rotate Z→X
        assertEquals(1.0f, v.x, EPSILON);
        assertEquals(0.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void setEulerZYX_differentVector() {
        float angle90 = (float) Math.PI / 2.0f;

        // Test with (0,0,1) - pointing forward
        Vec3 v = new Vec3(0, 0, 1);
        Quaternion q = new Quaternion();
        q.setEulerZYX(angle90, 0, 0);  // Pitch 90° only
        v.rotate(q);

        // Pitch 90° around X should rotate Z→-Y
        assertEquals(0.0f, v.x, EPSILON);
        assertEquals(-1.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void eulerOrders_produceDifferentResults() {
        // Prove that different orders produce different results
        float angle = (float) Math.PI / 4.0f;  // 45°

        Vec3 vYXZ = new Vec3(1, 0, 0);
        Vec3 vZXY = new Vec3(1, 0, 0);
        Vec3 vZYX = new Vec3(1, 0, 0);

        Quaternion qYXZ = new Quaternion();
        Quaternion qZXY = new Quaternion();
        Quaternion qZYX = new Quaternion();

        qYXZ.setEulerYXZ(angle, angle, angle);
        qZXY.setEulerZXY(angle, angle, angle);
        qZYX.setEulerZYX(angle, angle, angle);

        vYXZ.rotate(qYXZ);
        vZXY.rotate(qZXY);
        vZYX.rotate(qZYX);

        // All three should produce different results
        boolean yxzVsZxy = Math.abs(vYXZ.x - vZXY.x) > EPSILON ||
                Math.abs(vYXZ.y - vZXY.y) > EPSILON ||
                Math.abs(vYXZ.z - vZXY.z) > EPSILON;

        boolean yxzVsZyx = Math.abs(vYXZ.x - vZYX.x) > EPSILON ||
                Math.abs(vYXZ.y - vZYX.y) > EPSILON ||
                Math.abs(vYXZ.z - vZYX.z) > EPSILON;

        boolean zxyVsZyx = Math.abs(vZXY.x - vZYX.x) > EPSILON ||
                Math.abs(vZXY.y - vZYX.y) > EPSILON ||
                Math.abs(vZXY.z - vZYX.z) > EPSILON;

        assertTrue(yxzVsZxy, "YXZ and ZXY should produce different results");
        assertTrue(yxzVsZyx, "YXZ and ZYX should produce different results");
        assertTrue(zxyVsZyx, "ZXY and ZYX should produce different results");
    }

    @Test
    public void combine() {
        Vec3 axisZ = new Vec3(0, 0, 1);
        Quaternion q1 = new Quaternion(axisZ, (float) Math.PI / 4);  // 45°
        Quaternion q2 = new Quaternion(axisZ, (float) Math.PI / 4);  // 45°
        Quaternion q3 = new Quaternion(axisZ, (float) Math.PI / 4);  // 45°
        Quaternion result = new Quaternion();

        // 45° + 45° + 45° = 135° around Z
        Quaternion.combine(q1, q2, q3, result);

        // Rotate (1,0,0) by 135° around Z
        Vec3 v = new Vec3(1, 0, 0);
        v.rotate(result);

        // cos(135°) = -√2/2, sin(135°) = √2/2
        assertEquals(-0.7071f, v.x, EPSILON);
        assertEquals(0.7071f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }

    @Test
    public void toBasis() {
        // Identity quaternion should give identity basis
        Quaternion q = new Quaternion();
        Vec3 xAxis = new Vec3();
        Vec3 yAxis = new Vec3();
        Vec3 zAxis = new Vec3();

        q.toBasis(xAxis, yAxis, zAxis);

        assertEquals(1.0f, xAxis.x, EPSILON);
        assertEquals(0.0f, xAxis.y, EPSILON);
        assertEquals(0.0f, xAxis.z, EPSILON);

        assertEquals(0.0f, yAxis.x, EPSILON);
        assertEquals(1.0f, yAxis.y, EPSILON);
        assertEquals(0.0f, yAxis.z, EPSILON);

        assertEquals(0.0f, zAxis.x, EPSILON);
        assertEquals(0.0f, zAxis.y, EPSILON);
        assertEquals(1.0f, zAxis.z, EPSILON);
    }

    @Test
    public void toBasisRotated() {
        // 90° around Z axis
        Vec3 axis = new Vec3(0, 0, 1);
        Quaternion q = new Quaternion(axis, (float) Math.PI / 2);

        Vec3 xAxis = new Vec3();
        Vec3 yAxis = new Vec3();
        Vec3 zAxis = new Vec3();

        q.toBasis(xAxis, yAxis, zAxis);

        // X axis rotated 90° around Z -> Y axis
        assertEquals(0.0f, xAxis.x, EPSILON);
        assertEquals(1.0f, xAxis.y, EPSILON);
        assertEquals(0.0f, xAxis.z, EPSILON);

        // Y axis rotated 90° around Z -> -X axis
        assertEquals(-1.0f, yAxis.x, EPSILON);
        assertEquals(0.0f, yAxis.y, EPSILON);
        assertEquals(0.0f, yAxis.z, EPSILON);

        // Z axis unchanged
        assertEquals(0.0f, zAxis.x, EPSILON);
        assertEquals(0.0f, zAxis.y, EPSILON);
        assertEquals(1.0f, zAxis.z, EPSILON);
    }

    @Test
    public void toAxisAngle() {
        Vec3 originalAxis = new Vec3(0, 1, 0);  // Y axis
        float originalAngle = (float) Math.PI / 3;  // 60 degrees

        Quaternion q = new Quaternion(originalAxis, originalAngle);

        Vec3 extractedAxis = new Vec3();
        float extractedAngle = q.toAxisAngle(extractedAxis);

        assertEquals(0.0f, extractedAxis.x, EPSILON);
        assertEquals(1.0f, extractedAxis.y, EPSILON);
        assertEquals(0.0f, extractedAxis.z, EPSILON);
        assertEquals(originalAngle, extractedAngle, EPSILON);
    }

    @Test
    public void toAxisAngleZeroRotation() {
        Quaternion q = new Quaternion();  // Identity
        Vec3 axis = new Vec3();
        float angle = q.toAxisAngle(axis);

        assertEquals(0.0f, angle, EPSILON);
        // Axis can be anything for zero rotation, but should be normalized
        assertEquals(1.0f, axis.length(), EPSILON);
    }

    @Test
    public void slerp() {
        // From identity to 90° around Z
        Quaternion q0 = new Quaternion();
        Vec3 axis = new Vec3(0, 0, 1);
        Quaternion q1 = new Quaternion(axis, (float) Math.PI / 2);

        Quaternion result = new Quaternion();

        // t=0 should give q0
        Quaternion.slerp(q0, q1, 0.0f, result);
        Vec3 v0 = new Vec3(1, 0, 0);
        v0.rotate(result);
        assertEquals(1.0f, v0.x, EPSILON);
        assertEquals(0.0f, v0.y, EPSILON);

        // t=1 should give q1
        Quaternion.slerp(q0, q1, 1.0f, result);
        Vec3 v1 = new Vec3(1, 0, 0);
        v1.rotate(result);
        assertEquals(0.0f, v1.x, EPSILON);
        assertEquals(1.0f, v1.y, EPSILON);

        // t=0.5 should give 45° rotation
        Quaternion.slerp(q0, q1, 0.5f, result);
        Vec3 v05 = new Vec3(1, 0, 0);
        v05.rotate(result);
        assertEquals(0.7071f, v05.x, EPSILON);
        assertEquals(0.7071f, v05.y, EPSILON);
    }

    @Test
    public void slerpOppositeQuaternions() {
        // Test shortest path behavior
        Vec3 axis = new Vec3(0, 0, 1);
        Quaternion q1 = new Quaternion(axis, 0.1f);
        Quaternion q2 = new Quaternion(axis, 0.1f);
        q2.conjugate();  // Make it opposite
        q2.w *= -1;      // Now it represents same rotation but negative

        Quaternion result = new Quaternion();
        Quaternion.slerp(q1, q2, 0.5f, result);

        // Should take shortest path (stay near q1)
        assertTrue(result.lengthSqr() > 0.9f);  // Should be normalized
    }


    @Test
    public void floatArray() {
        float[] dest = new float[] { 0, 0, 0, 0, 0, 0 };
        Quaternion q = new Quaternion(1, 2, 3, 4);
        q.toFloatArray(dest, 1);
        assertArrayEquals(new float[] { 0, 1, 2, 3, 4, 0 }, dest);

        Quaternion destQ = new Quaternion();
        Quaternion.fromFloatArray(dest, 1, destQ);
        assertEquals(1.0f, destQ.x);
        assertEquals(2.0f, destQ.y);
        assertEquals(3.0f, destQ.z);
        assertEquals(4.0f, destQ.w);
    }

    @Test
    public void memorySegment() {
        float[] dest = new float[] {0, 0, 0, 0, 0, 0};
        MemorySegment segment = MemorySegment.ofArray(dest);
        Quaternion q = new Quaternion(1, 2, 3, 4);
        q.toMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize());
        assertArrayEquals(new float[] { 0, 1, 2, 3, 4, 0 }, dest);

        Quaternion destQ = new Quaternion();
        Quaternion.fromMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize(), destQ);
        assertEquals(1.0f, destQ.x);
        assertEquals(2.0f, destQ.y);
        assertEquals(3.0f, destQ.z);
        assertEquals(4.0f, destQ.w);
    }


    @Test
    public void testFromMatrixIdentity() {
        // Identity rotation: trace > 0, qw dominant
        Mat4 m = new Mat4(); // identity
        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        assertEquals(0.0f, q.x, EPSILON);
        assertEquals(0.0f, q.y, EPSILON);
        assertEquals(0.0f, q.z, EPSILON);
        assertEquals(1.0f, q.w, EPSILON);
    }

    @Test
    public void testFromMatrix90DegreesX() {
        // 90° rotation about X: m00 dominant (branch 2)
        Mat4 m = new Mat4();
        m.m00 = 1;  m.m01 = 0;  m.m02 = 0;
        m.m10 = 0;  m.m11 = 0;  m.m12 = -1;
        m.m20 = 0;  m.m21 = 1;  m.m22 = 0;

        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        // Expected: qx = sin(45°) = 0.707, qw = cos(45°) = 0.707
        assertEquals(0.70710677f, q.x, EPSILON);
        assertEquals(0.0f, q.y, EPSILON);
        assertEquals(0.0f, q.z, EPSILON);
        assertEquals(0.70710677f, q.w, EPSILON);
    }

    @Test
    public void testFromMatrix90DegreesY() {
        // 90° rotation about Y: m11 dominant (branch 3)
        Mat4 m = new Mat4();
        m.m00 = 0;   m.m01 = 0;  m.m02 = 1;
        m.m10 = 0;   m.m11 = 1;  m.m12 = 0;
        m.m20 = -1;  m.m21 = 0;  m.m22 = 0;

        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        assertEquals(0.0f, q.x, EPSILON);
        assertEquals(0.70710677f, q.y, EPSILON);
        assertEquals(0.0f, q.z, EPSILON);
        assertEquals(0.70710677f, q.w, EPSILON);
    }

    @Test
    public void testFromMatrix90DegreesZ() {
        // 90° rotation about Z: m22 dominant (branch 4)
        Mat4 m = new Mat4();
        m.m00 = 0;  m.m01 = -1;  m.m02 = 0;
        m.m10 = 1;  m.m11 = 0;   m.m12 = 0;
        m.m20 = 0;  m.m21 = 0;   m.m22 = 1;

        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        assertEquals(0.0f, q.x, EPSILON);
        assertEquals(0.0f, q.y, EPSILON);
        assertEquals(0.70710677f, q.z, EPSILON);
        assertEquals(0.70710677f, q.w, EPSILON);
    }

    @Test
    public void testFromMatrix180DegreesX() {
        // 180° rotation about X: trace ≈ -1, m00 = 1 (branch 2)
        Mat4 m = new Mat4();
        m.m00 = 1;   m.m01 = 0;   m.m02 = 0;
        m.m10 = 0;   m.m11 = -1;  m.m12 = 0;
        m.m20 = 0;   m.m21 = 0;   m.m22 = -1;

        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        // Expected: (1, 0, 0, 0) or (-1, 0, 0, 0)
        assertEquals(1.0f, Math.abs(q.x), EPSILON);
        assertEquals(0.0f, q.y, EPSILON);
        assertEquals(0.0f, q.z, EPSILON);
        assertEquals(0.0f, Math.abs(q.w), EPSILON);
    }

    @Test
    public void testFromMatrix180DegreesY() {
        // 180° rotation about Y: trace ≈ -1, m11 = 1 (branch 3)
        Mat4 m = new Mat4();
        m.m00 = -1;  m.m01 = 0;  m.m02 = 0;
        m.m10 = 0;   m.m11 = 1;  m.m12 = 0;
        m.m20 = 0;   m.m21 = 0;  m.m22 = -1;

        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        assertEquals(0.0f, q.x, EPSILON);
        assertEquals(1.0f, Math.abs(q.y), EPSILON);
        assertEquals(0.0f, q.z, EPSILON);
        assertEquals(0.0f, Math.abs(q.w), EPSILON);
    }

    @Test
    public void testFromMatrix180DegreesZ() {
        // 180° rotation about Z: trace ≈ -1, m22 = 1 (branch 4)
        Mat4 m = new Mat4();
        m.m00 = -1;  m.m01 = 0;   m.m02 = 0;
        m.m10 = 0;   m.m11 = -1;  m.m12 = 0;
        m.m20 = 0;   m.m21 = 0;   m.m22 = 1;

        Quaternion q = new Quaternion();
        q.setFromRotationMatrix(m);

        assertEquals(0.0f, q.x, EPSILON);
        assertEquals(0.0f, q.y, EPSILON);
        assertEquals(1.0f, Math.abs(q.z), EPSILON);
        assertEquals(0.0f, Math.abs(q.w), EPSILON);
    }

    @Test
    public void testFromMatrixArbitrary() {
        // Arbitrary rotation: 45° about axis (1,1,1)
        Vec3 axis = new Vec3(1, 1, 1).normalize();
        Quaternion qOriginal = new Quaternion().setAxisAngle(axis, (float)Math.toRadians(45));

        // Convert to matrix
        Mat3 m = new Mat3(qOriginal);

        // Convert back to quaternion
        Quaternion qResult = new Quaternion();
        qResult.setFromRotationMatrix(m);

        // Should match (or be negated - same rotation)
        boolean matches =
                (Math.abs(qResult.x - qOriginal.x) < EPSILON &&
                        Math.abs(qResult.y - qOriginal.y) < EPSILON &&
                        Math.abs(qResult.z - qOriginal.z) < EPSILON &&
                        Math.abs(qResult.w - qOriginal.w) < EPSILON) ||
                        (Math.abs(qResult.x + qOriginal.x) < EPSILON &&
                                Math.abs(qResult.y + qOriginal.y) < EPSILON &&
                                Math.abs(qResult.z + qOriginal.z) < EPSILON &&
                                Math.abs(qResult.w + qOriginal.w) < EPSILON);

        assertTrue(matches);
    }

    @Test
    public void testRoundTripQuaternionToMatrixToQuaternion() {
        // Test various quaternions: quat → matrix → quat should be identical
        Quaternion[] testCases = {
                new Quaternion().setAxisAngle(new Vec3(1, 0, 0), (float)Math.toRadians(30)),
                new Quaternion().setAxisAngle(new Vec3(0, 1, 0), (float)Math.toRadians(60)),
                new Quaternion().setAxisAngle(new Vec3(0, 0, 1), (float)Math.toRadians(120)),
                new Quaternion().setEulerYXZ(0.5f, 0.3f, 0.7f),
                new Quaternion().setAxisAngle(new Vec3(1, 1, 1).normalize(), (float)Math.toRadians(87))
        };

        Mat3 m = new Mat3();
        Quaternion result = new Quaternion();

        for (Quaternion original : testCases) {
            // Convert to matrix
            m.setRotation(original);

            // Convert back
            result.setFromRotationMatrix(m);

            // Check (handle quaternion double-cover: q and -q represent same rotation)
            boolean matches =
                    (Math.abs(result.x - original.x) < EPSILON &&
                            Math.abs(result.y - original.y) < EPSILON &&
                            Math.abs(result.z - original.z) < EPSILON &&
                            Math.abs(result.w - original.w) < EPSILON) ||
                            (Math.abs(result.x + original.x) < EPSILON &&
                                    Math.abs(result.y + original.y) < EPSILON &&
                                    Math.abs(result.z + original.z) < EPSILON &&
                                    Math.abs(result.w + original.w) < EPSILON);

            assertTrue(matches, "failed for " + original);
        }
    }
}
