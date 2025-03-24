package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.order.TCHOrder;
import ru.zaomurom.applicationzao.models.product.Sum;

import java.util.Optional;

public interface TchOrderRepository extends JpaRepository<TCHOrder, Long> {
    Optional<TCHOrder> findById(Long id);
}
