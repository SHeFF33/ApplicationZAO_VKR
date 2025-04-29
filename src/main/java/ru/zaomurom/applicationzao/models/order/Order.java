package ru.zaomurom.applicationzao.models.order;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.Addresses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.text.SimpleDateFormat;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Addresses deliveryAddress;

    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveryDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TCHOrder> tchOrders;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDocumentation> documentations;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderTruck> trucks = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory;

    public Order() {}

    public Order(Client client, Addresses deliveryAddress, Date deliveryDate, String status) {
        this.client = client;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.orderDate = new Date();
    }

    // Геттеры и сеттеры
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

    public Addresses getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Addresses deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TCHOrder> getTchOrders() {
        return tchOrders;
    }

    public void setTchOrders(List<TCHOrder> tchOrders) {
        this.tchOrders = tchOrders;
    }

    public List<OrderDocumentation> getDocumentations() {
        return documentations;
    }

    public void setDocumentations(List<OrderDocumentation> documentations) {
        this.documentations = documentations;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<OrderStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public List<OrderTruck> getTrucks() {
        return trucks;
    }

    public void setTrucks(List<OrderTruck> trucks) {
        this.trucks = trucks;
    }

    public String getFormattedOrderDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        return formatter.format(orderDate);
    }

    public String getFormattedDeliveryDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        return formatter.format(deliveryDate);
    }
}
