package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.product.ProductImage;
import ru.zaomurom.applicationzao.repositories.ProductImageRepository;

import java.util.Optional;

@Service
public class ProductImageService {
    @Autowired
    private ProductImageRepository productImageRepository;

    public ProductImage save(ProductImage image) {
        return productImageRepository.save(image);
    }

    public Optional<ProductImage> findById(Long id) {
        return productImageRepository.findById(id);
    }
}

