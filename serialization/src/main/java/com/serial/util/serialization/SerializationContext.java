package com.serial.util.serialization;

/**
 * Provides context about serialization and deserialization environment.
 * <p>
 * Could be configured to debug or release mode which are expected to be mutually exclusive.
 */
public interface SerializationContext {

    /**
     * Default serialization context implementation.
     * Always returns true for {@link SerializationContext#isRelease()}.
     */
    SerializationContext ALWAYS_RELEASE = new SerializationContext() {
        @Override
        public boolean isDebug() {
            return false;
        }

        @Override
        public boolean isRelease() {
            return true;
        }
    };

    /**
     * @return true - if serialization and deserialization should be done for development environment, false - if not.
     */
    boolean isDebug();

    /**
     * @return true - if serialization and deserialization should be done for release environment, false - if not.
     */
    boolean isRelease();
}
