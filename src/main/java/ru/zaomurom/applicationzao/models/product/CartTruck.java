package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class CartTruck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartTruckItem> items = new ArrayList<>();

    public CartTruck() {}

    public CartTruck(Cart cart) {
        this.cart = cart;
    }

    public boolean canAddProduct(Product product, List<Product> productsInTruck) {
        int totalItems = getTotalItems();
        long longProductsCount = productsInTruck.stream()
                .filter(p -> p.getLength() == 2.8)
                .count();

        int maxItems = longProductsCount >= 7 ?
                Product.MAX_ITEMS_LONG : Product.MAX_ITEMS_SHORT;

        return totalItems < maxItems;
    }

    public int getAvailableSpace(Product product, List<Product> productsInTruck) {
        int totalItems = getTotalItems();
        long longProductsCount = productsInTruck.stream()
                .filter(p -> p.getLength() == 2.8)
                .count();

        int maxItems = longProductsCount >= 7 ?
                Product.MAX_ITEMS_LONG : Product.MAX_ITEMS_SHORT;

        return maxItems - totalItems;
    }

    private int getTotalItems() {
        return items.stream().mapToInt(CartTruckItem::getQuantity).sum();
    }

    public void addItem(Product product, int quantity, Sum sum) {
        CartTruckItem existingItem = items.stream()
                .filter(item -> item.getProduct().equals(product) && item.getSum().equals(sum))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartTruckItem newItem = new CartTruckItem(this, product, quantity, sum);
            items.add(newItem);
        }
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<CartTruckItem> getItems() {
        return items;
    }

    public void setItems(List<CartTruckItem> items) {
        this.items = items;
    }
}