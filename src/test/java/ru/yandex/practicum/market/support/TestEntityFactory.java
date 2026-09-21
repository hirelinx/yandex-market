package ru.yandex.practicum.market.support;

import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;

import java.math.BigDecimal;
import java.util.HashSet;

public final class TestEntityFactory {
    private TestEntityFactory() {
    }

    public static Item item(String title, BigDecimal price) {
        var item = new Item();
        item.setTitle(title);
        item.setDescription("Описание: " + title);
        item.setImgPath("files/test.jpg");
        item.setPrice(price);
        return item;
    }

    public static Cart emptyCart() {
        var cart = new Cart();
        cart.setCountedItems(new HashSet<>());
        return cart;
    }

    public static CountedItem countedItem(Item item, long count) {
        var countedItem = new CountedItem(item);
        countedItem.setCount(count);
        return countedItem;
    }
}
