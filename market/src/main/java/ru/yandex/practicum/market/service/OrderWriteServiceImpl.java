package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.model.OrdersItems;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.OrderRepository;
import ru.yandex.practicum.market.repository.OrdersItemsRepository;

@Primary
@Service
@AllArgsConstructor
public class OrderWriteServiceImpl implements OrderWriteService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrdersItemsRepository ordersItemsRepository;
    private final CartCountedItemsRepository cartCountedItemsRepository;
    @Setter
    private OrderReadService orderReadService;

    @Override
    @Transactional
    public Mono<OrderDto> createFromCart() {
        return cartService.retrieveCartEntity().flatMap(cart -> cartService.getCountedItems(cart).collectList().flatMap(
                countedItems -> orderRepository.save(new Order()).flatMap(savedOrder ->
                        ordersItemsRepository.saveAll(
                                        Flux.fromIterable(
                                                countedItems.stream().map(ci -> new OrdersItems(savedOrder.getId(), ci.getId()))
                                                        .toList())).then(cartCountedItemsRepository.deleteAll())
                                .doOnSuccess((it) -> {orderReadService.cacheEvictOrderById(savedOrder.getId());})
                                .doOnSuccess((it) -> {orderReadService.cacheEvictOrders();})
                                .doOnSuccess((it) -> cartService.cacheEvictCartItems())
                                .then(orderReadService.retrieveById(savedOrder.getId())))));
    }
}
