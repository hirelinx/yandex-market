package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CartCountedItems;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ItemMapper itemMapper;
    private final CartCountedItemsRepository cartCountedItemsRepository;
    private final CountedItemRepository countedItemRepository;
    private final ItemRepository itemRepository;

    @Override
    public Mono<Boolean> addToCart(long itemId) {
        var cart = retrieveCartEntity();
        var existingCartItem = cart.flatMap(it -> findInCart(it, itemId));

        return existingCartItem
                .flatMap(cartItem -> {
                    cartItem.setCount(cartItem.getCount() + 1);
                    return countedItemRepository.save(cartItem).thenReturn(false);
                })
                .switchIfEmpty(
                        cart.flatMap(c ->
                                itemRepository.findById(itemId)
                                        .switchIfEmpty(Mono.defer(() -> {
                                            var item = new Item();
                                            item.setId(itemId);
                                            return Mono.error(new EntityNotFoundException(item));
                                        }))
                                        .flatMap(item -> countedItemRepository.save(new CountedItem(itemId))
                                                .flatMap(saved -> cartCountedItemsRepository.save(
                                                        new CartCountedItems(c.getId(), saved.getId())
                                                ).thenReturn(true)))
                        )
                );
    }

    @Override
    public Mono<@NonNull Boolean> removeFromCart(long itemId) {
        var cart = retrieveCartEntity();
        var optCartItem = cart.flatMap(it -> findInCart(it, itemId));

        return optCartItem.flatMap(cartItem -> {
            if (cartItem.getCount() == 0) {
                return Mono.just(false);
            }

            cartItem.setCount(cartItem.getCount() - 1);

            if (cartItem.getCount() == 0) {

                return cartCountedItemsRepository.deleteCartCountedItemsByCountedItemsId(cartItem.getId())
                        .then(countedItemRepository.delete(cartItem))
                        .then(Mono.just(false));
            }

            return countedItemRepository.save(cartItem).then(Mono.just(true));
        }).switchIfEmpty(Mono.just(false));
    }

    private Mono<CountedItem> findInCart(Cart cart, long itemId) {
        return getCountedItems(cart)
                .filter(cartItem -> cartItem.getItemId() != null && cartItem.getItemId() == itemId)
                .next();
    }

    @Override
    public Flux<@NonNull CountedItem> getCountedItems(Cart cart) {
        return cartCountedItemsRepository.findByCartId(cart.getId())
                .flatMap(link -> countedItemRepository.findById(link.getCountedItemsId()));
    }

    @Override
    public Mono<@NonNull Cart> retrieveCartEntity() {
        return cartRepository.findFirstBy();
    }

    @Override
    public Flux<@NonNull ItemDto> retrieveItems() {
        var cart = retrieveCartEntity();

        var items = cart.flatMapMany(this::getCountedItems);

        return items.flatMap(countedItem -> itemRepository.findById(countedItem.getItemId())
                .map(item -> itemMapper.toDto(item, countedItem.getCount())));
    }
}
