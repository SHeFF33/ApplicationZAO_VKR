package ru.zaomurom.applicationzao.dto;

import java.util.Date;

public class ProductDTO {
    private Long id;
    private String name;
    private String sort;
    private String tolsh;
    private Double length;
    private Double volume;
    private int quantity;
    private String description;
    private boolean visible;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public String getTolsh() { return tolsh; }
    public void setTolsh(String tolsh) { this.tolsh = tolsh; }
    public Double getLength() { return length; }
    public void setLength(Double length) { this.length = length; }
    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}