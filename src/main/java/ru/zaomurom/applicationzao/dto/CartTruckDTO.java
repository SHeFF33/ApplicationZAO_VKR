package ru.zaomurom.applicationzao.dto;

import java.util.List;

public class CartTruckDTO {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartTruckItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartTruckItemDTO> items) {
        this.items = items;
    }

    public CartTruckDTO(Long id, List<CartTruckItemDTO> items) {
        this.id = id;
        this.items = items;
    }

    private List<CartTruckItemDTO> items;

    // конструктор, геттеры и сеттеры
}
