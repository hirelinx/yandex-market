package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
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

@Primary
@Service
@AllArgsConstructor
public class OrderReadServiceImpl implements OrderReadService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final OrdersItemsRepository ordersItemsRepository;
    private final CountedItemRepository countedItemRepository;
    private final ItemRepository itemRepository;

    @Override
    @Cacheable(
            value = "orders",
            cacheManager = "shortTtlCacheManager"
    )
    public Flux<OrderDto> retrieveOrders() {
        return orderRepository.findAll().flatMap(
                order -> ordersItemsRepository.findByOrderId(order.getId()).map(OrdersItems::getItemsId).collectList()
                        .flatMap(orderItemIds -> Mono.zip(orderTotalSum(orderItemIds), orderItems(orderItemIds))
                                .map(tuple -> orderMapper.toDto(order, tuple.getT1(), tuple.getT2()))));
    }

    @Override
    @Cacheable(
            value = "order",
            key = "#id"
    )
    public Mono<OrderDto> retrieveById(long id) {
        return orderRepository.findById(id).flatMap(
                        order -> ordersItemsRepository.findByOrderId(order.getId()).map(OrdersItems::getItemsId).collectList()
                                .flatMap(orderItemIds -> Mono.zip(orderTotalSum(orderItemIds), orderItems(orderItemIds))
                                        .map(tuple -> orderMapper.toDto(order, tuple.getT1(), tuple.getT2()))))
                .switchIfEmpty(Mono.defer(() -> {
                    var criteria = new Order();
                    criteria.setId(id);
                    return Mono.error(new EntityNotFoundException(criteria));
                }));
    }

    @Override
    @CacheEvict(value = "orders", allEntries = true)
    public void cacheEvictOrders() {}

    @CacheEvict(value = "order", key = "#id")
    @Override
    public void cacheEvictOrderById(long id) {}

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
