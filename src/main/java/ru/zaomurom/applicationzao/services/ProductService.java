    package ru.zaomurom.applicationzao.services;

    import jakarta.persistence.EntityManager;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import ru.zaomurom.applicationzao.models.product.Cart;
    import ru.zaomurom.applicationzao.models.product.CartItem;
    import ru.zaomurom.applicationzao.models.product.Product;
    import ru.zaomurom.applicationzao.models.product.Sum;
    import ru.zaomurom.applicationzao.repositories.*;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @Service
    public class ProductService {
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private SumRepository sumRepository;
        @Autowired
        private DocumentationRepository documentationRepository;
        @Autowired
        private ProductImageRepository productImageRepository;
        @Autowired
        private CartRepository cartRepository;
        @Autowired
        private CartItemRepository cartItemRepository;

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
        @Transactional
        public void deleteProduct(Long id) {
            Product product = productRepository.findById(id).orElseThrow();

            // Удаляем все связанные CartItems
            List<CartItem> cartItems = cartItemRepository.findByProduct(product);
            for (CartItem cartItem : cartItems) {
                Cart cart = cartItem.getCart();
                // Удаляем товар из списка products в корзине
                cart.getProducts().remove(product);
                // Удаляем CartItem из списка cartItems в корзине
                cart.getCartItems().remove(cartItem);
                cartRepository.save(cart);
            }
            // Удаляем все CartItems из базы
            cartItemRepository.deleteAll(cartItems);

            // Удаляем связанные суммы, документацию и изображения
            sumRepository.deleteByProductId(id);
            documentationRepository.deleteByProductId(id);
            productImageRepository.deleteByProductId(id);

            // Наконец, удаляем сам товар
            productRepository.delete(product);
        }

        public void updateProductVisibility(Long productId) {
            Product product = productRepository.findById(productId).orElseThrow();
            product.setVisible(product.isReadyForDisplay());
            productRepository.save(product);
        }

        public List<Product> findAllVisible() {
            return productRepository.findAllVisible();
        }
        public List<Product> findBySearch() {
            return productRepository.findBySearch();
        }
    }
