package ru.zaomurom.applicationzao.dto;

import java.util.Date;

public class SumDTO {
    private Long id;
    private Double summa;
    private Date period;
    private String Region;
    private Long productId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getSumma() { return summa; }
    public void setSumma(Double summa) { this.summa = summa; }
    public Date getPeriod() { return period; }
    public void setPeriod(Date period) { this.period = period; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getRegion() {
        return Region;
    }
    public void setRegion(String region) {
        Region = region;
    }

}