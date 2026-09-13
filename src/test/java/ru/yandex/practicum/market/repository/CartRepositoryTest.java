package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.market.support.TestEntityFactory;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CartRepositoryTest {
    @Autowired
    private CartRepository cartRepository;

    @Test
    void saveAndFindAll() {
        cartRepository.save(TestEntityFactory.emptyCart());

        assertThat(cartRepository.findAll()).hasSize(1);
    }
}
