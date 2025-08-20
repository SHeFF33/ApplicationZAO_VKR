package ru.zaomurom.applicationzao.models.order;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.client.User;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date changeDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public OrderStatusHistory() {}

    public OrderStatusHistory(Order order, String status, Date changeDate, User user) {
        this.order = order;
        this.status = status;
        this.changeDate = changeDate;
        this.user = user;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFormattedChangeDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return formatter.format(changeDate);
    }
}