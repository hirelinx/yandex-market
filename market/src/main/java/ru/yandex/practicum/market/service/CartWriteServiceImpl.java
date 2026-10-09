package ru.yandex.practicum.market.service;

import lombok.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CartCountedItems;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;

@Primary
@Service
@AllArgsConstructor
public class CartWriteServiceImpl implements CartWriteService {
    private final CartCountedItemsRepository cartCountedItemsRepository;
    private final CountedItemRepository countedItemRepository;
    private final ItemRepository itemRepository;
    @Setter
    private CartReadService cartReadService;

    @Override
    @Transactional
    public Mono<Boolean> addToCart(long itemId) {
        var cart = cartReadService.retrieveCartEntity().cache();
        var existingCartItem = cart.flatMap(it -> findInCart(it, itemId));

        var addMono = existingCartItem.flatMap(cartItem -> {
            cartItem.setCount(cartItem.getCount() + 1);
            return countedItemRepository.save(cartItem).thenReturn(false);
        }).switchIfEmpty(cart.flatMap(c -> itemRepository.findById(itemId).switchIfEmpty(Mono.defer(() -> {
            var item = new Item();
            item.setId(itemId);
            return Mono.error(new EntityNotFoundException(item));
        })).flatMap(item -> countedItemRepository.save(new CountedItem(itemId)).flatMap(
                saved -> cartCountedItemsRepository.save(new CartCountedItems(c.getId(), saved.getId()))
                        .thenReturn(true)))));

        return addMono.doOnSuccess(result -> cartReadService.cacheEvictCartItems());
    }

    @Override
    @Transactional
    public Mono<@NonNull Boolean> removeFromCart(long itemId) {
        var cart = cartReadService.retrieveCartEntity();
        var optCartItem = cart.flatMap(it -> findInCart(it, itemId));

        return optCartItem.flatMap(cartItem -> {
            if (cartItem.getCount() == 0) {
                return Mono.just(false);
            }

            cartItem.setCount(cartItem.getCount() - 1);

            if (cartItem.getCount() == 0) {

                return cartCountedItemsRepository.deleteCartCountedItemsByCountedItemsId(cartItem.getId())
                        .then(countedItemRepository.delete(cartItem)).then(Mono.just(false));
            }

            return countedItemRepository.save(cartItem).then(Mono.just(true));
        }).switchIfEmpty(Mono.just(false))
                .doOnSuccess(result -> cartReadService.cacheEvictCartItems());
    }

    private Mono<CountedItem> findInCart(Cart cart, long itemId) {
        return cartReadService.getCountedItems(cart)
                .filter(cartItem -> cartItem.getItemId() != null && cartItem.getItemId() == itemId)
                .next();
    }
}
