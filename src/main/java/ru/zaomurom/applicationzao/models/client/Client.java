package ru.zaomurom.applicationzao.models.client;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.product.Cart;

import java.util.List;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String inn;
    private String kpp;
    private String uraddress;
    private String factaddress;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Addresses> addresses;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contacts> contacts;

    @ManyToOne
    @JoinColumn(name = "price_id")
    private Price selectedPrice;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    @OneToOne(mappedBy = "client")
    private Cart cart;

    // Конструкторы, геттеры и сеттеры
    public Client() {}

    public Client(String name, String inn, String kpp, String uraddress, String factaddress) {
        this.name = name;
        this.inn = inn;
        this.kpp = kpp;
        this.uraddress = uraddress;
        this.factaddress = factaddress;
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

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getUraddress() {
        return uraddress;
    }

    public void setUraddress(String uraddress) {
        this.uraddress = uraddress;
    }

    public String getFactaddress() {
        return factaddress;
    }

    public void setFactaddress(String factaddress) {
        this.factaddress = factaddress;
    }

    public List<Addresses> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Addresses> addresses) {
        this.addresses = addresses;
    }

    public List<Contacts> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contacts> contacts) {
        this.contacts = contacts;
    }

    public Price getSelectedPrice() {
        return selectedPrice;
    }

    public void setSelectedPrice(Price selectedPrice) {
        this.selectedPrice = selectedPrice;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
