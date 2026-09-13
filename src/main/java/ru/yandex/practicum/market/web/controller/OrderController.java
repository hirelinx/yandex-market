package ru.yandex.practicum.market.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
    OrderService orderService;

    @GetMapping
    public String getOrders(Model model) {
        List<OrderDto> orderDtos = orderService.retrieveOrders();

        model.addAttribute("orders", orderDtos);

        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrder(@PathVariable long id, @RequestParam(defaultValue = "false") boolean newOrder, Model model) {
        OrderDto orderDto = orderService.retrieveById(id);

        model.addAttribute("order", orderDto);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
