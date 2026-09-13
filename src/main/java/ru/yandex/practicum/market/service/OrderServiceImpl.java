package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.OrderMapper;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.OrderRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;

    @Override
    public List<OrderDto> retrieveOrders() {
        var orders = orderRepository.findAllBy();

        return orders.stream().map(orderMapper::toDto).toList();
    }

    @Override
    public OrderDto retrieveById(long id) {
        var order = orderRepository.findById(id);

        if (order.isEmpty()){
            var criteria = new Order();
            criteria.setId(id);
            throw new EntityNotFoundException(criteria);
        }

        return orderMapper.toDto(order.get());
    }

    @Override
    public OrderDto createFromCart() {
        var cart = cartService.retrieveCartEntity();
        var countedItems = cart.getCountedItems();

        var order = new Order();
        order.getItems().addAll(countedItems);
        order = orderRepository.save(order);

        cart.getCountedItems().clear();
        cartRepository.save(cart);

        return orderMapper.toDto(order);
    }
}
