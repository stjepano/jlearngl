package dev.stjepano.math;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.jupiter.api.Assertions.*;

class Vec2Test {

    @Test
    public void add() {
        Vec2 a = new Vec2(1, 2);
        Vec2 b = new Vec2(2, 1);

        a.add(b);
        assertEquals(3.0f, a.x);
        assertEquals(3.0f, a.y);
    }

    @Test
    public void sub() {
        Vec2 a = new Vec2(1, 2);
        Vec2 b = new Vec2(2, 1);

        a.sub(b);
        assertEquals(-1.0f, a.x);
        assertEquals(1.0f, a.y);
    }

    @Test
    public void copy() {
        Vec2 a = new Vec2(1, 2);
        Vec2 copy1 = a.copy();
        assertEquals(1.0f, copy1.x);
        assertEquals(2.0f, copy1.y);

        Vec2 copy2 = new Vec2();
        a.copy(copy2);
        assertEquals(1.0f, copy2.x);
        assertEquals(2.0f, copy2.y);
    }

    @Test
    public void scale() {
        Vec2 a = new Vec2(1.0f, 2.0f);
        a.scale(3.0f);
        assertEquals(3.0f, a.x);
        assertEquals(6.0f, a.y);

        a.scale(0.5f, 2.0f);
        assertEquals(1.5f, a.x);
        assertEquals(12.0f, a.y);
    }

    @Test
    public void negate() {
        Vec2 a = new Vec2();
        a.set(1, 2).negate();
        assertEquals(-1.0f, a.x);
        assertEquals(-2.0f, a.y);
    }

    @Test
    public void dot() {
        Vec2 a = new Vec2(1.0f, 2.0f);
        Vec2 b = new Vec2(3.0f, 4.0f);
        float dot = a.dot(b);
        assertEquals(11.0f, dot);
    }

    @Test
    public void lengthSqr() {
        Vec2 a = new Vec2(3.0f, 4.0f);
        float l = a.lengthSqr();
        assertEquals(25.0f, l);
    }

    @Test
    public void length() {
        Vec2 a = new Vec2(3.0f, 4.0f);
        float l = a.length();
        assertEquals(5.0f, l);
    }

    @Test
    public void normalize() {
        Vec2 a = new Vec2(3.0f, 4.0f);
        a.normalize();
        assertEquals(1.0f, a.lengthSqr());
    }

    @Test
    public void cross2D() {
        Vec2 x = new Vec2(1.0f, 0.0f);
        Vec2 y = new Vec2(0.0f, 1.0f);
        assertTrue(x.cross2D(y) > 0.0f);
        assertTrue(y.cross2D(x) < 0.0f);
    }

    @Test
    public void distanceSqr() {
        Vec2 a = new Vec2(1.0f, 0.0f);
        Vec2 b = new Vec2(3.0f, 0.0f);
        float squaredDistance = a.distanceSqr(b);
        assertEquals(4.0f, squaredDistance);
    }

    @Test
    public void distance() {
        Vec2 a = new Vec2(0.0f, 1.0f);
        Vec2 b = new Vec2(0.0f, 3.0f);
        float dist = a.distance(b);
        assertEquals(2.0f, dist);
    }

    @Test
    public void lerp() {
        Vec2 start = new Vec2(0, 0);
        Vec2 target = new Vec2(2, 2);
        Vec2 dest = new Vec2();

        Vec2.lerp(start, target, 0.0f, dest);
        assertEquals(0.0f, dest.x);
        assertEquals(0.0f, dest.y);

        Vec2.lerp(start, target, 0.5f, dest);
        assertEquals(1.0f, dest.x);
        assertEquals(1.0f, dest.y);

        Vec2.lerp(start, target, 1.0f, dest);
        assertEquals(2.0f, dest.x);
        assertEquals(2.0f, dest.y);
    }

    @Test
    public void floatArray() {
        float[] dest = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
        Vec2 v = new Vec2(1.0f, 2.0f);
        v.toFloatArray(dest, 1);
        assertArrayEquals(new float[] { 0.0f, 1.0f, 2.0f, 0.0f }, dest);

        Vec2 destV = new Vec2();
        Vec2.fromFloatArray(dest, 1, destV);
        assertEquals(1.0f, destV.x);
        assertEquals(2.0f, destV.y);
    }

    @Test
    public void memorySegment() {
        float[] dest = new float[] {0.0f, 0.0f, 0.0f, 0.0f};
        MemorySegment segment = MemorySegment.ofArray(dest);
        Vec2 v = new Vec2(1.0f, 2.0f);
        v.toMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize());
        assertArrayEquals(new float[] { 0.0f, 1.0f, 2.0f, 0.0f }, dest);

        Vec2 destV = new Vec2();
        Vec2.fromMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize(), destV);
        assertEquals(1.0f, destV.x);
        assertEquals(2.0f, destV.y);
    }
}