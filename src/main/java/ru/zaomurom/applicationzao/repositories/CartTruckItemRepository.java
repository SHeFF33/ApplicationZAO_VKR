package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.CartTruckItem;

public interface CartTruckItemRepository extends JpaRepository<CartTruckItem, Long> {
}