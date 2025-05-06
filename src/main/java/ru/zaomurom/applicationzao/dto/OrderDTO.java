package ru.zaomurom.applicationzao.dto;

import java.util.Date;

public class OrderDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long deliveryAddressId;
    private String deliveryAddressString;
    private Date deliveryDate;
    private Date orderDate;
    private String comment;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public Long getDeliveryAddressId() { return deliveryAddressId; }
    public void setDeliveryAddressId(Long deliveryAddressId) { this.deliveryAddressId = deliveryAddressId; }
    public String getDeliveryAddressString() { return deliveryAddressString; }
    public void setDeliveryAddressString(String deliveryAddressString) { this.deliveryAddressString = deliveryAddressString; }
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}