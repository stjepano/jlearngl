package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.opengl.Buffer;
import dev.stjepano.platform.opengl.BufferStorageFlags;
import dev.stjepano.platform.opengl.MapAccessFlags;
import dev.stjepano.platform.opengl.OpenGLException;

import java.lang.foreign.MemorySegment;

final class BufferImpl implements Buffer {
    /* package */ int glId;
    private final long size;
    private final BufferStorageFlags flags;

    BufferImpl(int glId, long size, BufferStorageFlags flags) {
        this.glId = glId;
        this.size = size;
        this.flags = flags;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public BufferStorageFlags flags() {
        return this.flags;
    }

    @Override
    public void update(long offset, long size, MemorySegment data) {
        checkValid();
        if (!JGL.jglNamedBufferSubData(glId, offset, size, data)) {
            throw new OpenGLException("Failed to update buffer!");
        }
    }

    @Override
    public MemorySegment map(long offset, long length, MapAccessFlags flags) {
        checkValid();
        MemorySegment segment = JGL.jglMapNamedBufferRange(glId, offset, length, flags.glFlags());
        if (segment.address() == 0L) {
            throw new OpenGLException("Failed to map buffer!");
        }
        return segment;
    }

    @Override
    public void unmap() {
        checkValid();
        if (!JGL.jglUnmapNamedBuffer(glId)) {
            throw new OpenGLException("Failed to unmap buffer!");
        }
    }

    @Override
    public void flushMappedBufferRange(long offset, long length) {
        checkValid();
        if (!JGL.jglFlushMappedNamedBufferRange(glId, offset, length)) {
            throw new OpenGLException("Failed to flush buffer range!");
        }
    }

    private void checkValid() {
        if (glId == 0) {
            throw new OpenGLException("Buffer is not valid!");
        }
    }

    @Override
    public void delete() {
        if (glId > 0) {
            JGL.jglDeleteBuffers(glId);
            glId = 0;
        }
    }
}
