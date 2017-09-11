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

package com.twitter.serial.object;

import com.twitter.serial.util.InternalSerialUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * A base class for all object builders.

 * @param <T> the type of the object.
 */
public abstract class ObjectBuilder<T> implements Builder<T> {
    /**
     * Checks whether the builder is ready to build an object. {@link #build()} will throw an
     * exception if trying to build an object with an invalid builder. By default, it returns true.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Builds an object.
     *
     * @throws IllegalStateException if the builder is not in a valid state, as determined by
     *         calling {@link #isValid()}.
     */
    @Override
    @NotNull
    public final T build() {
        final T object = buildOrNull();
        if (object != null) {
            return object;
        } else {
            throw new IllegalStateException("The builder configuration is invalid: " + getClass().getName() +
                    InternalSerialUtils.lineSeparator() + getBuilderConfiguration());
        }
    }

    /**
     * Builds an object and returns null if the builder is invalid.
     */
    @Nullable
    public final T buildOrNull() {
        prepareForBuild();
        if (validateForBuild()) {
            return buildObject();
        } else {
            return null;
        }
    }

    /**
     * This is called right before the builder is validated and the object is built, and allows the builder
     * to set derived fields before construction. It is especially useful in a hierarchy of builders.
     */
    protected void prepareForBuild() {
    }

    /**
     * This is called when the builder is validated for object creation, and can be overridden by subclasses
     * to perform actions that need to happen only when an object is created (like exception reporting).
     */
    protected boolean validateForBuild() {
        return isValid();
    }

    @NotNull
    protected abstract T buildObject();

    @NotNull
    private String getBuilderConfiguration() {
        final StringBuilder config = new StringBuilder();
        try {
            for (Class<?> klass = getClass(); klass != ObjectBuilder.class; klass = klass.getSuperclass()) {
                for (Field field : klass.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (config.length() != 0) {
                        config.append(InternalSerialUtils.lineSeparator());
                    }
                    config.append(field.getName())
                            .append(": ")
                            .append(field.get(this));
                }
            }
        } catch (Exception ignore) {
        }
        return config.toString();
    }
}
