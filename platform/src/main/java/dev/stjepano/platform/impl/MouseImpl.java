package dev.stjepano.platform.impl;

import dev.stjepano.platform.MButton;
import dev.stjepano.platform.Mouse;

class MouseImpl implements Mouse {
    private final WindowImpl window;

    public MouseImpl(WindowImpl window) {
        this.window = window;
    }

    @Override
    public boolean isPressed(MButton button) {
        window.throwIfClosed();
        byte buttonState = NativePlatform.WindowStructPtr.button(window.windowPtr, button);
        return (buttonState & NativePlatform.INPUT_STATE_PRESSED) != 0;
    }

    @Override
    public boolean wasJustPressed(MButton button) {
        window.throwIfClosed();
        byte buttonState = NativePlatform.WindowStructPtr.button(window.windowPtr, button);
        return (buttonState & NativePlatform.INPUT_STATE_PRESSED) != 0
                && (buttonState & NativePlatform.INPUT_STATE_TRANSITIONED) != 0;
    }

    @Override
    public boolean wasJustReleased(MButton button) {
        window.throwIfClosed();
        byte buttonState = NativePlatform.WindowStructPtr.button(window.windowPtr, button);
        return (buttonState & NativePlatform.INPUT_STATE_PRESSED) == 0
                && (buttonState & NativePlatform.INPUT_STATE_TRANSITIONED) != 0;
    }

    @Override
    public float x() {
        window.throwIfClosed();
        return NativePlatform.WindowStructPtr.mouseX(window.windowPtr);
    }

    @Override
    public float y() {
        window.throwIfClosed();
        return NativePlatform.WindowStructPtr.mouseY(window.windowPtr);
    }

    @Override
    public float verticalScrollOffset() {
        window.throwIfClosed();
        return NativePlatform.WindowStructPtr.verticalScrollOffset(window.windowPtr);
    }
}
