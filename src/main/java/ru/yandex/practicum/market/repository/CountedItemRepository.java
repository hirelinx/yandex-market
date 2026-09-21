package ru.yandex.practicum.market.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.market.model.CountedItem;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CountedItemRepository extends JpaRepository<CountedItem, Long> {
    Optional<CountedItem> findFirstByItem_Id(Long id);

    Set<CountedItem> findAllByItem_IdIn(List<Long> ids);
    CountedItem findAllByItem_Id(Long id);
}
