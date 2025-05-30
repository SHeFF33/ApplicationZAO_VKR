package ru.zaomurom.applicationzao.models.order;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.product.Product;

@Entity
@Table(name = "tch_orders")
public class TCHOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "truck_id")
    private OrderTruck truck;

    private int quantity;
    private double price;
    private double volume;

    @Column(name = "original_price")
    private Double originalPrice;

    @Column(name = "discount")
    private Double discount;

    public TCHOrder() {}

    public TCHOrder(Order order, Product product, int quantity, double price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.volume = product != null ? product.getVolume() : 0.0;
    }

    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        if (originalPrice == null) {
            originalPrice = price;
        }
        if (product != null) {
            this.volume = product.getVolume();
        }
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.volume = product.getVolume();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public OrderTruck getTruck() {
        return truck;
    }

    public void setTruck(OrderTruck truck) {
        this.truck = truck;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getTotalSum() {
        double total = quantity * price;
        return Math.round(total * 100.0) / 100.0;
    }
}