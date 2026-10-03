package ru.yandex.practicum.market.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

public interface ItemService {
    Mono<ItemsAndPagingDto> search(String search, Pageable realPaging);

    Mono<ItemDto> retrieveById(long id);

    Mono<ItemDto> create(ItemCreateRequest request, FilePart image);
}
