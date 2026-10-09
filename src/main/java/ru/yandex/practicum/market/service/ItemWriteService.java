package ru.yandex.practicum.market.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

public interface ItemWriteService {
    Mono<ItemDto> create(ItemCreateRequest request, FilePart image);
}
