package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;

@Entity
public class Documentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @Lob
    @Column(name = "bytes", columnDefinition = "LONGBLOB")
    private byte[] bytes;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Documentation() {}

    public Documentation(String name, byte[] bytes, String description) {
        this.name = name;
        this.bytes = bytes;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
