package ru.zaomurom.applicationzao.models.order;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class OrderTruck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double total;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TCHOrder> tchOrders = new ArrayList<>();

    public OrderTruck() {}

    public OrderTruck(Order order) {
        this.order = order;
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

    public List<TCHOrder> getTchOrders() {
        return tchOrders;
    }

    public void setTchOrders(List<TCHOrder> tchOrders) {
        this.tchOrders = tchOrders;
    }
}