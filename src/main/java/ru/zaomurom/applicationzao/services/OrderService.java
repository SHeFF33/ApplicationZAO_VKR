package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.order.OrderStatusHistory;
import ru.zaomurom.applicationzao.models.order.TCHOrder;
import ru.zaomurom.applicationzao.repositories.OrderRepository;
import ru.zaomurom.applicationzao.repositories.OrderStatusHistoryRepository;
import ru.zaomurom.applicationzao.repositories.TchOrderRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TchOrderRepository tchOrderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<OrderStatusHistory> getStatusHistory(Order order) {
        return orderStatusHistoryRepository.findByOrder(order);
    }

    public List<Order> findByClientAndStatus(Client client, String status) {
        Specification<Order> spec = Specification.where((root, query, cb) -> cb.equal(root.get("client"), client));
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return orderRepository.findAll(spec);
    }

    public List<Order> findByClient(Client client) {
        return orderRepository.findByClient(client);
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

        return orderRepository.findAll(spec);
    }

    public void addStatusHistory(Order order, String status, User user) {
        OrderStatusHistory history = new OrderStatusHistory(order, status, new Date(), user);
        orderStatusHistoryRepository.save(history);
    }

    public List<OrderStatusHistory> findStatusHistoryByFilters(String status, LocalDateTime startDate, LocalDateTime endDate) {
        Specification<OrderStatusHistory> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("changeDate"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("changeDate"), endDate));
        }

        return orderStatusHistoryRepository.findAll(spec);
    }
    public TCHOrder findTchOrderById(Long id) {
        return tchOrderRepository.findById(id).orElse(null);
    }

    public void saveTchOrder(TCHOrder tchOrder) {
        tchOrderRepository.save(tchOrder);
    }
}
