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
}
