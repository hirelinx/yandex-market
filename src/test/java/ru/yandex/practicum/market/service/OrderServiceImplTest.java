package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.OrderMapper;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.OrderRepository;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
    private CartRepository cartRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void retrieveOrders_mapsAllOrders() {
        var order = new Order();
        order.setId(1L);
        var dto = new OrderDto(1L, List.of(), BigDecimal.ZERO);
        when(orderRepository.findAllBy()).thenReturn(Set.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        assertThat(orderService.retrieveOrders()).containsExactly(dto);
    }

    @Test
    void retrieveById_existingOrder_returnsDto() {
        var order = new Order();
        order.setId(2L);
        var dto = new OrderDto(2L, List.of(), BigDecimal.TEN);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        assertThat(orderService.retrieveById(2L)).isEqualTo(dto);
    }

    @Test
    void retrieveById_missingOrder_throwsEntityNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.retrieveById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createFromCart_createsOrderAndClearsCart() {
        var item = TestEntityFactory.item("Молоко", BigDecimal.valueOf(50));
        var countedItem = TestEntityFactory.countedItem(item, 1L);
        var cart = TestEntityFactory.emptyCart();
        cart.getCountedItems().add(countedItem);
        var savedOrder = new Order();
        savedOrder.setId(3L);
        savedOrder.getItems().add(countedItem);
        var dto = new OrderDto(3L, List.of(new ItemDto(1L, "Молоко", "desc", "files/test.jpg", BigDecimal.valueOf(50), 1)), BigDecimal.valueOf(50));

        when(cartService.retrieveCartEntity()).thenReturn(cart);
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(dto);

        var result = orderService.createFromCart();

        assertThat(result.id()).isEqualTo(3L);
        assertThat(cart.getCountedItems()).isEmpty();
        verify(cartRepository).save(cart);
    }
}
