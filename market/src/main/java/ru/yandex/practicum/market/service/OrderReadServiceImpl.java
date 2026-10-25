package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.model.Order;

@Primary
@Service
@AllArgsConstructor
public class OrderReadServiceImpl implements OrderReadService {
    private final UserService userService;
    private final UserOrdersProvider userOrdersProvider;

    @Override
    public Flux<OrderDto> retrieveOrders() {
        return userService.getCurrentUser()
                .flatMapMany(user -> userOrdersProvider.retrieveOrders(user.getId()));
    }

    @Override
    public Mono<OrderDto> retrieveById(long id) {
        return userService.getCurrentUser()
                .flatMap(user -> userOrdersProvider.retrieveById(user.getId(), id))
                .switchIfEmpty(Mono.defer(() -> {
                    var criteria = new Order();
                    criteria.setId(id);
                    return Mono.error(new EntityNotFoundException(criteria));
                }));
    }

    @Override
    @CacheEvict(value = "orders", allEntries = true)
    public void cacheEvictOrders() {}

    @CacheEvict(value = "order", allEntries = true)
    @Override
    public void cacheEvictOrderById(long id) {}
}
