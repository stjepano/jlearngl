package dev.stjepano.math;

import org.junit.jupiter.api.Test;

  import java.lang.foreign.MemorySegment;
  import java.lang.foreign.ValueLayout;

  import static org.junit.jupiter.api.Assertions.*;

  class Vec4Test {

      @Test
      public void add() {
          Vec4 a = new Vec4(1, 2, 3, 4);
          Vec4 b = new Vec4(2, 1, 1, 1);

          a.add(b);
          assertEquals(3.0f, a.x);
          assertEquals(3.0f, a.y);
          assertEquals(4.0f, a.z);
          assertEquals(5.0f, a.w);
      }

      @Test
      public void sub() {
          Vec4 a = new Vec4(1, 2, 3, 4);
          Vec4 b = new Vec4(2, 1, 1, 1);

          a.sub(b);
          assertEquals(-1.0f, a.x);
          assertEquals(1.0f, a.y);
          assertEquals(2.0f, a.z);
          assertEquals(3.0f, a.w);
      }

      @Test
      public void copy() {
          Vec4 a = new Vec4(1, 2, 3, 4);
          Vec4 copy1 = a.copy();
          assertEquals(1.0f, copy1.x);
          assertEquals(2.0f, copy1.y);
          assertEquals(3.0f, copy1.z);
          assertEquals(4.0f, copy1.w);

          Vec4 copy2 = new Vec4();
          a.copy(copy2);
          assertEquals(1.0f, copy2.x);
          assertEquals(2.0f, copy2.y);
          assertEquals(3.0f, copy2.z);
          assertEquals(4.0f, copy2.w);
      }

      @Test
      public void set() {
          Vec4 v = new Vec4();

          // Test set(x, y, z, w)
          v.set(1, 2, 3, 4);
          assertEquals(1.0f, v.x);
          assertEquals(2.0f, v.y);
          assertEquals(3.0f, v.z);
          assertEquals(4.0f, v.w);

          // Test set(Vec2, z, w)
          Vec2 v2 = new Vec2(5, 6);
          v.set(v2, 7, 8);
          assertEquals(5.0f, v.x);
          assertEquals(6.0f, v.y);
          assertEquals(7.0f, v.z);
          assertEquals(8.0f, v.w);

          // Test set(Vec3, w)
          Vec3 v3 = new Vec3(9, 10, 11);
          v.set(v3, 12);
          assertEquals(9.0f, v.x);
          assertEquals(10.0f, v.y);
          assertEquals(11.0f, v.z);
          assertEquals(12.0f, v.w);
      }

      @Test
      public void scale() {
          Vec4 a = new Vec4(1.0f, 2.0f, 3.0f, 4.0f);
          a.scale(2.0f);
          assertEquals(2.0f, a.x);
          assertEquals(4.0f, a.y);
          assertEquals(6.0f, a.z);
          assertEquals(8.0f, a.w);

          a.scale(0.5f, 2.0f, 0.5f, 0.25f);
          assertEquals(1.0f, a.x);
          assertEquals(8.0f, a.y);
          assertEquals(3.0f, a.z);
          assertEquals(2.0f, a.w);
      }

      @Test
      public void negate() {
          Vec4 a = new Vec4();
          a.set(1, 2, 3, 4).negate();
          assertEquals(-1.0f, a.x);
          assertEquals(-2.0f, a.y);
          assertEquals(-3.0f, a.z);
          assertEquals(-4.0f, a.w);
      }

      @Test
      public void dot() {
          Vec4 a = new Vec4(1.0f, 2.0f, 3.0f, 4.0f);
          Vec4 b = new Vec4(5.0f, 6.0f, 7.0f, 8.0f);
          float dot = a.dot(b);
          assertEquals(70.0f, dot);  // 1*5 + 2*6 + 3*7 + 4*8 = 5 + 12 + 21 + 32 = 70
      }

      @Test
      public void lengthSqr() {
          Vec4 a = new Vec4(1.0f, 2.0f, 3.0f, 4.0f);
          float l = a.lengthSqr();
          assertEquals(30.0f, l);  // 1 + 4 + 9 + 16 = 30
      }

      @Test
      public void length() {
          Vec4 a = new Vec4(2.0f, 4.0f, 4.0f, 0.0f);
          float l = a.length();
          assertEquals(6.0f, l);  // sqrt(4 + 16 + 16 + 0) = sqrt(36) = 6
      }

      @Test
      public void normalize() {
          Vec4 a = new Vec4(2.0f, 4.0f, 4.0f, 0.0f);
          a.normalize();
          assertEquals(1.0f, a.lengthSqr(), 0.0001f);
      }

      @Test
      public void distanceSqr() {
          Vec4 a = new Vec4(1.0f, 0.0f, 0.0f, 0.0f);
          Vec4 b = new Vec4(3.0f, 0.0f, 0.0f, 0.0f);
          float squaredDistance = a.distanceSqr(b);
          assertEquals(4.0f, squaredDistance);
      }

      @Test
      public void distance() {
          Vec4 a = new Vec4(0.0f, 1.0f, 0.0f, 0.0f);
          Vec4 b = new Vec4(0.0f, 3.0f, 0.0f, 0.0f);
          float dist = a.distance(b);
          assertEquals(2.0f, dist);
      }

      @Test
      public void lerp() {
          Vec4 start = new Vec4(0, 0, 0, 0);
          Vec4 target = new Vec4(4, 4, 4, 4);

          Vec4 dest = new Vec4();
          Vec4.lerp(start, target, 0.0f, dest);
          assertEquals(0.0f, dest.x);
          assertEquals(0.0f, dest.y);
          assertEquals(0.0f, dest.z);
          assertEquals(0.0f, dest.w);

          Vec4.lerp(start, target, 0.5f, dest);
          assertEquals(2.0f, dest.x);
          assertEquals(2.0f, dest.y);
          assertEquals(2.0f, dest.z);
          assertEquals(2.0f, dest.w);

          Vec4.lerp(start, target, 1.0f, dest);
          assertEquals(4.0f, dest.x);
          assertEquals(4.0f, dest.y);
          assertEquals(4.0f, dest.z);
          assertEquals(4.0f, dest.w);
      }

      @Test
      public void floatArray() {
          float[] dest = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
          Vec4 v = new Vec4(1.0f, 2.0f, 3.0f, 4.0f);
          v.toFloatArray(dest, 1);
          assertArrayEquals(new float[] { 0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 0.0f }, dest);

          Vec4 destV = new Vec4();
          Vec4.fromFloatArray(dest, 1, destV);
          assertEquals(1.0f, destV.x);
          assertEquals(2.0f, destV.y);
          assertEquals(3.0f, destV.z);
          assertEquals(4.0f, destV.w);
      }

      @Test
      public void memorySegment() {
          float[] dest = new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
          MemorySegment segment = MemorySegment.ofArray(dest);
          Vec4 v = new Vec4(1.0f, 2.0f, 3.0f, 4.0f);
          v.toMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize());
          assertArrayEquals(new float[] { 0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 0.0f }, dest);

          Vec4 destV = new Vec4();
          Vec4.fromMemorySegment(segment, ValueLayout.JAVA_FLOAT.byteSize(), destV);
          assertEquals(1.0f, destV.x);
          assertEquals(2.0f, destV.y);
          assertEquals(3.0f, destV.z);
          assertEquals(4.0f, destV.w);
      }
  }
