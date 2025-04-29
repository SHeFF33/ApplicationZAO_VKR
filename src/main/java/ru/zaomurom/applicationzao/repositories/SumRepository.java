package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.Sum;

import java.util.List;

public interface SumRepository extends JpaRepository<Sum, Long> {
    List<Sum> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}
