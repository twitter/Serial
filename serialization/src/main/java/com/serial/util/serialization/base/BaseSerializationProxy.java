package com.serial.util.serialization.base;

import com.serial.util.serialization.serializer.Serializer;
import com.serial.util.serialization.SerializationContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A base class for the serialization proxy of a model object that doesn't have a builder.
 */
@SuppressWarnings("BlacklistedInterface")
public abstract class BaseSerializationProxy<T> implements Externalizable {
    private static final long serialVersionUID = 6518447514822849372L;

    private final boolean mUseVersionNumbers;

    @NotNull
    private final Serializer<T> mSerializer;
    @NotNull
    private final SerializationContext mContext;

    @Nullable
    private T mObject;

    protected BaseSerializationProxy(@NotNull Serializer<T> serializer, boolean useVersionNumbers) {
        this(SerializationContext.ALWAYS_RELEASE, serializer, useVersionNumbers);
    }

    protected BaseSerializationProxy(@NotNull SerializationContext context,
            @NotNull Serializer<T> serializer, boolean useVersionNumbers) {
        // This constructor is called for deserializing an object.
        mSerializer = serializer;
        mUseVersionNumbers = useVersionNumbers;
        mContext = context;
    }

    protected BaseSerializationProxy(@NotNull Serializer<T> serializer, @NotNull T object, boolean useVersionNumbers) {
        this(SerializationContext.ALWAYS_RELEASE, serializer, object, useVersionNumbers);
    }

    protected BaseSerializationProxy(@NotNull SerializationContext context,
            @NotNull Serializer<T> serializer, @NotNull T object,
            boolean useVersionNumbers) {
        // This constructor is called for serializing the object.
        mSerializer = serializer;
        mObject = object;
        mUseVersionNumbers = useVersionNumbers;
        mContext = context;
    }

    @Override
    public final void readExternal(@NotNull ObjectInput input)
            throws IOException, ClassNotFoundException {
        final SerializerInput serializerInput = new LegacySerializerInput(input, mUseVersionNumbers);
        mObject = mSerializer.deserializeNotNull(mContext, serializerInput);
    }

    @Override
    public final void writeExternal(@NotNull ObjectOutput output) throws IOException {
        if (mObject != null) {
            final SerializerOutput serializerOutput = new LegacySerializerOutput(output, mUseVersionNumbers);
            mSerializer.serialize(mContext, serializerOutput, mObject);
        } else {
            // This should not happen if the proxy is used correctly.
            throw new IllegalStateException("writeExternal called without an object.");
        }
    }

    @NotNull
    protected Object readResolve() {
        if (mObject != null) {
            return mObject;
        } else {
            // This should not happen if the proxy is used correctly.
            throw new IllegalStateException("readResolve called without an object.");
        }
    }
}
