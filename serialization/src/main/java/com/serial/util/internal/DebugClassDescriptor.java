package com.serial.util.internal;

import org.jetbrains.annotations.NotNull;

public class DebugClassDescriptor {
    public final int version;
    @NotNull public final String className;

    public DebugClassDescriptor(int version) {
        this(version, "");
    }

    public DebugClassDescriptor(int version, @NotNull String className) {
        this.version = version;
        this.className = className;
    }
}
