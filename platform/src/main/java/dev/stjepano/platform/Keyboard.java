package dev.stjepano.platform;

/// You can use this interface to query key states for a {@link Window}.
///
/// **IMPORTANT**: Keyboard querying with {@link Key} codes is layout independent. So when you are asking
/// `isPressed(Key.W)` you are basically asking if whatever key that is on the same position as `W` is on
/// US ASCII keyboard is pressed. Key codes disregard OS layout.
@SuppressWarnings("unused")
public interface Keyboard {

    // TODO: add support for text input

    /// Return true if key is down.
    boolean isPressed(Key key);

    /// Return true if key was pressed "this frame".
    ///
    /// This will return true only in case of key half-transition from unpressed to pressed state.
    boolean wasJustPressed(Key key);

    ///  Return true if key was released "this frame".
    ///
    /// This will return true only in case of key half-transition from pressed to depressed state.
    boolean wasJustReleased(Key key);

    /// If key is printable then returns the actual OS and keyboard layout dependent character that this key would "type".
    /// If key is not printable returns 0.
    char getPrintableKeyChar(Key key);
}
