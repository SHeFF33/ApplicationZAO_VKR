package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.zaomurom.applicationzao.models.product.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
    @Query("SELECT p FROM Product p WHERE p.visible = true")
    List<Product> findAllVisible();
    @Query("SELECT p FROM Product p")
    List<Product> findBySearch();
}
