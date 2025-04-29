package ru.zaomurom.applicationzao.dto;

public class CartTruckItemDTO {
    private Long id;
    private Long productId;
    private Long sumId;
    private int quantity;
    private String productName;
    private Double length;

    // конструктор, геттеры и сеттеры


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSumId() {
        return sumId;
    }

    public void setSumId(Long sumId) {
        this.sumId = sumId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public CartTruckItemDTO(Long id, Long productId, Long sumId, int quantity, String productName, Double length) {
        this.id = id;
        this.productId = productId;
        this.sumId = sumId;
        this.quantity = quantity;
        this.productName = productName;
        this.length = length;
    }
}