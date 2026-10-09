package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.ItemRepository;

import java.util.Map;
import java.util.stream.Collectors;

@Primary
@Service
@AllArgsConstructor
public class ItemReadServiceImpl implements ItemReadService{
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final CartService cartService;

    @Override
    @Cacheable(
            value = "items",
            key = "#search + ':' + #pageable.toString().replace(' ', '_')"
    )
    public Mono<ItemsAndPagingDto> search(String search, Pageable pageable) {
        return getCountMap().flatMap(
                        countMap ->
                                itemRepository
                                        .findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search,
                                                search, pageable).map(item -> itemMapper.toDto(item,
                                                countMap.getOrDefault(item.getId(), 0L)))
                                        .collectList())
                .map(items -> new ItemsAndPagingDto(items, pageable.isPaged() && pageable.getPageNumber() > 0,
                        pageable.isPaged() && items.size() == pageable.getPageSize()));
    }

    @Override
    @Cacheable(
            value = "item",
            key = "#id"
    )
    public Mono<ItemDto> retrieveById(long id) {
        var optItem = itemRepository.findById(id);

        return optItem.flatMap(item -> getCountMap().map(cM -> Map.entry(item, cM.getOrDefault(item.getId(), 0L))))
                .map(it -> itemMapper.toDto(it.getKey(), it.getValue()))
                .switchIfEmpty(Mono.defer(() -> {
                    var itemCriteria = new Item();
                    itemCriteria.setId(id);
                    return Mono.error(new EntityNotFoundException(itemCriteria));
                }));
    }

    @Override
    @CacheEvict(value = "orders", allEntries = true)
    public void cacheEvictOrders() {}

    @Override
    @CacheEvict(value = "order", key = "#id")
    public void cacheEvictOrderById(long id) {}

    private Mono<Map<Long, Long>> getCountMap() {
        return cartService.retrieveCartEntity()
                .flatMapMany(cartService::getCountedItems)
                .collect(Collectors.toMap(CountedItem::getItemId, CountedItem::getCount));
    }
}