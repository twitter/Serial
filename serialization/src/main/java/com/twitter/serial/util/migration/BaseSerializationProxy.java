/*
 * Copyright 2017 Twitter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.serial.util.migration;

import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.stream.legacy.LegacySerializerInput;
import com.twitter.serial.stream.legacy.LegacySerializerOutput;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.serializer.SerializationContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A base class for the serialization proxy of a model object that doesn't have a builder.
 */
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
