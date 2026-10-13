package ru.yandex.practicum.market.exception;

import lombok.Getter;
import lombok.ToString;
import ru.yandex.practicum.market.model.IEntity;

import java.util.Map;

@Getter
@ToString
public class EntityNotFoundException extends RuntimeException {
    IEntity entity;
    Class<? extends IEntity> entityClass;
    Map<String, Object> searchedBy;
    NotFoundBy notFoundBy;

    public EntityNotFoundException(IEntity entity, Map<String, Object> searchedBy) {
        this.entity = entity;
        this.searchedBy = searchedBy;
        this.entityClass = entity.getClass();
        this.notFoundBy = NotFoundBy.BOTH;
    }

    public EntityNotFoundException(IEntity entity) {
        this.entity = entity;
        this.entityClass = entity.getClass();
        this.notFoundBy = NotFoundBy.CRITERIA;
    }

    public EntityNotFoundException(Class<? extends IEntity> entityClass, Map<String, Object> searchedBy) {
        this.entityClass = entityClass;
        this.searchedBy = searchedBy;
        this.notFoundBy = NotFoundBy.MAP;
    }
}
