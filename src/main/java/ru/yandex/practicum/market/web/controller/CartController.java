package ru.yandex.practicum.market.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.web.request.PostCartItemRequest;

import java.math.BigDecimal;

import static ru.yandex.practicum.market.web.request.PostItemsAction.MINUS;
import static ru.yandex.practicum.market.web.request.PostItemsAction.PLUS;

@Controller
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/items")
    public Mono<String> getCartItems(Model model) {
        var itemsDto = cartService.retrieveItems();

        model.addAttribute("items", itemsDto);
        return itemsDto.map(it -> it.price().multiply(BigDecimal.valueOf(it.count())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).map(it ->
                        model.addAttribute("total",
                                    it
                                )
                ).thenReturn("cart");
    }

    @PostMapping("/items")
    public Mono<String> getCartItem(
            @Valid @ModelAttribute PostCartItemRequest postCartItemRequest,
            Model model) {
        var mono = switch (postCartItemRequest.action()) {
            case PLUS -> cartService.addToCart(postCartItemRequest.id());
            case MINUS -> cartService.removeFromCart(postCartItemRequest.id());
        };

        var itemsMono = mono.thenMany(cartService.retrieveItems()).collectList();

        return itemsMono.map(items -> {
            var total = items.stream()
                .map(it -> it.price().multiply(BigDecimal.valueOf(it.count())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("items", items);
            model.addAttribute("total", total);
            return "cart";
        });
    }

}
