package ru.yandex.practicum.market.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

public interface ItemService {
    ItemsAndPagingDto search(String search, Pageable realPaging);

    ItemDto retrieveById(long id);

    ItemDto create(ItemCreateRequest request, MultipartFile image);
}
