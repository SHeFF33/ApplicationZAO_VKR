package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.CartTruck;

public interface CartTruckRepository extends JpaRepository<CartTruck, Long> {
    void deleteAll(Iterable<? extends CartTruck> trucks);
}