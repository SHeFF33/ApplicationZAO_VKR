package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;

@Entity
public class CartTruckItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "truck_id")
    private CartTruck truck;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "sum_id")
    private Sum sum;

    public CartTruckItem() {}

    public CartTruckItem(CartTruck truck, Product product, int quantity, Sum sum) {
        this.truck = truck;
        this.product = product;
        this.quantity = quantity;
        this.sum = sum;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CartTruck getTruck() {
        return truck;
    }

    public void setTruck(CartTruck truck) {
        this.truck = truck;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Sum getSum() {
        return sum;
    }

    public void setSum(Sum sum) {
        this.sum = sum;
    }
}