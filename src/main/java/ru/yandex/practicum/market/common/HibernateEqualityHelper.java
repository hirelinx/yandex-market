package ru.yandex.practicum.market.common;

import jakarta.validation.constraints.NotNull;
import org.hibernate.proxy.HibernateProxy;

/**
 * Utility class for handling equality in Hibernate entity comparisons,
 * especially when dealing with lazy-loaded proxy classes.
 *
 * @since 0.0.1-SNAPSHOT
 */
public final class HibernateEqualityHelper {

    /**
     * Returns the actual (non-proxy) class of a given Hibernate-managed object.
     *
     * @param obj  the object to inspect (must not be {@code null})
     * @return the real class of the object, unwrapped from Hibernate proxy if applicable
     */
    public static Class<?> getEffectiveClass(@NotNull Object obj) {
        return obj instanceof HibernateProxy
                ? ((HibernateProxy) obj).getHibernateLazyInitializer().getPersistentClass()
                : obj.getClass();
    }
}