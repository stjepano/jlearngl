package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
  import java.lang.foreign.ValueLayout;
  import java.util.Objects;

  public final class Vec4 {
      public float x, y, z, w;

      /// Default constructor (0, 0, 0, 0)
      public Vec4() {
          this(0, 0, 0, 0);
      }

      /// Value constructor
      public Vec4(float x, float y, float z, float w) {
          this.x = x;
          this.y = y;
          this.z = z;
          this.w = w;
      }

      /// Copy into new object and return it.
      public Vec4 copy() {
          return new Vec4(this.x, this.y, this.z, this.w);
      }

      /// Allocation free copy.
      public void copy(Vec4 dest) {
          dest.x = x;
          dest.y = y;
          dest.z = z;
          dest.w = w;
      }

      /// Set vector components.
      public Vec4 set(float x, float y, float z, float w) {
          this.x = x;
          this.y = y;
          this.z = z;
          this.w = w;
          return this;
      }

      /// Set vector components from other vector.
      public Vec4 set(Vec4 other) {
          this.x = other.x;
          this.y = other.y;
          this.z = other.z;
          this.w = other.w;
          return this;
      }

      /// Set from Vec2 with z and w components.
      public Vec4 set(Vec2 other, float z, float w) {
          this.x = other.x;
          this.y = other.y;
          this.z = z;
          this.w = w;
          return this;
      }

      /// Set from Vec3 with w component.
      public Vec4 set(Vec3 other, float w) {
          this.x = other.x;
          this.y = other.y;
          this.z = other.z;
          this.w = w;
          return this;
      }

      /// Add: this += other.
      public Vec4 add(Vec4 other) {
          this.x += other.x;
          this.y += other.y;
          this.z += other.z;
          this.w += other.w;
          return this;
      }

      /// Subtract: this -= other.
      public Vec4 sub(Vec4 other) {
          this.x -= other.x;
          this.y -= other.y;
          this.z -= other.z;
          this.w -= other.w;
          return this;
      }

      /// Scale: this *= s.
      public Vec4 scale(float s) {
          this.x *= s;
          this.y *= s;
          this.z *= s;
          this.w *= s;
          return this;
      }

      /// Negate: this = -this.
      public Vec4 negate() {
          return scale(-1.0f);
      }

      /// Component-wise scale: this.x *= sx, this.y *= sy, this.z *= sz, this.w *= sw
      public Vec4 scale(float sx, float sy, float sz, float sw) {
          this.x *= sx;
          this.y *= sy;
          this.z *= sz;
          this.w *= sw;
          return this;
      }

      /// Dot product.
      public float dot(Vec4 other) {
          return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
      }

      /// Return squared length.
      public float lengthSqr() {
          return this.dot(this);
      }

      /// Return vector length.
      public float length() {
          return (float) Math.sqrt(lengthSqr());
      }

      /// Normalize the vector.
      public Vec4 normalize() {
          float len = length();
          if (len < 1e-8f) {
              return this;
          }
          return scale(1.0f / len);
      }

      /// Squared distance between this vector and other vector.
      public float distanceSqr(Vec4 other) {
          float dx = x - other.x;
          float dy = y - other.y;
          float dz = z - other.z;
          float dw = w - other.w;
          return dx*dx + dy*dy + dz*dz + dw*dw;
      }

      /// Distance between this vector and other vector.
      public float distance(Vec4 other) {
          return (float) Math.sqrt(distanceSqr(other));
      }

      /// Linearly interpolate between source and target. Store result in dest.
      public static void lerp(Vec4 source, Vec4 target, float t, Vec4 dest) {
          dest.x = source.x + (target.x - source.x) * t;
          dest.y = source.y + (target.y - source.y) * t;
          dest.z = source.z + (target.z - source.z) * t;
          dest.w = source.w + (target.w - source.w) * t;
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

      /// Initialize Vec4 from float array, first component at src + offset.
      public static void fromFloatArray(float[] src, int offset, Vec4 dest) {
          dest.x = src[offset];
          dest.y = src[offset + 1];
          dest.z = src[offset + 2];
          dest.w = src[offset + 3];
      }

      /// Initialize Vec4 from memory segment, first component at src + byteOffset.
      public static void fromMemorySegment(MemorySegment src, long byteOffset, Vec4 dest) {
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

      @Override
      public String toString() {
          return "Vec4(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
      }

      @Override
      public boolean equals(Object o) {
          if (o == null || getClass() != o.getClass()) return false;
          Vec4 vec4 = (Vec4) o;
          return Float.compare(x, vec4.x) == 0 &&
                 Float.compare(y, vec4.y) == 0 &&
                 Float.compare(z, vec4.z) == 0 &&
                 Float.compare(w, vec4.w) == 0;
      }

      @Override
      public int hashCode() {
          return Objects.hash(x, y, z, w);
      }
  }
