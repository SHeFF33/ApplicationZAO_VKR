package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.Documentation;

public interface DocumentationRepository extends JpaRepository<Documentation, Long> {
    void deleteByProductId(Long productId);
}
