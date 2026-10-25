package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.mapper.OrderMapper;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.model.OrdersItems;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.repository.OrderRepository;
import ru.yandex.practicum.market.repository.OrdersItemsRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
@AllArgsConstructor
public class UserOrdersProvider {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final OrdersItemsRepository ordersItemsRepository;
    private final CountedItemRepository countedItemRepository;
    private final ItemRepository itemRepository;

    @Cacheable(
            value = "orders",
            key = "#userId",
            cacheManager = "shortTtlCacheManager"
    )
    public Flux<OrderDto> retrieveOrders(long userId) {
        return orderRepository.findAllByUserId(userId).flatMap(this::toDto);
    }

    @Cacheable(
            value = "order",
            key = "#userId + ':' + #id",
            unless = "#result == null"
    )
    public Mono<OrderDto> retrieveById(long userId, long id) {
        return orderRepository.findByIdAndUserId(id, userId).flatMap(this::toDto);
    }

    private Mono<OrderDto> toDto(Order order) {
        return ordersItemsRepository.findByOrderId(order.getId()).map(OrdersItems::getItemsId).collectList()
                .flatMap(orderItemIds -> Mono.zip(orderTotalSum(orderItemIds), orderItems(orderItemIds))
                        .map(tuple -> orderMapper.toDto(order, tuple.getT1(), tuple.getT2())));
    }

    private Mono<List<ItemDto>> orderItems(List<Long> countedItemIds) {
        if (countedItemIds.isEmpty()) {
            return Mono.just(List.of());
        }
        return countedItemRepository.findAllById(Flux.fromIterable(countedItemIds))
                .flatMap(countedItem -> itemRepository.findById(countedItem.getItemId())
                        .map(item -> itemMapper.toDto(item, countedItem.getCount()))).collectList();
    }

    private Mono<BigDecimal> orderTotalSum(List<Long> countedItemIds) {
        if (countedItemIds.isEmpty()) {
            return Mono.just(BigDecimal.ZERO);
        }
        return countedItemRepository.findAllById(Flux.fromIterable(countedItemIds)).flatMap(
                        countedItem -> itemRepository.findById(countedItem.getItemId())
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(countedItem.getCount()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add).defaultIfEmpty(BigDecimal.ZERO);
    }
}
