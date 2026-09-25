package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final FilesService filesService;
    private final CartService cartService;

    @Override
    public Mono<ItemsAndPagingDto> search(String search, Pageable pageable) {
        return getCountMap()
                .flatMap(countMap ->
                        itemRepository
                                .findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                        search,
                                        search,
                                        pageable
                                )
                                .map(item -> itemMapper.toDto(
                                        item,
                                        countMap.getOrDefault(item.getId(), 0L)
                                ))
                                .collectList()
                )
                .map(items -> new ItemsAndPagingDto(
                        items,
                        pageable.isPaged() && pageable.getPageNumber() > 0,
                        pageable.isPaged() && items.size() == pageable.getPageSize()
                ));
    }

    @Override
    public Mono<ItemDto> retrieveById(long id) {
        var optItem = itemRepository.findById(id);

        return optItem.flatMap(item ->
                getCountMap().map(cM ->
                        Map.entry(item, cM.getOrDefault(item.getId(), 0L))
                )
        ).map(it -> itemMapper.toDto(it.getKey(), it.getValue())
        ).switchIfEmpty(Mono.defer(() -> {
            var itemCriteria = new Item();
            itemCriteria.setId(id);
            return Mono.error(new EntityNotFoundException(itemCriteria));
        }));
    }

    private Mono<Map<Long, Long>> getCountMap() {
        return cartService.retrieveCartEntity()
                .flatMapMany(cartService::getCountedItems)
                .collect(Collectors.toMap(CountedItem::getItemId, CountedItem::getCount));
    }

    @Override
    public Mono<ItemDto> create(ItemCreateRequest request, FilePart image) {
        var extension = StringUtils.getFilenameExtension(image.filename());
        var newFileName = UUID.randomUUID() + (extension != null ? "." + extension : "");
        return filesService.upload(image, newFileName).then(Mono.defer(() -> {
            var item = new Item();
            item.setTitle(request.getTitle());
            item.setDescription(request.getDescription());
            item.setImgPath("files/" + newFileName);
            item.setPrice(request.getPrice());
            return itemRepository.save(item).map(it -> itemMapper.toDto(it, 0L));
        }));
    }
}
