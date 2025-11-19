package dev.stjepano.platform.opengl;

public record MapAccessFlags(int glFlags) {

    /// Mapped buffer may be used for reading.
    public boolean isMapRead() {
        return (this.glFlags & GLConst.MAP_READ_BIT) != 0;
    }

    /// Mapped buffer may be used for writing.
    public boolean isMapWrite() {
        return (this.glFlags & GLConst.MAP_WRITE_BIT) != 0;
    }

    /// Mapped pointer remains valid across GL calls.
    public boolean isMapPersistent() {
        return (this.glFlags & GLConst.MAP_PERSISTENT_BIT) != 0;
    }

    /// Guarantee that writes to the buffer will be visible by GPU.
    public boolean isMapCoherent() {
        return (this.glFlags & GLConst.MAP_COHERENT_BIT) != 0;
    }

    /// Previous content of the specified range may be discarder.
    public boolean isMapInvalidateRange() {
        return (this.glFlags & GLConst.MAP_INVALIDATE_RANGE_BIT) != 0;
    }

    /// Previous content of entire buffer may be discarded.
    public boolean isMapInvalidateBuffer() {
        return (this.glFlags & GLConst.MAP_INVALIDATE_BUFFER_BIT) != 0;
    }

    /// One or more discrete subranges of mapping may be modified. Modifications to each subrange must be explicitly
    /// flushed by calling `flushMappedBufferRange`.
    public boolean isMapFlushExplicit() {
        return (this.glFlags & GLConst.MAP_FLUSH_EXPLICIT_BIT) != 0;
    }

    /// GL should not synchronize pending operations on buffer prior to return from `map`.
    public boolean isMapUnsynchronized() {
        return (this.glFlags & GLConst.MAP_UNSYNCHRONIZED_BIT) != 0;
    }

    public static class Builder {
        private int flags;

        public Builder mapRead() {
            flags |= GLConst.MAP_READ_BIT;
            return this;
        }

        public Builder mapWrite() {
            flags |= GLConst.MAP_WRITE_BIT;
            return this;
        }

        public Builder mapPersistent() {
            flags |= GLConst.MAP_PERSISTENT_BIT;
            return this;
        }

        public Builder mapCoherent() {
            flags |= GLConst.MAP_COHERENT_BIT;
            return this;
        }

        public Builder mapInvalidateRange() {
            flags |= GLConst.MAP_INVALIDATE_RANGE_BIT;
            return this;
        }

        public Builder mapInvalidateBuffer() {
            flags |= GLConst.MAP_INVALIDATE_BUFFER_BIT;
            return this;
        }

        public Builder mapFlushExplicit() {
            flags |= GLConst.MAP_FLUSH_EXPLICIT_BIT;
            return this;
        }

        public Builder mapUnsynchronized() {
            flags |= GLConst.MAP_UNSYNCHRONIZED_BIT;
            return this;
        }

        public MapAccessFlags build() {
            return new MapAccessFlags(this.flags);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
