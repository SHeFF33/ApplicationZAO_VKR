package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.dto.*;
import ru.zaomurom.applicationzao.models.client.Addresses;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.product.*;
import ru.zaomurom.applicationzao.repositories.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartTruckRepository cartTruckRepository;

    @Autowired
    private SumRepository sumRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartTruckItemRepository cartTruckItemRepository;

    @Autowired
    private AddressesRepository addressesRepository;

    public Cart findByClient(Client client) {
        return cartRepository.findByClient(client);
    }

    @Transactional
    public void save(Cart cart) {
        // Перед сохранением выполняем распределение
        if (!cart.getCartItems().isEmpty()) {
            cart.distributeItemsToTrucks();
        }
        cartRepository.save(cart);
    }

    public void saveCartItem(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void deleteCartItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }
    @Transactional
    public void saveTrucksWithItems(Cart cart) {
        // Удаляем старые записи только если они есть
        if (!cart.getTrucks().isEmpty()) {
            cartTruckRepository.deleteAll(cart.getTrucks());
            cart.getTrucks().clear();
        }

        // Распределяем товары по машинам
        cart.distributeItemsToTrucks();

        // Сохраняем новые записи
        for (CartTruck truck : cart.getTrucks()) {
            cartTruckRepository.save(truck);
            for (CartTruckItem item : truck.getItems()) {
                item.setTruck(truck); // Убедимся, что связь установлена
                cartTruckItemRepository.save(item);
            }
        }
    }


    public CartDistributionDTO prepareCartDistributionDTO(Cart cart) {
        CartDistributionDTO dto = new CartDistributionDTO();

        // Все товары в корзине
        dto.setItems(cart.getCartItems().stream()
                .map(item -> new CartItemDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getSum() != null ? item.getSum().getId() : null,
                        item.getQuantity(),
                        item.getProduct().getName(),
                        item.getProduct().getLength()
                ))
                .collect(Collectors.toList()));

        // Автоматически распределенные машины
        cart.distributeItemsToTrucks();
        dto.setTrucks(cart.getTrucks().stream()
                .map(truck -> new CartTruckDTO(
                        truck.getId(),
                        truck.getItems().stream()
                                .map(item -> new CartTruckItemDTO(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getSum() != null ? item.getSum().getId() : null,
                                        item.getQuantity(),
                                        item.getProduct().getName(),
                                        item.getProduct().getLength()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public void applyManualDistribution(Cart cart, ManualDistributionRequest request) {
        // 1. Очищаем текущие trucks и их items
        cart.getTrucks().forEach(truck -> {
            cartTruckItemRepository.deleteAll(truck.getItems());
            truck.getItems().clear();
        });
        cartTruckRepository.deleteAll(cart.getTrucks());
        cart.getTrucks().clear();

        // 2. Сохраняем новое распределение
        for (ManualDistributionRequest.TruckDTO truckDTO : request.getTrucks()) {
            CartTruck truck = new CartTruck();
            truck.setCart(cart);
            cart.getTrucks().add(truck);
            cartTruckRepository.save(truck);

            for (ManualDistributionRequest.TruckItemDTO itemDTO : truckDTO.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                Sum sum = sumRepository.findById(itemDTO.getSumId())
                        .orElseThrow(() -> new RuntimeException("Sum not found"));

                CartTruckItem item = new CartTruckItem();
                item.setTruck(truck);
                item.setProduct(product);
                item.setSum(sum);
                item.setQuantity(itemDTO.getQuantity());

                cartTruckItemRepository.save(item);
                truck.getItems().add(item);
            }
        }

        // 3. Обновляем cartItems на основе текущего распределения
        updateCartItemsFromTrucks(cart);
        cartRepository.save(cart);
    }

    private void updateCartItemsFromTrucks(Cart cart) {
        // Удаляем все текущие CartItems
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        // Собираем все товары из всех машин
        Map<Pair<Long, Long>, Integer> productQuantities = new HashMap<>();

        cart.getTrucks().forEach(truck -> {
            truck.getItems().forEach(item -> {
                Pair<Long, Long> key = Pair.of(item.getProduct().getId(), item.getSum().getId());
                productQuantities.merge(key, item.getQuantity(), Integer::sum);
            });
        });

        // Создаем новые CartItems
        productQuantities.forEach((key, quantity) -> {
            Product product = productRepository.findById(key.getFirst())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            Sum sum = sumRepository.findById(key.getSecond())
                    .orElseThrow(() -> new RuntimeException("Sum not found"));

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setSum(sum);
            cartItem.setQuantity(quantity);

            cartItemRepository.save(cartItem);
            cart.getCartItems().add(cartItem);
        });
    }
    @Transactional
    public void autoDistribute(Cart cart) {
        // 1. Очищаем текущие trucks и их items
        cart.getTrucks().forEach(truck -> {
            cartTruckItemRepository.deleteAll(truck.getItems());
            truck.getItems().clear();
        });
        cartTruckRepository.deleteAll(cart.getTrucks());
        cart.getTrucks().clear();

        // 2. Сортируем товары по длине (2.8 в начало)
        List<CartItem> sortedItems = cart.getCartItems().stream()
                .sorted(Comparator.comparing(item ->
                        item.getProduct().getLength() == 2.8 ? 0 : 1))
                .collect(Collectors.toList());

        // 3. Распределяем товары по машинам
        CartTruck currentTruck = new CartTruck();
        currentTruck.setCart(cart);
        cart.getTrucks().add(currentTruck);
        cartTruckRepository.save(currentTruck);

        for (CartItem item : sortedItems) {
            int remainingQuantity = item.getQuantity();

            while (remainingQuantity > 0) {
                // Определяем доступное место в текущей машине
                int availableSpace = calculateAvailableSpace(currentTruck);

                if (availableSpace <= 0) {
                    // Создаем новую машину
                    currentTruck = new CartTruck();
                    currentTruck.setCart(cart);
                    cart.getTrucks().add(currentTruck);
                    cartTruckRepository.save(currentTruck);
                    availableSpace = calculateAvailableSpace(currentTruck);
                }

                int quantityToAdd = Math.min(remainingQuantity, availableSpace);
                addItemToTruck(currentTruck, item, quantityToAdd);
                remainingQuantity -= quantityToAdd;
            }
        }

        // 4. Сохраняем изменения
        cartRepository.save(cart);
    }

    private int calculateAvailableSpace(CartTruck truck) {
        long longProductsCount = truck.getItems().stream()
                .filter(item -> item.getProduct().getLength() == 2.8)
                .count();

        int totalItems = truck.getItems().stream()
                .mapToInt(CartTruckItem::getQuantity)
                .sum();

        // Определяем максимальное количество для этой машины
        if (longProductsCount >= 6) {
            return 13 - totalItems; // MAX_ITEMS_LONG
        } else {
            return 16 - totalItems; // MAX_ITEMS_SHORT
        }
    }

    private void addItemToTruck(CartTruck truck, CartItem cartItem, int quantity) {
        // Проверяем, есть ли уже такой товар в машине
        Optional<CartTruckItem> existingItem = truck.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItem.getProduct().getId()) &&
                        item.getSum().getId().equals(cartItem.getSum().getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Увеличиваем количество существующего товара
            CartTruckItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartTruckItemRepository.save(item);
        } else {
            // Создаем новый элемент в машине
            CartTruckItem newItem = new CartTruckItem();
            newItem.setTruck(truck);
            newItem.setProduct(cartItem.getProduct());
            newItem.setSum(cartItem.getSum());
            newItem.setQuantity(quantity);

            cartTruckItemRepository.save(newItem);
            truck.getItems().add(newItem);
        }
    }
    public double calculateCartTotal(Cart cart) {
        double total = cart.getCartItems().stream()
                .filter(item -> item.getSum() != null)
                .mapToDouble(item -> item.getQuantity() * item.getSum().getSumma())
                .sum();
        return Math.round(total * 100.0) / 100.0;
    }
    @Transactional
    public void updateQuantity(Long productId, Client client, int delta, Long addressId) {
        Cart cart = findByClient(client);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        // Находим товар в корзине
        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        // Обновляем количество
        int newQuantity = item.getQuantity() + delta;
        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        }

        // Если указан адрес, обновляем цены
        if (addressId != null) {
            updateCartItemPrices(cart, addressId);
        }

        // Перераспределяем товары по машинам
        cart.distributeItemsToTrucks();
        save(cart);
    }

    public Map<String, PriceInfo> updateCartItemPrices(Cart cart, Long addressId) {
        Map<String, PriceInfo> priceInfoMap = new HashMap<>();

        if (addressId == null) {
            return priceInfoMap;
        }

        Optional<Addresses> address = addressesRepository.findById(addressId);
        if (address.isEmpty() || address.get().getClientsRegion() == null) {
            return priceInfoMap;
        }

        String regionName = address.get().getClientsRegion().getRegion().getName();

        // Обновляем цены для каждого товара в корзине
        for (CartItem item : cart.getCartItems()) {
            // Находим цену для этого товара в регионе доставки
            Optional<Sum> regionPrice = sumRepository.findByProductAndRegionName(item.getProduct(), regionName);
            if (regionPrice.isPresent()) {
                Sum sum = regionPrice.get();
                cart.updateItemPrice(item, sum);

                // Сохраняем информацию о цене для ответа
                PriceInfo priceInfo = new PriceInfo();
                priceInfo.setPrice(sum.getSumma());
                priceInfo.setSumId(sum.getId());
                priceInfoMap.put(item.getProduct().getId().toString(), priceInfo);
            }
        }

        save(cart);
        return priceInfoMap;
    }

    public static class PriceInfo {
        private double price;
        private Long sumId;

        // Геттеры и сеттеры
        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public Long getSumId() {
            return sumId;
        }

        public void setSumId(Long sumId) {
            this.sumId = sumId;
        }
    }
}
