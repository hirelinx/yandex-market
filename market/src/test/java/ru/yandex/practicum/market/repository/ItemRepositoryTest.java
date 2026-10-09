package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;

@DataR2dbcTest
@ActiveProfiles("test")
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void saveAndFindById() {
        StepVerifier.create(
                        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.valueOf(80)))
                                .flatMap(saved -> itemRepository.findById(saved.getId()))
                )
                .expectNextMatches(found -> "Молоко".equals(found.getTitle()))
                .verifyComplete();
    }

    @Test
    void findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase_findsByTitle() {
        StepVerifier.create(
                        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN))
                                .then(itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE)))
                                .thenMany(itemRepository.findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                        "молок", "молок", PageRequest.of(0, 10)))
                )
                .expectNextMatches(item -> "Молоко".equals(item.getTitle()))
                .verifyComplete();
    }
}
