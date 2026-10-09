package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;

import java.util.List;

@Primary
@Service
@AllArgsConstructor
public class CartReadServiceImpl implements CartReadService {
    private final CartRepository cartRepository;
    private final ItemMapper itemMapper;
    private final CartCountedItemsRepository cartCountedItemsRepository;
    private final CountedItemRepository countedItemRepository;
    private final ItemRepository itemRepository;

    @Override
    public Flux<@NonNull CountedItem> getCountedItems(Cart cart) {
        return cartCountedItemsRepository.findByCartId(cart.getId())
                .flatMap(link -> countedItemRepository.findById(link.getCountedItemsId()));
    }

    @Override
    @Cacheable(value = "cart")
    public Mono<@NonNull Cart> retrieveCartEntity() {
        return cartRepository.findFirstBy();
    }

    @Override
    @Cacheable(value = "cartItems", key = "#cart.id")
    public Mono<List<@NonNull ItemDto>> retrieveItems(Cart cart) {

        var items = this.getCountedItems(cart);

        return items.flatMap(countedItem -> itemRepository.findById(countedItem.getItemId())
                .map(item -> itemMapper.toDto(item, countedItem.getCount()))).collectList();
    }

    @Override
    @CacheEvict(value = {"cartItems", "items", "item"}, allEntries = true)
    public void cacheEvictCartItems(){}
}
