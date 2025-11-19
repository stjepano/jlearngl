package dev.stjepano.platform.opengl;

import java.lang.foreign.MemorySegment;

public interface Buffer extends GpuResource {
    /// Buffer size in bytes.
    long size();

    /// Flags used to create the buffer.
    BufferStorageFlags flags();

    /// Update entire or part of buffer. Your buffer must be `dynamic storage` buffer.
    /// @param offset specifies the offset into the buffer object's data store, in bytes
    /// @param size specifies the size in bytes of the data store region being replaced
    /// @param data pointer to the new data that will be copied to store
    /// @throws OpenGLException on OpenGL error
    void update(long offset, long size, MemorySegment data);

    /// Update entire buffer with provided data.
    /// @param data pointer to the new data that will be copied to store
    /// @throws OpenGLException on OpenGL error
    default void update(MemorySegment data) {
        update(0, Math.min(data.byteSize(), size()), data);
    }

    /// Map entire or part of buffer for read and/or write access.
    /// @param offset start of the buffer subrange in bytes
    /// @param length length of the buffer subrange in bytes
    /// @param flags combination of access flags indicating the desired access to mapped range
    /// @throws OpenGLException on OpenGL error
    MemorySegment map(long offset, long length, MapAccessFlags flags);

    /// Unmap buffer or buffer range previously mapped by `map`.
    /// @throws OpenGLException on OpenGL error
    void unmap();

    /// Indicates the modifications have been made to a range of mapped buffer object. Buffer must be mapped
    /// with `flush explicit` access flag.
    /// @param offset start of the buffer subrange in bytes
    /// @param length length of the buffer subrange in bytes
    /// @throws OpenGLException on OpenGL error
    void flushMappedBufferRange(long offset, long length);

}
