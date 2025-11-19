package dev.stjepano.platform.opengl;

import static dev.stjepano.platform.opengl.GLConst.*;

public record BufferStorageFlags(int glFlags) {

    /// When buffer is created with this flag, you can update the buffer with `update`.
    /// Without this flag the buffer is effectively immutable.
    public boolean isDynamicStorage() {
        return (this.glFlags & DYNAMIC_STORAGE_BIT) != 0;
    }

    /// Allows mapping buffer for CPU reading.
    public boolean isMapRead() {
        return (this.glFlags & MAP_READ_BIT) != 0;
    }

    /// Allows mapping buffer for CPU writing.
    public boolean isMapWrite() {
        return (this.glFlags & MAP_WRITE_BIT) != 0;
    }

    /// Keeps mapped pointer valid across gl calls.
    public boolean isMapPersistent() {
        return (this.glFlags & MAP_PERSISTENT_BIT) != 0;
    }

    /// Automatic CPU/GPU synchronization (no memory barriers needed).
    public boolean isMapCoherent() {
        return (this.glFlags & MAP_COHERENT_BIT) != 0;
    }

    /// Hint to prefer system RAM over GPU RAM.
    public boolean isClientStorage() {
        return (this.glFlags & CLIENT_STORAGE_BIT) != 0;
    }

    public static class Builder {
        private int flags;

        public Builder dynamicStorage() {
            flags |= DYNAMIC_STORAGE_BIT;
            return this;
        }

        public Builder mapRead() {
            flags |= MAP_READ_BIT;
            return this;
        }

        public Builder mapWrite() {
            flags |= MAP_WRITE_BIT;
            return this;
        }

        public Builder mapPersistent() {
            flags |= MAP_PERSISTENT_BIT;
            return this;
        }

        public Builder mapCoherent() {
            flags |= MAP_COHERENT_BIT;
            return this;
        }

        public Builder clientStorage() {
            flags |= CLIENT_STORAGE_BIT;
            return this;
        }

        public BufferStorageFlags build() {
            return new BufferStorageFlags(this.flags);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
