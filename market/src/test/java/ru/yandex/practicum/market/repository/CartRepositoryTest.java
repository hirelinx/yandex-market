package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
class CartRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;

    @Test
    void saveAndFindFirstByUserId() {
        var newUser = new User();
        newUser.setUsername("cart-owner");
        newUser.setPasswordHash("neverlogin");
        var user = userRepository.save(newUser).block();

        var cart = new Cart();
        cart.setUserId(user.getId());

        StepVerifier.create(
                        cartRepository.save(cart)
                                .flatMap(saved -> cartRepository.findFirstByUserId(user.getId()))
                )
                .assertNext(found -> {
                    assertThat(found.getId()).isNotNull();
                    assertThat(found.getUserId()).isEqualTo(user.getId());
                })
                .verifyComplete();
    }
}
