package ru.yandex.practicum.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.market.model.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

}
