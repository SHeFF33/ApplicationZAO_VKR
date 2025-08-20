package ru.zaomurom.applicationzao.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.client.Addresses;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.order.OrderStatusHistory;
import ru.zaomurom.applicationzao.models.order.OrderTruck;
import ru.zaomurom.applicationzao.models.order.TCHOrder;
import ru.zaomurom.applicationzao.models.product.Cart;
import ru.zaomurom.applicationzao.models.product.CartTruck;
import ru.zaomurom.applicationzao.models.product.CartTruckItem;
import ru.zaomurom.applicationzao.repositories.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Service
public class OrderService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TchOrderRepository tchOrderRepository;

    @Autowired
    private OrderTruckRepository orderTruckRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    public List<Order> findAll() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order save(Order order) {
        // Сохраняем Order
        Order savedOrder = orderRepository.save(order);

        // Сохраняем OrderTruck
        for (OrderTruck truck : order.getTrucks()) {
            truck.setOrder(savedOrder);
            orderTruckRepository.save(truck);
        }

        // Сохраняем TCHOrder
        for (TCHOrder tchOrder : order.getTchOrders()) {
            tchOrder.setOrder(savedOrder);
            tchOrderRepository.save(tchOrder);
        }

        return savedOrder;
    }

    public List<OrderStatusHistory> getStatusHistory(Order order) {
        return orderStatusHistoryRepository.findByOrder(order, Sort.by(Sort.Direction.DESC, "changeDate"));
    }

    public List<Order> findByClientAndStatus(Client client, String status) {
        Specification<Order> spec = Specification.where((root, query, cb) -> cb.equal(root.get("client"), client));
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "orderDate"));
    }

    public List<Order> findByClient(Client client) {
        return orderRepository.findByClient(client, Sort.by(Sort.Direction.DESC, "orderDate"));
    }

    public List<Order> findByFilters(Long clientId, String status, LocalDateTime startDate, LocalDateTime endDate) {
        Specification<Order> spec = Specification.where(null);

        if (clientId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("client").get("id"), clientId));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), endDate));
        }

        return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "orderDate"));
    }

    public void addStatusHistory(Order order, String status, User user) {
        OrderStatusHistory history = new OrderStatusHistory(order, status, new Date(), user);
        orderStatusHistoryRepository.save(history);
    }

    public List<OrderStatusHistory> findStatusHistoryByFilters(Long orderId, Long clientId, String status,
                                                               LocalDateTime startDate, LocalDateTime endDate) {
        Specification<OrderStatusHistory> spec = Specification.where(null);

        if (orderId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("order").get("id"), orderId));
        }

        if (clientId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("order").get("client").get("id"), clientId));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("changeDate"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("changeDate"), endDate));
        }

        return orderStatusHistoryRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "changeDate"));
    }
    
    public TCHOrder findTchOrderById(Long id) {
        return tchOrderRepository.findById(id).orElse(null);
    }

    public void saveTchOrder(TCHOrder tchOrder) {
        tchOrderRepository.save(tchOrder);
    }

    public Optional<Order> findWithTrucksAndTchOrdersById(Long id) {
        Optional<Order> orderWithTrucks = orderRepository.findWithTrucksById(id);
        Optional<Order> orderWithTchOrders = orderRepository.findWithTchOrdersById(id);

        if (orderWithTrucks.isPresent() && orderWithTchOrders.isPresent()) {
            Order order = orderWithTrucks.get();
            order.setTchOrders(orderWithTchOrders.get().getTchOrders());
            return Optional.of(order);
        }
        return Optional.empty();
    }
    
    public double calculateOrderTotal(Order order) {
        double total = order.getTchOrders().stream()
                .mapToDouble(tchOrder -> tchOrder.getQuantity() *
                        tchOrder.getPrice())
                .sum();
        return Math.round(total * 100.0) / 100.0;
    }
    
    public List<Order> findByClientId(Long clientId) {
        return orderRepository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public List<String> collectOrderEmailAddresses(Order order) {
        Set<String> emailAddresses = new HashSet<>();
        Client client = order.getClient();

        // Add emails from client contacts
        if (client.getContacts() != null) {
            client.getContacts().stream()
                .filter(contact -> contact.getEmail() != null && !contact.getEmail().isEmpty())
                .forEach(contact -> emailAddresses.add(contact.getEmail()));
        }

        // Add emails from client users
        if (client.getUsers() != null) {
            client.getUsers().stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty())
                .forEach(user -> emailAddresses.add(user.getEmail()));
        }

        return new ArrayList<>(emailAddresses);
    }
}
