package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.client.Price;

import java.util.Date;

@Entity
@Table(name = "sum")
public class Sum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double summa; // Это и будет цена
    private Date period; // Период цены

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "price_id")
    private Price price;

    // Конструкторы, геттеры и сеттеры
    public Sum() {}

    public Sum(Double summa, Date period) {
        this.summa = summa;
        this.period = period;
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

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}
