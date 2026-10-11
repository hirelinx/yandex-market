package ru.yandex.practicum.market.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "count", source = "count")
    ItemDto toDto(Item item, Long count);
}
