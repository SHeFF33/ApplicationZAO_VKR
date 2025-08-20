package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {
    public static final int MAX_ITEMS_SHORT = 16;
    public static final int MAX_ITEMS_LONG = 13;
    public static final int THRESHOLD_LONG = 7;
    public static final int MAX_IMAGES = 3;
    
    // Константы для ЖД товаров
    public static final int MAX_ITEMS_RAILWAY = 49;
    public static final double THICKNESS_9MM = 9.0;
    public static final double THICKNESS_12MM = 12.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String sort;
    private Double tolsh;
    private Double length;
    private Double volume;
    private int quantity;
    @Column(length = 3000)
    private String description;
    private boolean visible = false;

    @Enumerated(EnumType.STRING)
    private NomenclatureType nomenclatureType = NomenclatureType.AUTO;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sum> sums;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documentation> documentations;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    public Product() {}

    public Product(String name, String sort, double tolsh, double length, double volume, int quantity, String description, boolean visible, NomenclatureType nomenclatureType) {
        this.name = name;
        this.sort = sort;
        this.tolsh = tolsh;
        this.length = length;
        this.volume = volume;
        this.quantity = quantity;
        this.description = description;
        this.visible = visible;
        this.nomenclatureType = nomenclatureType;
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

    public double getTolsh() {
        return tolsh;
    }

    public void setTolsh(double tolsh) {
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

    public boolean canAddMoreImages() {
        return images.size() < MAX_IMAGES;
    }

    public int getRemainingImageSlots() {
        return MAX_IMAGES - images.size();
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }
    public int getMaxItemsPerTruck(List<Product> productsInTruck) {
        // Проверяем тип номенклатуры первого товара в грузовике
        if (!productsInTruck.isEmpty()) {
            NomenclatureType firstProductType = productsInTruck.get(0).getNomenclatureType();
            
            if (firstProductType == NomenclatureType.RAILWAY) {
                return getMaxItemsPerRailwayWagon(productsInTruck);
            }
        }
        
        // Логика для авто товаров (существующая)
        long longProductsCount = productsInTruck.stream()
                .filter(p -> p.length == 2.8)
                .count();

        if (longProductsCount >= THRESHOLD_LONG) {
            return MAX_ITEMS_LONG; // 13
        }
        return MAX_ITEMS_SHORT; // 16
    }
    
    private int getMaxItemsPerRailwayWagon(List<Product> productsInTruck) {
        // Проверяем, есть ли товары с толщиной 9мм или 12мм
        boolean has9mmThickness = productsInTruck.stream()
                .anyMatch(p -> Math.abs(p.getTolsh() - THICKNESS_9MM) < 0.1);
        
        boolean has12mmThickness = productsInTruck.stream()
                .anyMatch(p -> Math.abs(p.getTolsh() - THICKNESS_12MM) < 0.1);
        
        if (has9mmThickness) {
            // Для толщины 9мм: 40 пачек (72 листа) + 9 пачек (88 листов) = 49 пачек
            return MAX_ITEMS_RAILWAY; // 49
        } else if (has12mmThickness) {
            // Для толщины 12мм: 40 пачек (44 листа) + 9 пачек (66 листов) = 49 пачек
            return MAX_ITEMS_RAILWAY; // 49
        } else {
            // Для остальных толщин: просто ограничение 49 пачек
            return MAX_ITEMS_RAILWAY; // 49
        }
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
        // Проверяем тип номенклатуры
        if (!productsInTruck.isEmpty()) {
            NomenclatureType firstProductType = productsInTruck.get(0).getNomenclatureType();
            
            if (firstProductType == NomenclatureType.RAILWAY) {
                // Для ЖД товаров: проверяем толщину 9мм и 12мм
                boolean has9mmThickness = productsInTruck.stream()
                        .anyMatch(p -> Math.abs(p.getTolsh() - THICKNESS_9MM) < 0.1);
                boolean has12mmThickness = productsInTruck.stream()
                        .anyMatch(p -> Math.abs(p.getTolsh() - THICKNESS_12MM) < 0.1);
                
                return Math.abs(this.getTolsh() - THICKNESS_9MM) < 0.1 || 
                       Math.abs(this.getTolsh() - THICKNESS_12MM) < 0.1 ||
                       has9mmThickness || has12mmThickness;
            }
        }
        
        // Логика для авто товаров (существующая)
        long longProductsCount = productsInTruck.stream()
                .filter(p -> p.length == 2.8)
                .count();
        return this.length == 2.8 || longProductsCount >= 7;
    }

    public NomenclatureType getNomenclatureType() {
        return nomenclatureType;
    }
    public void setNomenclatureType(NomenclatureType nomenclatureType) {
        this.nomenclatureType = nomenclatureType;
    }
}
