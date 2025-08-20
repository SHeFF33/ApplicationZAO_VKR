package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.client.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartTruck> trucks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "cart_product",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    public Cart() {
    }

    public Cart(Client client) {
        this.client = client;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addProduct(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().equals(product) && item.getSum() == null) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        CartItem cartItem = new CartItem(this, product, quantity, null);
        cartItems.add(cartItem);
        products.add(product);
    }

    public void addProduct(Product product, int quantity, Sum sum) {
        for (CartItem item : cartItems) {
            if (item.getProduct().equals(product) && 
                (item.getSum() == null || item.getSum().equals(sum))) {
                item.setQuantity(item.getQuantity() + quantity);
                item.setSum(sum);
                return;
            }
        }
        CartItem cartItem = new CartItem(this, product, quantity, sum);
        cartItems.add(cartItem);
        products.add(product);
    }

    public void updateItemPrice(CartItem item, Sum sum) {
        item.setSum(sum);
    }

    @Transactional
    public List<CartTruck> distributeItemsToTrucks() {
        // Инициализируем trucks, если они null
        if (trucks == null) {
            trucks = new ArrayList<>();
        } else {
            // Очищаем существующие записи
            trucks.forEach(truck -> {
                if (truck.getItems() != null) {
                    truck.getItems().clear();
                }
            });
            trucks.clear();
        }

        // Сортируем товары в зависимости от типа номенклатуры
        List<CartItem> sortedItems;
        if (!cartItems.isEmpty()) {
            NomenclatureType cartType = cartItems.get(0).getProduct().getNomenclatureType();
            
            if (cartType == NomenclatureType.RAILWAY) {
                // Для ЖД товаров сортируем по толщине (9мм и 12мм в начало)
                sortedItems = cartItems.stream()
                        .sorted(Comparator.comparingDouble(item -> {
                            double tolsh = item.getProduct().getTolsh();
                            if (Math.abs(tolsh - Product.THICKNESS_9MM) < 0.1) return 0;
                            if (Math.abs(tolsh - Product.THICKNESS_12MM) < 0.1) return 1;
                            return 2;
                        }))
                        .collect(Collectors.toList());
            } else {
                // Для авто товаров сортируем по длине (2.8 в начало)
                sortedItems = cartItems.stream()
                        .sorted(Comparator.comparingDouble(item ->
                                item.getProduct().getLength() == 2.8 ? 0 : 1))
                        .collect(Collectors.toList());
            }
        } else {
            sortedItems = new ArrayList<>();
        }

        CartTruck currentTruck = new CartTruck(this);
        trucks.add(currentTruck);

        for (CartItem item : sortedItems) {
            int remainingQuantity = item.getQuantity();

            while (remainingQuantity > 0) {
                List<Product> productsInTruck = currentTruck.getItems().stream()
                        .flatMap(truckItem ->
                                Collections.nCopies(truckItem.getQuantity(), truckItem.getProduct()).stream())
                        .collect(Collectors.toList());

                int availableSpace = currentTruck.getAvailableSpace(item.getProduct(), productsInTruck);

                if (availableSpace <= 0) {
                    currentTruck = new CartTruck(this);
                    trucks.add(currentTruck);
                    availableSpace = currentTruck.getAvailableSpace(item.getProduct(), new ArrayList<>());
                }

                int quantityToAdd = Math.min(remainingQuantity, availableSpace);
                currentTruck.addItem(item.getProduct(), quantityToAdd, item.getSum());
                remainingQuantity -= quantityToAdd;
            }
        }

        return trucks;
    }

    // Геттеры и сеттеры для trucks
    public List<CartTruck> getTrucks() {
        return trucks;
    }

    public void setTrucks(List<CartTruck> trucks) {
        this.trucks = trucks;
    }
}
