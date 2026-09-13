package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CountedItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CountedItemRepository countedItemRepository;

    @Test
    void findFirstByItem_Id() {
        var item = itemRepository.save(TestEntityFactory.item("Сыр", BigDecimal.valueOf(200)));
        var countedItem = countedItemRepository.save(TestEntityFactory.countedItem(item, 3L));

        assertThat(countedItemRepository.findFirstByItem_Id(item.getId()))
                .contains(countedItem);
    }

    @Test
    void findAllByItem_IdIn() {
        var first = itemRepository.save(TestEntityFactory.item("Сыр", BigDecimal.valueOf(200)));
        var second = itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE));
        countedItemRepository.save(TestEntityFactory.countedItem(first, 1L));
        countedItemRepository.save(TestEntityFactory.countedItem(second, 2L));

        var result = countedItemRepository.findAllByItem_IdIn(List.of(first.getId(), second.getId()));

        assertThat(result).hasSize(2);
    }
}
