package dev.stjepano.platform;

@SuppressWarnings("unused")
public interface Mouse {

    // TODO: support for raw mouse motion
    // TODO: support for horizontal scroll offset
    // TODO: mouse enter/leave

    /// Return true if mouse button is pressed.
    boolean isPressed(MButton button);

    /// Return true if mouse button was pressed this frame.
    ///
    /// This returns true only in case of half-transition from depressed to pressed state.
    boolean wasJustPressed(MButton button);

    /// Return true if mouse button was released this frame.
    ///
    /// This returns true only in case of half-transition from pressed to depressed state.
    boolean wasJustReleased(MButton button);

    /// The x coordinate of mouse cursor (relative to window left side).
    float x();

    /// The y coordinate of mouse cursor (relative to window top side - grows as you get closer to bottom).
    float y();

    /// Gives you vertical scroll offset for current frame only.
    float verticalScrollOffset();

}
