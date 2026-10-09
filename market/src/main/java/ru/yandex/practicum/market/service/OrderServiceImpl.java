package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.mapper.OrderMapper;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.model.OrdersItems;
import ru.yandex.practicum.market.repository.*;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderReadService orderReadService;
    private final OrderWriteService orderWriteService;

    @Override
    public Flux<OrderDto> retrieveOrders() {
        return orderReadService.retrieveOrders();
    }

    @Override
    public Mono<OrderDto> retrieveById(long id) {
        return orderReadService.retrieveById(id);
    }

    @Override
    public void cacheEvictOrders() {
        orderReadService.cacheEvictOrders();
    }

    @Override
    public void cacheEvictOrderById(long id) {
        orderReadService.cacheEvictOrderById(id);
    }

    @Override
    public Mono<OrderDto> createFromCart() {
        return orderWriteService.createFromCart();
    }
}
