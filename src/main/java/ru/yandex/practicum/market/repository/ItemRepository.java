package ru.yandex.practicum.market.repository;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.market.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<@NonNull Item, @NonNull Long> {
    Page<@NonNull Item> findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);
}
