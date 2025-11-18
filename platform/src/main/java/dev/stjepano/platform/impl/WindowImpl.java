package dev.stjepano.platform.impl;

import dev.stjepano.platform.Keyboard;
import dev.stjepano.platform.Mouse;
import dev.stjepano.platform.Window;

import java.lang.foreign.MemorySegment;

final class WindowImpl implements Window {

    /* package */ MemorySegment windowPtr;

    private KeyboardImpl cachedKeyboard = null;
    private MouseImpl cachedMouse = null;

    public WindowImpl(MemorySegment windowPtr) {
        this.windowPtr = windowPtr;
    }

    @Override
    public Keyboard keyboard() {
        if (cachedKeyboard == null) {
            cachedKeyboard = new KeyboardImpl(this);
        }
        return cachedKeyboard;
    }

    @Override
    public Mouse mouse() {
        if (cachedMouse == null) {
            cachedMouse = new MouseImpl(this);
        }
        return cachedMouse;
    }

    @Override
    public boolean shouldClose() {
        throwIfClosed();
        return NativePlatform.WindowStructPtr.shouldClose(this.windowPtr);
    }

    @Override
    public void setShouldClose(boolean value) {
        throwIfClosed();
        NativePlatform.glfw3WindowSetShouldClose(this.windowPtr, value);
    }

    @Override
    public void swapBuffers() {
        throwIfClosed();
        NativePlatform.glfw3SwapBuffers(this.windowPtr);
    }

    @Override
    public int width() {
        throwIfClosed();
        return NativePlatform.WindowStructPtr.width(this.windowPtr);
    }

    @Override
    public int height() {
        throwIfClosed();
        return NativePlatform.WindowStructPtr.height(this.windowPtr);
    }

    @Override
    public int framebufferWidth() {
        throwIfClosed();
        return NativePlatform.WindowStructPtr.framebufferWidth(this.windowPtr);
    }

    @Override
    public int framebufferHeight() {
        throwIfClosed();
        return NativePlatform.WindowStructPtr.framebufferHeight(this.windowPtr);
    }

    @Override
    public void close() {
        if (windowPtr != null) {
            NativePlatform.glfw3WindowClose(windowPtr);
            windowPtr = null;
        }
    }

    void throwIfClosed() {
        if (windowPtr == null) {
            throw new RuntimeException("Window is closed.");
        }
    }
}
