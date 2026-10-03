package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.List;

@DataR2dbcTest
@ActiveProfiles("test")
class CountedItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CountedItemRepository countedItemRepository;

    @Test
    void findFirstByItemId() {
        StepVerifier.create(
                        itemRepository.save(TestEntityFactory.item("Сыр", BigDecimal.valueOf(200)))
                                .flatMap(item -> countedItemRepository.save(TestEntityFactory.countedItem(item.getId(), 3L)))
                                .flatMap(countedItem -> countedItemRepository.findFirstByItemId(countedItem.getItemId()))
                )
                .expectNextMatches(found -> found.getCount() == 3L)
                .verifyComplete();
    }

    @Test
    void findAllByItemIdIn() {
        StepVerifier.create(
                        itemRepository.save(TestEntityFactory.item("Сыр", BigDecimal.valueOf(200)))
                                .flatMapMany(first -> itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE))
                                        .flatMapMany(second -> countedItemRepository.save(TestEntityFactory.countedItem(first.getId(), 1L))
                                                .then(countedItemRepository.save(TestEntityFactory.countedItem(second.getId(), 2L)))
                                                .thenMany(countedItemRepository.findAllByItemIdIn(List.of(first.getId(), second.getId())))))
                )
                .expectNextCount(2)
                .verifyComplete();
    }
}
