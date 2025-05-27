package ru.zaomurom.applicationzao.models.prices;

import jakarta.persistence.*;
import org.hibernate.annotations.OptimisticLock;

@Entity
@Table(name = "prices_on_regions")
public class PricesOnRegions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false)
    private Double thickness;

    @Column(name = "price_per_square_meter", nullable = false)
    private Double pricePerSquareMeter;

    @Version
    private Long version;

    // Конструкторы
    public PricesOnRegions() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
    public Double getThickness() { return thickness; }
    public void setThickness(Double thickness) { this.thickness = thickness; }
    public Double getPricePerSquareMeter() { return pricePerSquareMeter; }
    public void setPricePerSquareMeter(Double pricePerSquareMeter) { this.pricePerSquareMeter = pricePerSquareMeter; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}