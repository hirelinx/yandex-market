package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.mapper.OrderMapper;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.model.OrdersItems;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.*;
import ru.yandex.practicum.market.support.TestEntityFactory;

import org.reactivestreams.Publisher;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private OrdersItemsRepository ordersItemsRepository;
    @Mock
    private CartCountedItemsRepository cartCountedItemsRepository;
    @Mock
    private CountedItemRepository countedItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void retrieveOrders_mapsAllOrders() {
        var order = new Order();
        order.setId(1L);
        var dto = new OrderDto(1L, List.of(), BigDecimal.ZERO);

        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(ordersItemsRepository.findByOrderId(1L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(any(Order.class), any(BigDecimal.class), any())).thenReturn(dto);

        StepVerifier.create(orderService.retrieveOrders())
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void retrieveById_existingOrder_returnsDto() {
        var order = new Order();
        order.setId(2L);
        var dto = new OrderDto(2L, List.of(), BigDecimal.TEN);
        when(orderRepository.findById(2L)).thenReturn(Mono.just(order));
        when(ordersItemsRepository.findByOrderId(2L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(any(Order.class), any(BigDecimal.class), any())).thenReturn(dto);

        StepVerifier.create(orderService.retrieveById(2L))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void retrieveById_calculatesTotalAsPriceTimesCount() {
        var order = new Order();
        order.setId(2L);
        var countedItem = TestEntityFactory.countedItem(5L, 3L);
        countedItem.setId(100L);
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(5L);
        var expectedTotal = BigDecimal.valueOf(30);

        when(orderRepository.findById(2L)).thenReturn(Mono.just(order));
        when(ordersItemsRepository.findByOrderId(2L))
                .thenReturn(Flux.just(new OrdersItems(2L, 100L)));
        when(countedItemRepository.findAllById(any(Publisher.class))).thenReturn(Flux.just(countedItem));
        when(itemRepository.findById(5L)).thenReturn(Mono.just(item));
        when(itemMapper.toDto(item, 3L)).thenReturn(new ItemDto(5L, "Товар", "desc", "files/test.jpg", BigDecimal.TEN, 3));
        when(orderMapper.toDto(eq(order), eq(expectedTotal), any()))
                .thenReturn(new OrderDto(2L, List.of(), expectedTotal));

        StepVerifier.create(orderService.retrieveById(2L))
                .expectNextMatches(dto -> dto.totalSum().compareTo(expectedTotal) == 0)
                .verifyComplete();
    }

    @Test
    void retrieveById_missingOrder_emitsEntityNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.retrieveById(99L))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    void createFromCart_createsOrderAndClearsCartLinks() {
        var cart = TestEntityFactory.cart(1L);
        var countedItem = TestEntityFactory.countedItem(1L, 1L);
        countedItem.setId(10L);
        var savedOrder = new Order();
        savedOrder.setId(3L);
        var dto = new OrderDto(3L, List.of(), BigDecimal.ZERO);

        when(cartService.retrieveCartEntity()).thenReturn(Mono.just(cart));
        when(cartService.getCountedItems(cart)).thenReturn(Flux.just(countedItem));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(ordersItemsRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(new OrdersItems(3L, 10L)));
        when(cartCountedItemsRepository.deleteAll()).thenReturn(Mono.empty());
        when(orderRepository.findById(3L)).thenReturn(Mono.just(savedOrder));
        when(ordersItemsRepository.findByOrderId(3L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(any(Order.class), any(BigDecimal.class), any())).thenReturn(dto);

        StepVerifier.create(orderService.createFromCart())
                .expectNext(dto)
                .verifyComplete();
    }
}
