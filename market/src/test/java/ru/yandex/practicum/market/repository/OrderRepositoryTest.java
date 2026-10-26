package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrdersItemsRepository ordersItemsRepository;
    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User stranger;

    @BeforeEach
    void setUp() {
        ordersItemsRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        owner = saveUser("order-owner");
        stranger = saveUser("order-stranger");
    }

    @Test
    void saveAndFindById() {
        StepVerifier.create(
                        orderRepository.save(newOrder(owner))
                                .flatMap(saved -> orderRepository.findById(saved.getId()))
                )
                .assertNext(found -> assertThat(found.getUserId()).isEqualTo(owner.getId()))
                .verifyComplete();
    }

    @Test
    void findAllByUserId_returnsOnlyUserOrders() {
        orderRepository.save(newOrder(owner)).block();
        orderRepository.save(newOrder(owner)).block();
        orderRepository.save(newOrder(stranger)).block();

        StepVerifier.create(orderRepository.findAllByUserId(owner.getId()))
                .expectNextMatches(order -> order.getUserId().equals(owner.getId()))
                .expectNextMatches(order -> order.getUserId().equals(owner.getId()))
                .verifyComplete();
    }

    @Test
    void findByIdAndUserId_orderOfAnotherUser_returnsEmpty() {
        var order = orderRepository.save(newOrder(owner)).block();

        StepVerifier.create(orderRepository.findByIdAndUserId(order.getId(), stranger.getId()))
                .verifyComplete();
        StepVerifier.create(orderRepository.findByIdAndUserId(order.getId(), owner.getId()))
                .expectNextCount(1)
                .verifyComplete();
    }

    private User saveUser(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> userRepository.save(newUser(username))))
                .block();
    }

    private static User newUser(String username) {
        var user = new User();
        user.setUsername(username);
        user.setPasswordHash("neverlogin");
        return user;
    }

    private static Order newOrder(User user) {
        var order = new Order();
        order.setUserId(user.getId());
        return order;
    }
}
