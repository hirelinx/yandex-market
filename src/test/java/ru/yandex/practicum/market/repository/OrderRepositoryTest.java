package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.model.Order;

@DataR2dbcTest
@ActiveProfiles("test")
class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveAndFindById() {
        StepVerifier.create(
                        orderRepository.save(new Order())
                                .flatMap(saved -> orderRepository.findById(saved.getId()))
                )
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findAll() {
        StepVerifier.create(
                        orderRepository.save(new Order())
                                .thenMany(orderRepository.findAll())
                )
                .expectNextCount(1)
                .verifyComplete();
    }
}
