package ru.zaomurom.applicationzao.dto;

import java.util.List;

public class ManualDistributionRequest {
    private List<TruckDTO> trucks;

    public List<TruckDTO> getTrucks() {
        return trucks;
    }

    public void setTrucks(List<TruckDTO> trucks) {
        this.trucks = trucks;
    }

    public ManualDistributionRequest(List<TruckDTO> trucks) {
        this.trucks = trucks;
    }

    public static class TruckDTO {
        private String id;
        private List<TruckItemDTO> items;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<TruckItemDTO> getItems() {
            return items;
        }

        public void setItems(List<TruckItemDTO> items) {
            this.items = items;
        }

        public TruckDTO(String id, List<TruckItemDTO> items) {
            this.id = id;
            this.items = items;
        }

        // геттеры и сеттеры
    }

    public static class TruckItemDTO {
        private Long productId;
        private Long sumId;
        private int quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getSumId() {
            return sumId;
        }

        public void setSumId(Long sumId) {
            this.sumId = sumId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public TruckItemDTO(Long productId, Long sumId, int quantity) {
            this.productId = productId;
            this.sumId = sumId;
            this.quantity = quantity;
        }
// геттеры и сеттеры
    }
}