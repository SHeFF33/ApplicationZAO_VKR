package ru.zaomurom.applicationzao.dto;

import java.util.List;

public class CartDistributionDTO {
    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public List<CartTruckDTO> getTrucks() {
        return trucks;
    }

    public void setTrucks(List<CartTruckDTO> trucks) {
        this.trucks = trucks;
    }

    public CartDistributionDTO() {
    }


    public CartDistributionDTO(List<CartItemDTO> items, List<CartTruckDTO> trucks) {
        this.items = items;
        this.trucks = trucks;
    }

    private List<CartItemDTO> items;
    private List<CartTruckDTO> trucks;

    // геттеры и сеттеры
}
