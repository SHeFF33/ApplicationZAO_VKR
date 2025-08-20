package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegionsJD;

import java.util.Date;

@Entity
@Table(name = "sum")
public class Sum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double summa; // Это и будет цена
    private Date period; // Период цены
    private String regionName; // Название региона

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "prices_on_regions_id")
    private PricesOnRegions pricesOnRegions;

    @ManyToOne
    @JoinColumn(name = "prices_on_regions_jd_id")
    private PricesOnRegionsJD pricesOnRegionsJD;

    // Конструкторы, геттеры и сеттеры
    public Sum() {}

    public Sum(Double summa, Date period, String regionName) {
        this.summa = summa;
        this.period = period;
        this.regionName = regionName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getSumma() {
        return summa;
    }

    public void setSumma(Double summa) {
        this.summa = summa;
    }

    public Date getPeriod() {
        return period;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public PricesOnRegions getPricesOnRegions() {
        return pricesOnRegions;
    }

    public void setPricesOnRegions(PricesOnRegions pricesOnRegions) {
        this.pricesOnRegions = pricesOnRegions;
    }

    public PricesOnRegionsJD getPricesOnRegionsJD() {
        return pricesOnRegionsJD;
    }

    public void setPricesOnRegionsJD(PricesOnRegionsJD pricesOnRegionsJD) {
        this.pricesOnRegionsJD = pricesOnRegionsJD;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
