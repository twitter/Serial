package com.serial.util.object;

import com.serial.util.StringUtils;

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
                    StringUtils.lineSeparator() + getBuilderConfiguration());
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
                        config.append(StringUtils.lineSeparator());
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
