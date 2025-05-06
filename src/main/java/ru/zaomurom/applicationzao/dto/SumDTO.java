package ru.zaomurom.applicationzao.dto;

import java.util.Date;

public class SumDTO {
    private Long id;
    private Double summa;
    private Date period;
    private Long priceId;
    private String priceName;
    private Long productId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getSumma() { return summa; }
    public void setSumma(Double summa) { this.summa = summa; }
    public Date getPeriod() { return period; }
    public void setPeriod(Date period) { this.period = period; }
    public Long getPriceId() { return priceId; }
    public void setPriceId(Long priceId) { this.priceId = priceId; }
    public String getPriceName() { return priceName; }
    public void setPriceName(String priceName) { this.priceName = priceName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}