package ru.yandex.practicum.market.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CountedItemRepository countedItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveAndFindAllBy() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));
        var countedItem = countedItemRepository.save(TestEntityFactory.countedItem(item, 1L));
        var order = new Order();
        order.getItems().add(countedItem);
        orderRepository.save(order);

        assertThat(orderRepository.findAllBy()).hasSize(1);
    }

    @Test
    void findById() {
        var saved = orderRepository.save(new Order());

        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }
}
