package dev.stjepano.platform.impl.opengl;

import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;

/// Glue class that allows you to initialize package private JGL from some other package.
public final class JGLInit {
    public static void init(Linker linker, SymbolLookup symbolLookup) {
        JGL.init(linker, symbolLookup);
    }
}
