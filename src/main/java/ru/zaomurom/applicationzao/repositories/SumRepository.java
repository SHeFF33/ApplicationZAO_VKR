package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.Product;
import ru.zaomurom.applicationzao.models.product.Sum;

import java.util.List;
import java.util.Optional;

public interface SumRepository extends JpaRepository<Sum, Long> {
    List<Sum> findByProductId(Long productId);
    void deleteByProductId(Long productId);
    Optional<Sum> findByProductAndRegionName(Product product, String regionName);
}
