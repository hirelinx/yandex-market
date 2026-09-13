package ru.yandex.practicum.market.common;

import java.io.Serializable;
import java.util.Objects;

import static ru.yandex.practicum.market.common.HibernateEqualityHelper.getEffectiveClass;

/**
 * Abstract base class for entities implementing {@link Identifiable} with correct
 * {@code equals()} and {@code hashCode()} implementations that support Hibernate proxies.
 *
 * @param <ID>  the type of the identifier
 * @since 0.0.1-SNAPSHOT
 */
public abstract class HibernateIdentifiable<ID extends Serializable> implements Identifiable<ID> {

    /**
     * Compares entities based on their effective class and identity.
     * Supports comparison even when Hibernate proxies are involved.
     *
     * @param o  the object to compare to
     * @return {@code true} if both objects represent the same entity, otherwise {@code false}
     */
    @SuppressWarnings({"EqualsDoesntCheckParameterClass", "unchecked"})
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = getEffectiveClass(o);
        Class<?> thisEffectiveClass = getEffectiveClass(this);
        if (oEffectiveClass != thisEffectiveClass) return false;

        Identifiable<ID> other = (Identifiable<ID>) o;
        return getIdentity() != null && Objects.equals(getIdentity(), other.getIdentity());
    }

    /**
     * Returns the hash code based on the identity.
     *
     * @return the hash code of the identifier
     */
    @Override
    public final int hashCode() {
        return Objects.hashCode(getIdentity());
    }
}