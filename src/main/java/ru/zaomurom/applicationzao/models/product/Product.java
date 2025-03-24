package ru.zaomurom.applicationzao.models.product;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String sort;
    private String tolsh;
    private int quantity;
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sum> sums;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documentation> documentations;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToMany(mappedBy = "products")
    private List<Cart> carts;

    public Product() {}

    public Product(String name, String sort, String tolsh, int quantity, String description) {
        this.name = name;
        this.sort = sort;
        this.tolsh = tolsh;
        this.quantity = quantity;
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

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getTolsh() {
        return tolsh;
    }

    public void setTolsh(String tolsh) {
        this.tolsh = tolsh;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Sum> getSums() {
        return sums;
    }

    public void setSums(List<Sum> sums) {
        this.sums = sums;
    }

    public List<Documentation> getDocumentations() {
        return documentations;
    }

    public void setDocumentations(List<Documentation> documentations) {
        this.documentations = documentations;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<Cart> getCarts() {
        return carts;
    }

    public void setCarts(List<Cart> carts) {
        this.carts = carts;
    }
}
