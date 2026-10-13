package ru.yandex.practicum.market.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

@Controller
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
    OrderService orderService;

    @GetMapping
    public Mono<String> getOrders(Model model) {
        return orderService.retrieveOrders()
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .then(Mono.just("orders"));
    }

    @GetMapping("/{id}")
    public Mono<String> getOrder(@PathVariable long id, @RequestParam(defaultValue = "false") boolean newOrder, Model model) {
        return orderService.retrieveById(id).doOnNext(it -> {
                model.addAttribute("order", it);
                model.addAttribute("newOrder", newOrder);
        }).then(Mono.just("order"));
    }
}
