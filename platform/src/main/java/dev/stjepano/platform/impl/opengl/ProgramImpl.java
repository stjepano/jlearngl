package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.memory.StackAllocator;
import dev.stjepano.platform.opengl.OpenGLException;
import dev.stjepano.platform.opengl.Program;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

final class ProgramImpl implements Program {
    /* package */ int glId;

    ProgramImpl(int glId) {
        this.glId = glId;
    }

    @Override
    public void delete() {
        if (glId != 0) {
            JGL.jglDeleteProgram(glId);
            glId = 0;
        }
    }

    private void checkValid() {
        if (glId == 0) {
            throw new OpenGLException("Program is not valid!");
        }
    }

    @Override
    public void setInt(int location, int i) {
        checkValid();
        JGL.jglProgramUniform1i(glId, location, i);
    }

    @Override
    public void setIntArray(int location, int[] array) {
        checkValid();
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_INT, array);
            JGL.jglProgramUniform1iv(glId, location, array.length, valuePtr);
        }
    }

    @Override
    public void setFloat(int location, float x) {
        checkValid();
        JGL.jglProgramUniform1f(glId, location, x);
    }

    @Override
    public void setFloatArray(int location, float[] array) {
        checkValid();
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            JGL.jglProgramUniform1fv(glId, location, array.length, valuePtr);
        }
    }

    @Override
    public void setFloat2(int location, float x, float y) {
        checkValid();
        JGL.jglProgramUniform2f(glId, location, x, y);
    }

    @Override
    public void setFloat2Array(int location, float[] array) {
        checkValid();
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException("array.length not multiple of 2");
        }
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            JGL.jglProgramUniform2fv(glId, location, array.length / 2, valuePtr);
        }
    }

    @Override
    public void setFloat3(int location, float x, float y, float z) {
        checkValid();
        JGL.jglProgramUniform3f(glId, location, x, y, z);
    }

    @Override
    public void setFloat3Array(int location, float[] array) {
        checkValid();
        if (array.length % 3 != 0) {
            throw new IllegalArgumentException("array.length not multiple of 3");
        }
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            JGL.jglProgramUniform3fv(glId, location, array.length / 3, valuePtr);
        }
    }

    @Override
    public void setFloat4(int location, float x, float y, float z, float w) {
        checkValid();
        JGL.jglProgramUniform4f(glId, location, x, y, z, w);
    }

    @Override
    public void setFloat4Array(int location, float[] array) {
        checkValid();
        if (array.length % 4 != 0) {
            throw new IllegalArgumentException("array.length not multiple of 4");
        }
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            JGL.jglProgramUniform4fv(glId, location, array.length / 4, valuePtr);
        }
    }

    @Override
    public void setMat2Array(int location, boolean transpose, float[] array) {
        checkValid();
        if (array.length % 4 != 0) {
            throw new IllegalArgumentException("array.length not multiple of 4");
        }
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            int numMatrices = array.length / 4;
            JGL.jglProgramUniformMatrix2fv(glId, location, numMatrices, transpose, valuePtr);
        }
    }

    @Override
    public void setMat3Array(int location, boolean transpose, float[] array) {
        checkValid();
        if (array.length % 9 != 0) {
            throw new IllegalArgumentException("array.length not multiple of 9");
        }
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            int numMatrices = array.length / 9;
            JGL.jglProgramUniformMatrix3fv(glId, location, numMatrices, transpose, valuePtr);
        }
    }

    @Override
    public void setMat4Array(int location, boolean transpose, float[] array) {
        checkValid();
        if (array.length % 16 != 0) {
            throw new IllegalArgumentException("array.length not multiple of 16");
        }
        try (StackAllocator alloc = StackAllocator.push()) {
            MemorySegment valuePtr = alloc.allocateFrom(ValueLayout.JAVA_FLOAT, array);
            int numMatrices = array.length / 16;
            JGL.jglProgramUniformMatrix4fv(glId, location, numMatrices, transpose, valuePtr);
        }
    }
}
