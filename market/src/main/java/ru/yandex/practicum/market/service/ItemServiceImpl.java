package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemReadServiceImpl itemReadServiceImpl;
    private final ItemWriteService itemWriteService;

    @Override
    public Mono<ItemsAndPagingDto> search(String search, Pageable pageable) {
        return itemReadServiceImpl.search(search, pageable);
    }

    @Override
    public Mono<ItemDto> retrieveById(long id) {
        return itemReadServiceImpl.retrieveById(id);
    }

    @Override
    public void cacheEvictOrders() {
        itemReadServiceImpl.cacheEvictOrders();
    }

    @Override
    public void cacheEvictOrderById(long id) {
        itemReadServiceImpl.cacheEvictOrderById(id);
    }

    @Override
    public Mono<ItemDto> create(ItemCreateRequest request, FilePart image) {
        return itemWriteService.create(request, image);
    }
}
