package dev.stjepano.platform.util;

import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;

/// Glue class that allows you to initialize package private JSTBI from some other package.
public final class JSTBIInit {

    public static void init(Linker linker, SymbolLookup symbolLookup) {
        JSTBI.init(linker, symbolLookup);
    }
}
