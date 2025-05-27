package ru.zaomurom.applicationzao.dto;

public class TCHOrderPriceUpdateDTO {
    private Long tchOrderId;
    private Double newPrice;

    public Long getTchOrderId() {
        return tchOrderId;
    }

    public void setTchOrderId(Long tchOrderId) {
        this.tchOrderId = tchOrderId;
    }

    public Double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }
}