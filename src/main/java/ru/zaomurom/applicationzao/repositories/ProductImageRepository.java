package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.product.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}

