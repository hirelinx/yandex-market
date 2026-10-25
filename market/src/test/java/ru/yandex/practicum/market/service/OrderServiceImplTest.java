package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.mapper.OrderMapper;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.model.OrdersItems;
import ru.yandex.practicum.market.model.User;
import ru.yandex.practicum.market.repository.*;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private UserService userService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private OrdersItemsRepository ordersItemsRepository;
    @Mock
    private CartCountedItemsRepository cartCountedItemsRepository;
    @Mock
    private CountedItemRepository countedItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PaymentService paymentService;
    @InjectMocks
    private OrderWriteServiceImpl orderWriteService;
    private OrderServiceImpl orderService;

    private final User currentUser = TestEntityFactory.user(7L, "user", "neverlogin");

    @BeforeEach
    void setUp() {
        var userOrdersProvider = new UserOrdersProvider(
                orderRepository,
                orderMapper,
                itemMapper,
                ordersItemsRepository,
                countedItemRepository,
                itemRepository
        );
        var orderReadService = new OrderReadServiceImpl(userService, userOrdersProvider);
        orderWriteService.setOrderReadService(orderReadService);

        orderService = new OrderServiceImpl(orderReadService, orderWriteService);
    }

    @Test
    void retrieveOrders_returnsOnlyCurrentUserOrders() {
        var order = TestEntityFactory.order(1L, currentUser.getId());
        var dto = new OrderDto(1L, List.of(), BigDecimal.ZERO);

        when(userService.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(orderRepository.findAllByUserId(currentUser.getId())).thenReturn(Flux.just(order));
        when(ordersItemsRepository.findByOrderId(1L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(any(Order.class), any(BigDecimal.class), any())).thenReturn(dto);

        StepVerifier.create(orderService.retrieveOrders())
                .expectNext(dto)
                .verifyComplete();

        verify(orderRepository, never()).findAll();
    }

    @Test
    void retrieveOrders_withoutCurrentUser_returnsEmpty() {
        when(userService.getCurrentUser()).thenReturn(Mono.empty());

        StepVerifier.create(orderService.retrieveOrders())
                .verifyComplete();
    }

    @Test
    void retrieveById_existingOrder_returnsDto() {
        var order = TestEntityFactory.order(2L, currentUser.getId());
        var dto = new OrderDto(2L, List.of(), BigDecimal.TEN);

        when(userService.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(orderRepository.findByIdAndUserId(2L, currentUser.getId())).thenReturn(Mono.just(order));
        when(ordersItemsRepository.findByOrderId(2L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(any(Order.class), any(BigDecimal.class), any())).thenReturn(dto);

        StepVerifier.create(orderService.retrieveById(2L))
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void retrieveById_calculatesTotalAsPriceTimesCount() {
        var order = TestEntityFactory.order(2L, currentUser.getId());
        var countedItem = TestEntityFactory.countedItem(5L, 3L);
        countedItem.setId(100L);
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(5L);
        var expectedTotal = BigDecimal.valueOf(30);

        when(userService.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(orderRepository.findByIdAndUserId(2L, currentUser.getId())).thenReturn(Mono.just(order));
        when(ordersItemsRepository.findByOrderId(2L))
                .thenReturn(Flux.just(new OrdersItems(2L, 100L)));
        when(countedItemRepository.findAllById(any(Publisher.class))).thenReturn(Flux.just(countedItem));
        when(itemRepository.findById(5L)).thenReturn(Mono.just(item));
        when(itemMapper.toDto(item, 3L)).thenReturn(
                new ItemDto(5L, "Товар", "desc", "files/test.jpg", BigDecimal.TEN, 3));
        when(orderMapper.toDto(eq(order), eq(expectedTotal), any()))
                .thenReturn(new OrderDto(2L, List.of(), expectedTotal));

        StepVerifier.create(orderService.retrieveById(2L))
                .expectNextMatches(dto -> dto.totalSum().compareTo(expectedTotal) == 0)
                .verifyComplete();
    }

    @Test
    void retrieveById_missingOrder_emitsEntityNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(orderRepository.findByIdAndUserId(99L, currentUser.getId())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.retrieveById(99L))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    void retrieveById_orderOfAnotherUser_emitsEntityNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(orderRepository.findByIdAndUserId(5L, currentUser.getId())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.retrieveById(5L))
                .expectError(EntityNotFoundException.class)
                .verify();

        verify(orderRepository, never()).findById(5L);
    }

    @Test
    void createFromCart_createsOrderForCartOwnerAndClearsOnlyThisCart() {
        var cart = TestEntityFactory.cart(1L, currentUser.getId());
        var countedItem = TestEntityFactory.countedItem(1L, 1L);
        countedItem.setId(10L);
        var savedOrder = TestEntityFactory.order(3L, currentUser.getId());
        var dto = new OrderDto(3L, List.of(), BigDecimal.ZERO);

        when(cartService.retrieveCartEntity()).thenReturn(Mono.just(cart));
        when(cartService.getCountedItems(cart)).thenReturn(Flux.just(countedItem));
        when(cartService.retrieveItems(cart)).thenReturn(Mono.just(List.of()));
        when(paymentService.payOrderAmount(any(BigDecimal.class))).thenReturn(Mono.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(ordersItemsRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(new OrdersItems(3L, 10L)));
        when(cartCountedItemsRepository.deleteByCartId(1L)).thenReturn(Mono.empty());
        when(userService.getCurrentUser()).thenReturn(Mono.just(currentUser));
        when(orderRepository.findByIdAndUserId(3L, currentUser.getId())).thenReturn(Mono.just(savedOrder));
        when(ordersItemsRepository.findByOrderId(3L)).thenReturn(Flux.empty());
        when(orderMapper.toDto(any(Order.class), any(BigDecimal.class), any())).thenReturn(dto);

        StepVerifier.create(orderService.createFromCart())
                .expectNext(dto)
                .verifyComplete();

        var orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getUserId()).isEqualTo(currentUser.getId());
        verify(cartCountedItemsRepository, never()).deleteAll();
    }
}
