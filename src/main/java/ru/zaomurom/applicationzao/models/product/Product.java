package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {
    public static final int MAX_ITEMS_SHORT = 16;
    public static final int MAX_ITEMS_LONG = 13;
    public static final int THRESHOLD_LONG = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String sort;
    private String tolsh;
    private Double length;
    private Double volume;
    private int quantity;
    @Column(length = 3000)
    private String description;
    private boolean visible = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sum> sums;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documentation> documentations;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    public Product() {}

    public Product(String name, String sort, String tolsh, double length, double volume, int quantity, String description, boolean visible) {
        this.name = name;
        this.sort = sort;
        this.tolsh = tolsh;
        this.length = length;
        this.volume = volume;
        this.quantity = quantity;
        this.description = description;
        this.visible = visible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getTolsh() {
        return tolsh;
    }

    public void setTolsh(String tolsh) {
        this.tolsh = tolsh;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Sum> getSums() {
        return sums;
    }

    public void setSums(List<Sum> sums) {
        this.sums = sums;
    }

    public List<Documentation> getDocumentations() {
        return documentations;
    }

    public void setDocumentations(List<Documentation> documentations) {
        this.documentations = documentations;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<CartItem> getCarts() {
        return cartItems;
    }

    public void setCarts(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isReadyForDisplay() {
        return !images.isEmpty() && !sums.isEmpty();
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }
    public int getMaxItemsPerTruck(List<Product> productsInTruck) {
        long longProductsCount = productsInTruck.stream()
                .filter(p -> p.length == 2.8)
                .count();

        if (longProductsCount >= THRESHOLD_LONG) {
            return MAX_ITEMS_LONG; // 13
        }
        return MAX_ITEMS_SHORT; // 16
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public boolean affectsTruckCapacity(List<Product> productsInTruck) {
        long longProductsCount = productsInTruck.stream()
                .filter(p -> p.length == 2.8)
                .count();
        return this.length == 2.8 || longProductsCount >= 7;
    }
}
