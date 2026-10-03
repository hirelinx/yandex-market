package ru.yandex.practicum.market.support;

import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CartCountedItems;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;

import java.math.BigDecimal;

public final class TestEntityFactory {
    private TestEntityFactory() {
    }

    public static Item item(String title, BigDecimal price) {
        var entity = new Item();
        entity.setTitle(title);
        entity.setDescription("Описание: " + title);
        entity.setImgPath("files/test.jpg");
        entity.setPrice(price);
        return entity;
    }

    public static Cart cart(long id) {
        var cart = new Cart();
        cart.setId(id);
        return cart;
    }

    public static CountedItem countedItem(long itemId, long count) {
        var countedItem = new CountedItem(itemId);
        countedItem.setCount(count);
        return countedItem;
    }

    public static CartCountedItems cartLink(long cartId, long countedItemId) {
        return new CartCountedItems(cartId, countedItemId);
    }
}
