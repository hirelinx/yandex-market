package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
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

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final CartRepository cartRepository;
    private final OrdersItemsRepository ordersItemsRepository;
    private final CartCountedItemsRepository cartCountedItemsRepository;
    private final CountedItemRepository countedItemRepository;
    private final ItemRepository itemRepository;

    @Override
    public Flux<OrderDto> retrieveOrders() {
        return orderRepository.findAll()
                .flatMap(order -> ordersItemsRepository.findByOrderId(order.getId())
                        .map(OrdersItems::getItemsId)
                        .collectList()
                        .flatMap(orderItemIds -> Mono.zip(
                                        orderTotalSum(orderItemIds),
                                        orderItems(orderItemIds))
                                .map(tuple -> orderMapper.toDto(order, tuple.getT1(), tuple.getT2()))));
    }

    @Override
    public Mono<OrderDto> retrieveById(long id) {
        return orderRepository.findById(id)
                .flatMap(order -> ordersItemsRepository.findByOrderId(order.getId())
                        .map(OrdersItems::getItemsId)
                        .collectList()
                        .flatMap(orderItemIds -> Mono.zip(
                                        orderTotalSum(orderItemIds),
                                        orderItems(orderItemIds))
                                .map(tuple -> orderMapper.toDto(order, tuple.getT1(), tuple.getT2()))))
                .switchIfEmpty(Mono.defer(() -> {
                    var criteria = new Order();
                    criteria.setId(id);
                    return Mono.error(new EntityNotFoundException(criteria));
                }));
    }

    @Override
    public Mono<OrderDto> createFromCart() {
        return cartService.retrieveCartEntity()
                .flatMap(cart -> cartService.getCountedItems(cart).collectList()
                        .flatMap(countedItems -> orderRepository.save(new Order())
                                .flatMap(savedOrder -> ordersItemsRepository.saveAll(Flux.fromIterable(
                                        countedItems.stream()
                                                .map(ci -> new OrdersItems(savedOrder.getId(), ci.getId()))
                                                .toList()
                                )).then(cartCountedItemsRepository.deleteAll())
                                        .then(retrieveById(savedOrder.getId())))));
    }

    private Mono<List<ItemDto>> orderItems(List<Long> countedItemIds) {
        if (countedItemIds.isEmpty()) {
            return Mono.just(List.of());
        }
        return countedItemRepository.findAllById(Flux.fromIterable(countedItemIds))
                .flatMap(countedItem -> itemRepository.findById(countedItem.getItemId())
                        .map(item -> itemMapper.toDto(item, countedItem.getCount())))
                .collectList();
    }

    private Mono<BigDecimal> orderTotalSum(List<Long> countedItemIds) {
        if (countedItemIds.isEmpty()) {
            return Mono.just(BigDecimal.ZERO);
        }
        return countedItemRepository.findAllById(Flux.fromIterable(countedItemIds))
                .flatMap(countedItem -> itemRepository.findById(countedItem.getItemId())
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(countedItem.getCount()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
