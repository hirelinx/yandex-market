package ru.yandex.practicum.market.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Interface for domain objects that can be uniquely identified by an ID.
 *
 * @param <ID>  the type of the identifier (e.g. {@link java.util.UUID}, {@link Long})
 * @since 0.0.1-SNAPSHOT
 */
public interface Identifiable<ID extends Serializable> {

    /**
     * Returns the identity of the object (typically a database ID or UUID).
     *
     * @return unique identifier of the object
     */
    ID getIdentity();
}