package ru.yandex.practicum.market.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.model.Order;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface OrderMapper {
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalSum", expression = "java(calculateTotalSum(order))")
    OrderDto toDto(Order order);

    default BigDecimal calculateTotalSum(Order order) {
        return order.getItems().stream()
                .map(item -> item.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

