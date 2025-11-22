package dev.stjepano.math;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.jupiter.api.Assertions.*;

class Mat4Test {

    private static final float EPSILON = 0.0001f;

    @Test
    public void defaultConstructor() {
        Mat4 m = new Mat4();
        // Should be identity
        assertEquals(1.0f, m.m00);
        assertEquals(0.0f, m.m01);
        assertEquals(0.0f, m.m02);
        assertEquals(0.0f, m.m03);

        assertEquals(0.0f, m.m10);
        assertEquals(1.0f, m.m11);
        assertEquals(0.0f, m.m12);
        assertEquals(0.0f, m.m13);

        assertEquals(0.0f, m.m20);
        assertEquals(0.0f, m.m21);
        assertEquals(1.0f, m.m22);
        assertEquals(0.0f, m.m23);

        assertEquals(0.0f, m.m30);
        assertEquals(0.0f, m.m31);
        assertEquals(0.0f, m.m32);
        assertEquals(1.0f, m.m33);
    }

    @Test
    public void elementConstructor() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        assertEquals(1.0f, m.m00);
        assertEquals(2.0f, m.m01);
        assertEquals(3.0f, m.m02);
        assertEquals(4.0f, m.m03);
        assertEquals(5.0f, m.m10);
        assertEquals(16.0f, m.m33);
    }

    @Test
    public void columnConstructor() {
        Vec4 col0 = new Vec4(1, 2, 3, 4);
        Vec4 col1 = new Vec4(5, 6, 7, 8);
        Vec4 col2 = new Vec4(9, 10, 11, 12);
        Vec4 col3 = new Vec4(13, 14, 15, 16);

        Mat4 m = new Mat4(col0, col1, col2, col3);

        // Column 0
        assertEquals(1.0f, m.m00);
        assertEquals(2.0f, m.m10);
        assertEquals(3.0f, m.m20);
        assertEquals(4.0f, m.m30);

        // Column 1
        assertEquals(5.0f, m.m01);
        assertEquals(6.0f, m.m11);
        assertEquals(7.0f, m.m21);
        assertEquals(8.0f, m.m31);

        // Column 3
        assertEquals(13.0f, m.m03);
        assertEquals(14.0f, m.m13);
        assertEquals(15.0f, m.m23);
        assertEquals(16.0f, m.m33);
    }

    @Test
    public void copy() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        Mat4 copy1 = m.copy();
        assertEquals(1.0f, copy1.m00);
        assertEquals(8.0f, copy1.m13);
        assertEquals(16.0f, copy1.m33);

        Mat4 copy2 = new Mat4();
        m.copy(copy2);
        assertEquals(1.0f, copy2.m00);
        assertEquals(8.0f, copy2.m13);
        assertEquals(16.0f, copy2.m33);
    }

    @Test
    public void setIdentity() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        m.setIdentity();

        assertEquals(1.0f, m.m00);
        assertEquals(0.0f, m.m01);
        assertEquals(1.0f, m.m11);
        assertEquals(0.0f, m.m12);
        assertEquals(1.0f, m.m33);
    }

    @Test
    public void setColumn() {
        Mat4 m = new Mat4();
        Vec4 col = new Vec4(1, 2, 3, 4);
        m.setColumn(2, col);

        assertEquals(1.0f, m.m02);
        assertEquals(2.0f, m.m12);
        assertEquals(3.0f, m.m22);
        assertEquals(4.0f, m.m32);
    }

    @Test
    public void setRow() {
        Mat4 m = new Mat4();
        Vec4 row = new Vec4(1, 2, 3, 4);
        m.setRow(1, row);

        assertEquals(1.0f, m.m10);
        assertEquals(2.0f, m.m11);
        assertEquals(3.0f, m.m12);
        assertEquals(4.0f, m.m13);
    }

    @Test
    public void scale() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        m.scale(2.0f);

        assertEquals(2.0f, m.m00);
        assertEquals(4.0f, m.m01);
        assertEquals(16.0f, m.m13);
        assertEquals(32.0f, m.m33);
    }

    @Test
    public void add() {
        Mat4 a = new Mat4();  // Identity
        Mat4 b = new Mat4();  // Identity
        a.add(b);

        assertEquals(2.0f, a.m00);
        assertEquals(0.0f, a.m01);
        assertEquals(2.0f, a.m11);
        assertEquals(2.0f, a.m22);
        assertEquals(2.0f, a.m33);
    }

    @Test
    public void sub() {
        Mat4 a = new Mat4();  // Identity
        Mat4 b = new Mat4();  // Identity
        a.sub(b);

        assertEquals(0.0f, a.m00);
        assertEquals(0.0f, a.m11);
        assertEquals(0.0f, a.m22);
        assertEquals(0.0f, a.m33);
    }

    @Test
    public void mulIdentity() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Mat4 original = m.copy();
        Mat4 identity = new Mat4();

        m.mul(identity);

        // M * I = M
        assertEquals(original.m00, m.m00, EPSILON);
        assertEquals(original.m01, m.m01, EPSILON);
        assertEquals(original.m13, m.m13, EPSILON);
        assertEquals(original.m33, m.m33, EPSILON);
    }

    @Test
    public void mulBasic() {
        // Simple test: scale matrix * identity
        Mat4 scale = new Mat4();
        scale.m00 = 2.0f;
        scale.m11 = 3.0f;
        scale.m22 = 4.0f;
        scale.m33 = 1.0f;

        Mat4 identity = new Mat4();
        scale.mul(identity);

        assertEquals(2.0f, scale.m00, EPSILON);
        assertEquals(3.0f, scale.m11, EPSILON);
        assertEquals(4.0f, scale.m22, EPSILON);
        assertEquals(1.0f, scale.m33, EPSILON);
    }

    @Test
    public void transpose() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        m.transpose();

        // Row 0 becomes column 0
        assertEquals(1.0f, m.m00);
        assertEquals(5.0f, m.m01);
        assertEquals(9.0f, m.m02);
        assertEquals(13.0f, m.m03);

        // Row 1 becomes column 1
        assertEquals(2.0f, m.m10);
        assertEquals(6.0f, m.m11);
        assertEquals(10.0f, m.m12);
        assertEquals(14.0f, m.m13);

        // Row 3 becomes column 3
        assertEquals(4.0f, m.m30);
        assertEquals(8.0f, m.m31);
        assertEquals(12.0f, m.m32);
        assertEquals(16.0f, m.m33);
    }

    @Test
    public void transposeOfTranspose() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Mat4 original = m.copy();

        m.transpose().transpose();

        // (M^T)^T = M
        assertEquals(original.m00, m.m00, EPSILON);
        assertEquals(original.m13, m.m13, EPSILON);
        assertEquals(original.m31, m.m31, EPSILON);
        assertEquals(original.m33, m.m33, EPSILON);
    }

    @Test
    public void determinantIdentity() {
        Mat4 m = new Mat4();
        float det = m.determinant();
        assertEquals(1.0f, det, EPSILON);
    }

    @Test
    public void determinantZero() {
        // All zeros = determinant 0
        Mat4 m = new Mat4(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
        float det = m.determinant();
        assertEquals(0.0f, det, EPSILON);
    }

    @Test
    public void determinantScale() {
        // Scale matrix with scale factors 2, 3, 4, 1
        // det = product of diagonal = 2*3*4*1 = 24
        Mat4 m = new Mat4();
        m.m00 = 2.0f;
        m.m11 = 3.0f;
        m.m22 = 4.0f;
        m.m33 = 1.0f;

        float det = m.determinant();
        assertEquals(24.0f, det, EPSILON);
    }

    @Test
    public void invertIdentity() {
        Mat4 m = new Mat4();
        m.invert();

        // Identity inverted is identity
        assertEquals(1.0f, m.m00, EPSILON);
        assertEquals(0.0f, m.m01, EPSILON);
        assertEquals(1.0f, m.m11, EPSILON);
        assertEquals(1.0f, m.m22, EPSILON);
        assertEquals(1.0f, m.m33, EPSILON);
    }

    @Test
    public void invertScale() {
        // Scale by 2,3,4 -> inverse should scale by 0.5, 1/3, 0.25
        Mat4 m = new Mat4();
        m.m00 = 2.0f;
        m.m11 = 3.0f;
        m.m22 = 4.0f;
        m.m33 = 1.0f;

        m.invert();

        assertEquals(0.5f, m.m00, EPSILON);
        assertEquals(1.0f/3.0f, m.m11, EPSILON);
        assertEquals(0.25f, m.m22, EPSILON);
        assertEquals(1.0f, m.m33, EPSILON);
    }

    @Test
    public void invertTimesOriginalIsIdentity() {
        // Create a non-trivial invertible matrix
        Mat4 m = new Mat4(
                2, 0, 0, 1,
                0, 3, 0, 2,
                0, 0, 4, 3,
                0, 0, 0, 1
        );

        Mat4 inverse = m.copy();
        inverse.invert();

        Mat4 result = m.copy();
        result.mul(inverse);

        // M * M^-1 = I
        assertEquals(1.0f, result.m00, EPSILON);
        assertEquals(0.0f, result.m01, EPSILON);
        assertEquals(0.0f, result.m02, EPSILON);
        assertEquals(0.0f, result.m03, EPSILON);

        assertEquals(0.0f, result.m10, EPSILON);
        assertEquals(1.0f, result.m11, EPSILON);
        assertEquals(0.0f, result.m12, EPSILON);
        assertEquals(0.0f, result.m13, EPSILON);

        assertEquals(0.0f, result.m20, EPSILON);
        assertEquals(0.0f, result.m21, EPSILON);
        assertEquals(1.0f, result.m22, EPSILON);
        assertEquals(0.0f, result.m23, EPSILON);

        assertEquals(0.0f, result.m30, EPSILON);
        assertEquals(0.0f, result.m31, EPSILON);
        assertEquals(0.0f, result.m32, EPSILON);
        assertEquals(1.0f, result.m33, EPSILON);
    }

    @Test
    public void floatArray() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        float[] array = new float[16];
        m.toFloatArray(array);

        // Row-major order
        assertArrayEquals(new float[] {
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        }, array);

        Mat4 m2 = new Mat4();
        Mat4.fromFloatArray(array, 0, m2);

        assertEquals(1.0f, m2.m00);
        assertEquals(2.0f, m2.m01);
        assertEquals(8.0f, m2.m13);
        assertEquals(16.0f, m2.m33);
    }

    @Test
    public void floatArrayWithOffset() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        float[] array = new float[20];
        m.toFloatArray(array, 2);

        assertEquals(0.0f, array[0]);
        assertEquals(0.0f, array[1]);
        assertEquals(1.0f, array[2]);
        assertEquals(2.0f, array[3]);
        assertEquals(16.0f, array[17]);
        assertEquals(0.0f, array[18]);
        assertEquals(0.0f, array[19]);
    }

    @Test
    public void memorySegment() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        float[] array = new float[16];
        MemorySegment segment = MemorySegment.ofArray(array);

        m.toMemorySegment(segment, 0);

        // Verify row-major order
        assertEquals(1.0f, array[0]);
        assertEquals(2.0f, array[1]);
        assertEquals(8.0f, array[7]);
        assertEquals(16.0f, array[15]);

        Mat4 m2 = new Mat4();
        Mat4.fromMemorySegment(segment, 0, m2);

        assertEquals(1.0f, m2.m00);
        assertEquals(2.0f, m2.m01);
        assertEquals(8.0f, m2.m13);
        assertEquals(16.0f, m2.m33);
    }

    @Test
    public void memorySegmentWithOffset() {
        Mat4 m = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        float[] array = new float[20];
        MemorySegment segment = MemorySegment.ofArray(array);

        long offset = ValueLayout.JAVA_FLOAT.byteSize() * 2;
        m.toMemorySegment(segment, offset);

        assertEquals(0.0f, array[0]);
        assertEquals(0.0f, array[1]);
        assertEquals(1.0f, array[2]);
        assertEquals(2.0f, array[3]);
        assertEquals(16.0f, array[17]);
    }

    @Test
    public void equalsAndHashCode() {
        Mat4 m1 = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        Mat4 m2 = new Mat4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        Mat4 m3 = new Mat4();

        assertTrue(m1.equals(m2));
        assertFalse(m1.equals(m3));
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void toStringFormat() {
        Mat4 m = new Mat4();
        String str = m.toString();
        assertTrue(str.contains("Mat4"));
        assertTrue(str.contains("1.0"));  // Identity diagonal
    }
}
