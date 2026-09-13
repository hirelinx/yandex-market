package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ItemMapper itemMapper;
    CountedItemRepository countedItemRepository;
    ItemRepository itemRepository;

    @Override
    public boolean addToCart(long itemId) {
        var cart = retrieveCartEntity();
        var optCartItem = findInCart(cart, itemId);

        return optCartItem.map(cartItem -> {
            cartItem.setCount(cartItem.getCount() + 1);
            countedItemRepository.save(cartItem);
            return false;
        }).orElseGet(() -> {
            var optItem = itemRepository.findById(itemId);
            if (optItem.isEmpty()) {
                var itemCriteria = new Item();
                itemCriteria.setId(itemId);
                throw new EntityNotFoundException(itemCriteria);
            }

            var cartItem = new CountedItem(optItem.get());
            cart.getCountedItems().add(cartItem);
            countedItemRepository.save(cartItem);
            cartRepository.save(cart);

            return true;
        });
    }

    @Override
    public boolean removeFromCart(long itemId) {
        var cart = retrieveCartEntity();
        var optCartItem = findInCart(cart, itemId);

        return optCartItem.map(cartItem -> {
            if (cartItem.getCount() == 0) {
                return false;
            }

            cartItem.setCount(cartItem.getCount() - 1);

            if (cartItem.getCount() == 0) {
                cart.getCountedItems().remove(cartItem);
                cartRepository.save(cart);
                countedItemRepository.delete(cartItem);
                return false;
            }

            countedItemRepository.save(cartItem);
            return true;
        }).orElse(false);
    }

    private static java.util.Optional<CountedItem> findInCart(Cart cart, long itemId) {
        return cart.getCountedItems().stream()
                .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                .findFirst();
    }

    @Override
    public Cart retrieveCartEntity() {
        return cartRepository.findAll().getFirst();
    }

    @Override
    public List<ItemDto> retrieveItems() {
        var cart = retrieveCartEntity();

        return cart.getCountedItems().stream().map(itemMapper::toDto).toList();
    }
}
