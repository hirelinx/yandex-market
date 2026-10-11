package ru.yandex.practicum.market.dto;

import java.util.List;

public record ItemsAndPagingDto(List<ItemDto> items, boolean hasPrevious, boolean hasNext) {
}
