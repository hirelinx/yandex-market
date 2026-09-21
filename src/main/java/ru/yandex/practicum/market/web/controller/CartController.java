package ru.yandex.practicum.market.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.service.ItemService;
import ru.yandex.practicum.market.web.request.PostItemsAction;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {
    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping("/items")
    public String getCartItems(Model model) {
        var itemsDto = cartService.retrieveItems();

        model.addAttribute("items", itemsDto);
        model.addAttribute("total",
                itemsDto.stream()
                        .map(it -> it.price().multiply(BigDecimal.valueOf(it.count())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return "cart";
    }

    @PostMapping("/items")
    public String getCartItem(
            @RequestParam(name = "id") long itemId,
            @RequestParam(name = "action") PostItemsAction postItemsAction,
            Model model) {
        switch (postItemsAction) {
            case PLUS -> cartService.addToCart(itemId);
            case MINUS -> cartService.removeFromCart(itemId);
        }

        var itemsDto = itemService.search("", Pageable.unpaged());

        model.addAttribute("items", itemsDto.items());
        model.addAttribute("total",
                itemsDto.items().stream()
                        .map(it -> it.price().multiply(BigDecimal.valueOf(it.count())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return "cart";
    }

}
