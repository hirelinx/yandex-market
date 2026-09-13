package ru.yandex.practicum.market.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "count", source = "count")
    ItemDto toDto(Item item, Long count);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "title", source = "item.title")
    @Mapping(target = "description", source = "item.description")
    @Mapping(target = "imgPath", source = "item.imgPath")
    @Mapping(target = "price", source = "item.price")
    @Mapping(target = "count", source = "count")
    ItemDto toDto(CountedItem countedItem);
}
