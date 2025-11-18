package dev.stjepano.platform.impl;

import dev.stjepano.platform.Key;
import dev.stjepano.platform.Keyboard;

class KeyboardImpl implements Keyboard {
    private final WindowImpl windowImpl;

    public KeyboardImpl(WindowImpl windowImpl) {
        this.windowImpl = windowImpl;
    }

    @Override
    public boolean isPressed(Key key) {
        windowImpl.throwIfClosed();
        return (NativePlatform.WindowStructPtr.key(windowImpl.windowPtr, key) & NativePlatform.INPUT_STATE_PRESSED) != 0;
    }

    @Override
    public boolean wasJustPressed(Key key) {
        windowImpl.throwIfClosed();
        byte keyState = NativePlatform.WindowStructPtr.key(windowImpl.windowPtr, key);
        return (keyState & NativePlatform.INPUT_STATE_PRESSED) != 0
                && (keyState & NativePlatform.INPUT_STATE_TRANSITIONED) != 0;
    }

    @Override
    public boolean wasJustReleased(Key key) {
        windowImpl.throwIfClosed();
        byte keyState = NativePlatform.WindowStructPtr.key(windowImpl.windowPtr, key);
        return (keyState & NativePlatform.INPUT_STATE_PRESSED) == 0
                && (keyState & NativePlatform.INPUT_STATE_TRANSITIONED) != 0;
    }

    @Override
    public char getPrintableKeyChar(Key key) {
        // todo:
        return 0;
    }
}
