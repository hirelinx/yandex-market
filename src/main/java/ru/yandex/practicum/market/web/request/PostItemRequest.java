package ru.yandex.practicum.market.web.request;

public record PostItemRequest(
        long id,
        String search,
        ItemSorting sort,
        int pageSize,
        int pageNumber,
        PostItemsAction action
) {
}
