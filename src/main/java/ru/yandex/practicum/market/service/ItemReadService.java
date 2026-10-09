package ru.yandex.practicum.market.service;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;

public interface ItemReadService {
    Mono<ItemsAndPagingDto> search(String search, Pageable realPaging);

    Mono<ItemDto> retrieveById(long id);

    void cacheEvictOrders();
    void cacheEvictOrderById(long id);
}
