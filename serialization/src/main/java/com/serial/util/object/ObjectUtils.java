package com.serial.util.object;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Some utilities for objects. Some of these methods have been copied from the
 * {@link java.util.Objects} class, since they are only available in Android in API 19+.
 */
public class ObjectUtils {

    private ObjectUtils() {
    }

    @Contract("!null -> !null; null -> null")
    public static <T> T cast(@Nullable Object object) {
        //noinspection unchecked
        return (T) object;
    }

    /**
     * Compares two given objects by using {@link Object#equals(Object)} and accepting null
     * references.
     */
    @Contract("null, null -> true; null, _ -> false; _, null -> false")
    public static <T> boolean equals(@Nullable T o1, @Nullable T o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }

    /**
     * Returns the hash code of an object, and 0 if it's null.
     */
    public static <T> int hashCode(@Nullable T o) {
        //noinspection SSBasedInspection
        return o != null ? o.hashCode() : 0;
    }

    /**
     * Returns the hashcode for 2 objects.
     * Use this over .hash() since it will create an implicit varargs array,
     * and using .hash() in critical paths will impact performance due to the number of arrays created.
     */
    public static <T1, T2> int hashCode(@Nullable T1 o1, @Nullable T2 o2) {
        int result = hashCode(o1);
        result = 31 * result + hashCode(o2);
        return result;
    }

    public static int hashCode(@Nullable Iterable<?> iterable) {
        if (iterable == null) {
            return 0;
        }
        int hash = 1;
        for (Object item : iterable) {
            hash = hash * 31 + hashCode(item);
        }
        return hash;
    }

    /**
     * Convenience wrapper for {@link java.util.Arrays#hashCode}, adding varargs.
     * This can be used to compute a hash code for an object's fields as follows:
     * {@code ObjectUtils.hash(a, b, c)}.
     *
     * Same as Objects.hash(Object... values), which is only available after API 19
     */
    public static int hashCode(@Nullable Object value, @NotNull Object... values) {
        return Arrays.hashCode(values) * 31 + hashCode(value);
    }

    /**
     * Returns a comparator object for the given comparable type.
     */
    @NotNull
    public static <T extends Comparable<T>> Comparator<T> getComparator() {
        return cast(NaturalComparator.INSTANCE);
    }

    /**
     * Returns an inverse comparator object for the given comparable type.
     */
    @NotNull
    public static <T extends Comparable<T>> Comparator<T> getInverseComparator() {
        return cast(InverseComparator.INSTANCE);
    }

    /**
     * Returns a comparator object that compares all elements as equal. This is useful as a comparator
     * to use with empty or singleton lists.
     */
    @NotNull
    public static <T> Comparator<T> getTrivialComparator() {
        return cast(TrivialComparator.INSTANCE);
    }

    @SuppressWarnings("BlacklistedInterface")
    private static class TrivialComparator<T> implements Comparator<T>, Serializable {
        public static final Comparator INSTANCE = new TrivialComparator();

        private static final long serialVersionUID = 116118386035401594L;

        @SuppressWarnings("ComparatorMethodParameterNotUsed")
        @Override
        public int compare(@NotNull T lhs, @NotNull T rhs) {
            return 0;
        }

        @NotNull
        protected Object readResolve() {
            return INSTANCE;
        }
    }

    @SuppressWarnings("BlacklistedInterface")
    private static class NaturalComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
        public static final Comparator INSTANCE = new NaturalComparator();

        private static final long serialVersionUID = 216992234422295118L;

        @Override
        public int compare(@NotNull T lhs, @NotNull T rhs) {
            return lhs.compareTo(rhs);
        }

        @NotNull
        protected Object readResolve() {
            return INSTANCE;
        }
    }

    @SuppressWarnings("BlacklistedInterface")
    private static class InverseComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
        public static final Comparator INSTANCE = new InverseComparator();

        private static final long serialVersionUID = 216992234422295119L;

        @Override
        public int compare(@NotNull T lhs, @NotNull T rhs) {
            return -lhs.compareTo(rhs);
        }

        @NotNull
        protected Object readResolve() {
            return INSTANCE;
        }
    }

    @SuppressWarnings("BlacklistedInterface")
    private static class ToStringComparator<T> implements Comparator<T>, Serializable {
        public static final Comparator INSTANCE = new ToStringComparator();

        private static final long serialVersionUID = 116118386035401595L;

        @Override
        public int compare(@NotNull T lhs, @NotNull T rhs) {
            return lhs.toString().compareTo(rhs.toString());
        }

        @NotNull
        protected Object readResolve() {
            return INSTANCE;
        }
    }
}
