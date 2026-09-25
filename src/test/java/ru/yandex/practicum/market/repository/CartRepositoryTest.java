package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.model.Cart;

@DataR2dbcTest
@ActiveProfiles("test")
class CartRepositoryTest {
    @Autowired
    private CartRepository cartRepository;

    @Test
    void saveAndFindFirstBy() {
        StepVerifier.create(
                        cartRepository.save(new Cart())
                                .flatMap(saved -> cartRepository.findFirstBy())
                )
                .expectNextCount(1)
                .verifyComplete();
    }
}
