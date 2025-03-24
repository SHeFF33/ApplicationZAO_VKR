    package ru.zaomurom.applicationzao.services;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import ru.zaomurom.applicationzao.models.product.Product;
    import ru.zaomurom.applicationzao.models.product.Sum;
    import ru.zaomurom.applicationzao.repositories.ProductRepository;
    import ru.zaomurom.applicationzao.repositories.SumRepository;

    import java.util.List;
    import java.util.Optional;

    @Service
    public class ProductService {
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private SumRepository sumRepository;

        public Optional<Sum> findSumById(Long sumId) {
            return sumRepository.findById(sumId);
        }

        public List<Product> findAll() {
            return productRepository.findAll();
        }

        public Optional<Product> findById(Long id) {
            return productRepository.findById(id);
        }

        public Product save(Product product) {
            return productRepository.save(product);
        }
    }
