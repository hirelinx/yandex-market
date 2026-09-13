package ru.yandex.practicum.market.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

import java.util.Map;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class RootController {
    private final OrderService orderService;


    @PostMapping("/buy")
    public String buy() {
        OrderDto order = orderService.createFromCart();

        return "redirect:/orders/%s?newOrder=true".formatted(order.id());
    }

    @GetMapping
    public String root(@RequestParam Map<String, String> allParams,
                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addAllAttributes(allParams);

        return "redirect:items";
    }
}
