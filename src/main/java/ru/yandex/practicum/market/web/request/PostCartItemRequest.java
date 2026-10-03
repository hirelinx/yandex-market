package ru.yandex.practicum.market.web.request;

public record PostCartItemRequest(
        long id,
        PostItemsAction action
) {
}
