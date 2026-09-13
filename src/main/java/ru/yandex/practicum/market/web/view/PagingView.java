package ru.yandex.practicum.market.web.view;

public record PagingView(int pageSize, int pageNumber, boolean hasPrevious, boolean hasNext) {
}
