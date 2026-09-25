package ru.yandex.practicum.market.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.model.Order;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface OrderMapper {
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalSum", source = "total")
    OrderDto toDto(Order order, BigDecimal total, List<ItemDto> items);
}

