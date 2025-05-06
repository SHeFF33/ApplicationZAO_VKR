package ru.zaomurom.applicationzao.dto;

public class OrderTruckDTO {
    private Long id;
    private Long orderId;
    private double total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}