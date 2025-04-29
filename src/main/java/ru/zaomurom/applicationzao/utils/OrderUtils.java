package ru.zaomurom.applicationzao.utils;

import ru.zaomurom.applicationzao.models.order.TCHOrder;

import java.util.List;

public class OrderUtils {
    public static double calculateTruckTotal(List<TCHOrder> orders) {
        return orders.stream()
                .mapToDouble(t -> t.getPrice() * t.getQuantity())
                .sum();
    }
}
