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
        
        // Определяем тип номенклатуры
        NomenclatureType productType = product.getNomenclatureType();
        
        if (productType == NomenclatureType.RAILWAY) {
            // Для ЖД товаров всегда максимум 49 пачек
            return totalItems < Product.MAX_ITEMS_RAILWAY;
        } else {
            // Логика для авто товаров (существующая)
            long longProductsCount = productsInTruck.stream()
                    .filter(p -> p.getLength() == 2.8)
                    .count();

            // Если добавляем длинный товар или уже есть 7+ длинных
            if (product.getLength() == 2.8 || longProductsCount >= 7) {
                return totalItems < Product.MAX_ITEMS_LONG;
            }
            return totalItems < Product.MAX_ITEMS_SHORT;
        }
    }

    public int getAvailableSpace(Product product, List<Product> productsInTruck) {
        int totalItems = getTotalItems();
        
        // Определяем тип номенклатуры
        NomenclatureType productType = product.getNomenclatureType();
        
        if (productType == NomenclatureType.RAILWAY) {
            // Для ЖД товаров всегда максимум 49 пачек
            return Product.MAX_ITEMS_RAILWAY - totalItems;
        } else {
            // Логика для авто товаров (существующая)
            long longProductsCount = productsInTruck.stream()
                    .filter(p -> p.getLength() == 2.8)
                    .count();

            // Если добавляем длинный товар или уже есть 7+ длинных
            if (product.getLength() == 2.8 || longProductsCount >= 7) {
                return Product.MAX_ITEMS_LONG - totalItems;
            }
            return Product.MAX_ITEMS_SHORT - totalItems;
        }
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