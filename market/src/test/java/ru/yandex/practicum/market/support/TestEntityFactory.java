package ru.yandex.practicum.market.support;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ru.yandex.practicum.market.model.*;

import java.math.BigDecimal;

public final class TestEntityFactory {
    private TestEntityFactory() {
    }

    public static User user(long id, String username, String passwordHash) {
        var entity = new User();
        entity.setId(id);
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        return entity;
    }

    public static Item item(String title, BigDecimal price) {
        var entity = new Item();
        entity.setTitle(title);
        entity.setDescription("Описание: " + title);
        entity.setImgPath("files/test.jpg");
        entity.setPrice(price);
        return entity;
    }

    public static Cart cart(long id, long userId) {
        var cart = new Cart();
        cart.setId(id);
        cart.setUserId(userId);
        return cart;
    }

    public static Order order(long id, long userId) {
        var order = new Order();
        order.setId(id);
        order.setUserId(userId);
        return order;
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
