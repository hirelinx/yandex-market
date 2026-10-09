package ru.yandex.practicum.market.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

import java.net.URI;
import java.util.Map;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class RootController {
    private final OrderService orderService;


    @PostMapping("/buy")
    public Mono<String> buy() {
        var order = orderService.createFromCart();

        return order.map(it -> "redirect:/orders/%s?newOrder=true".formatted(it.id()));
    }

    @GetMapping
    public Mono<Void> root(
            @RequestParam Map<String, String> allParams,
            ServerHttpResponse response) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/items");

        allParams.forEach(builder::queryParam);

        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(
                URI.create(builder.build().encode().toUriString())
        );

        return Mono.empty();
    }
}
