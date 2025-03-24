package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
