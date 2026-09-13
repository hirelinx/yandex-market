package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void saveAndFindById() {
        var saved = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.valueOf(80)));

        assertThat(itemRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase_findsByTitle() {
        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));
        itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE));

        var page = itemRepository.findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "молок", "молок", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Молоко");
    }
}
