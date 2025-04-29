package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.order.OrderTruck;

public interface OrderTruckRepository extends JpaRepository<OrderTruck, Long> {
}
