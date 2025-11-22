package dev.stjepano.math;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Vec3Test {

    private static final float EPSILON = 0.0001f;

    @Test
    public void add() {
        Vec3 a = new Vec3(1, 2, 3);
        Vec3 b = new Vec3(2, 1, 1);

        a.add(b);
        assertEquals(3.0f, a.x);
        assertEquals(3.0f, a.y);
        assertEquals(4.0f, a.z);
    }

    @Test
    public void sub() {
        Vec3 a = new Vec3(1, 2, 3);
        Vec3 b = new Vec3(2, 1, 1);

        a.sub(b);
        assertEquals(-1.0f, a.x);
        assertEquals(1.0f, a.y);
        assertEquals(2.0f, a.z);
    }

    @Test
    public void copy() {
        Vec3 a = new Vec3(1, 2, 3);
        Vec3 copy1 = a.copy();
        assertEquals(1.0f, copy1.x);
        assertEquals(2.0f, copy1.y);
        assertEquals(3.0f, copy1.z);

        Vec3 copy2 = new Vec3();
        a.copy(copy2);
        assertEquals(1.0f, copy2.x);
        assertEquals(2.0f, copy2.y);
        assertEquals(3.0f, copy2.z);
    }

    @Test
    public void scale() {
        Vec3 a = new Vec3(1.0f, 2.0f, 3.0f);
        a.scale(2.0f);
        assertEquals(2.0f, a.x);
        assertEquals(4.0f, a.y);
        assertEquals(6.0f, a.z);

        a.scale(0.5f, 2.0f, 0.5f);
        assertEquals(1.0f, a.x);
        assertEquals(8.0f, a.y);
        assertEquals(3.0f, a.z);
    }

    @Test
    public void negate() {
        Vec3 a = new Vec3();
        a.set(1, 2, 3).negate();
        assertEquals(-1.0f, a.x);
        assertEquals(-2.0f, a.y);
        assertEquals(-3.0f, a.z);
    }

    @Test
    public void dot() {
        Vec3 a = new Vec3(1.0f, 2.0f, 3.0f);
        Vec3 b = new Vec3(4.0f, 5.0f, 6.0f);
        float dot = a.dot(b);
        assertEquals(32.0f, dot);  // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
    }

    @Test
    public void cross() {
        // X × Y = Z
        Vec3 x = new Vec3(1.0f, 0.0f, 0.0f);
        Vec3 y = new Vec3(0.0f, 1.0f, 0.0f);

        Vec3 result = x.copy();
        result.cross(y);

        assertEquals(0.0f, result.x);
        assertEquals(0.0f, result.y);
        assertEquals(1.0f, result.z);

        // Static version
        Vec3 dest = new Vec3();
        Vec3.cross(x, y, dest);
        assertEquals(0.0f, dest.x);
        assertEquals(0.0f, dest.y);
        assertEquals(1.0f, dest.z);
    }

    @Test
    public void lengthSqr() {
        Vec3 a = new Vec3(2.0f, 3.0f, 6.0f);
        float l = a.lengthSqr();
        assertEquals(49.0f, l);  // 4 + 9 + 36 = 49
    }

    @Test
    public void length() {
        Vec3 a = new Vec3(2.0f, 3.0f, 6.0f);
        float l = a.length();
        assertEquals(7.0f, l);
    }

    @Test
    public void normalize() {
        Vec3 a = new Vec3(2.0f, 3.0f, 6.0f);
        a.normalize();
        assertEquals(1.0f, a.lengthSqr(), 0.0001f);
    }

    @Test
    public void distanceSqr() {
        Vec3 a = new Vec3(1.0f, 0.0f, 0.0f);
        Vec3 b = new Vec3(3.0f, 0.0f, 0.0f);
        float squaredDistance = a.distanceSqr(b);
        assertEquals(4.0f, squaredDistance);
    }

    @Test
    public void distance() {
        Vec3 a = new Vec3(0.0f, 1.0f, 0.0f);
        Vec3 b = new Vec3(0.0f, 3.0f, 0.0f);
        float dist = a.distance(b);
        assertEquals(2.0f, dist);
    }

    @Test
    public void lerp() {
        Vec3 start = new Vec3(0, 0, 0);
        Vec3 target = new Vec3(3, 3, 3);

        Vec3 dest = new Vec3();
        Vec3.lerp(start, target, 0.0f, dest);
        assertEquals(0.0f, dest.x);
        assertEquals(0.0f, dest.y);
        assertEquals(0.0f, dest.z);

        Vec3.lerp(start, target, 0.5f, dest);
        assertEquals(1.5f, dest.x);
        assertEquals(1.5f, dest.y);
        assertEquals(1.5f, dest.z);

        Vec3.lerp(start, target, 1.0f, dest);
        assertEquals(3.0f, dest.x);
        assertEquals(3.0f, dest.y);
        assertEquals(3.0f, dest.z);
    }

    @Test
    public void floatArray() {
        float[] dest = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
        Vec3 v = new Vec3(1.0f, 2.0f, 3.0f);
        v.toFloatArray(dest, 1);
        assertArrayEquals(new float[] { 0.0f, 1.0f, 2.0f, 3.0f, 0.0f }, dest);

        Vec3 destV = new Vec3();
        Vec3.fromFloatArray(dest, 1, destV);
        assertEquals(1.0f, destV.x);
        assertEquals(2.0f, destV.y);
        assertEquals(3.0f, destV.z);
    }

    @Test
    public void memorySegment() {
        float[] dest = new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        MemorySegment segment = MemorySegment.ofArray(dest);
        Vec3 v = new Vec3(1.0f, 2.0f, 3.0f);
        v.toMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize());
        assertArrayEquals(new float[] { 0.0f, 1.0f, 2.0f, 3.0f, 0.0f }, dest);

        Vec3 destV = new Vec3();
        Vec3.fromMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize(), destV);
        assertEquals(1.0f, destV.x);
        assertEquals(2.0f, destV.y);
        assertEquals(3.0f, destV.z);
    }

    @Test
    public void vec3Rotate() {
        // Test the Vec3.rotate method with quaternion
        Vec3 axis = new Vec3(0, 0, 1);
        Quaternion q = new Quaternion(axis, (float) Math.PI / 2);  // 90° around Z

        Vec3 v = new Vec3(1, 0, 0);
        v.rotate(q);

        assertEquals(0.0f, v.x, EPSILON);
        assertEquals(1.0f, v.y, EPSILON);
        assertEquals(0.0f, v.z, EPSILON);
    }
}
