package ru.yandex.practicum.market.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.common.CartTotals;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.service.PaymentService;
import ru.yandex.practicum.market.web.request.PostCartItemRequest;

import java.util.List;

import static ru.yandex.practicum.market.web.request.PostItemsAction.DELETE;
import static ru.yandex.practicum.market.web.request.PostItemsAction.MINUS;
import static ru.yandex.practicum.market.web.request.PostItemsAction.PLUS;

@Controller
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {
    private final CartService cartService;
    private final PaymentService paymentService;

    @GetMapping("/items")
    public Mono<String> getCartItems(
            @RequestParam(required = false) String paymentError,
            Model model) {
        applyPaymentErrorMessage(paymentError, model);
        return renderCart(model);
    }

    @PostMapping("/items")
    public Mono<String> getCartItem(
            @Valid @ModelAttribute PostCartItemRequest postCartItemRequest,
            Model model) {
        var mono = switch (postCartItemRequest.action()) {
            case PLUS -> cartService.addToCart(postCartItemRequest.id());
            case MINUS -> cartService.removeFromCart(postCartItemRequest.id());
            case DELETE -> cartService.deleteFromCart(postCartItemRequest.id());
        };

        return mono.then(Mono.defer(() -> renderCart(model)));
    }

    private Mono<String> renderCart(Model model) {
        return cartService.retrieveCartEntity().flatMap(cart ->
                cartService.retrieveItems(cart).flatMap(items -> enrichCartModel(model, items)));
    }

    private Mono<String> enrichCartModel(Model model, List<ItemDto> items) {
        var total = CartTotals.totalOf(items);
        model.addAttribute("items", items);
        model.addAttribute("total", total);

        if (items.isEmpty()) {
            model.addAttribute("canCheckout", false);
            return Mono.just("cart");
        }

        return paymentService.getCheckoutInfo(total)
                .doOnNext(checkout -> {
                    model.addAttribute("canCheckout", checkout.canCheckout());
                    model.addAttribute("balance", checkout.balance());
                    if (checkout.message() != null && model.getAttribute("checkoutMessage") == null) {
                        model.addAttribute("checkoutMessage", checkout.message());
                    }
                })
                .thenReturn("cart");
    }

    private void applyPaymentErrorMessage(String paymentError, Model model) {
        if ("insufficient".equals(paymentError)) {
            model.addAttribute("checkoutMessage", "Оплата не прошла: недостаточно средств на счёте");
        } else if ("unavailable".equals(paymentError)) {
            model.addAttribute("checkoutMessage", "Сервис платежей недоступен");
        }
    }
}
