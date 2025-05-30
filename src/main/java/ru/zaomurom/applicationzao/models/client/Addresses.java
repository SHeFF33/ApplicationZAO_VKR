package ru.zaomurom.applicationzao.models.client;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.prices.ClientsRegion;

@Entity
@Table(name = "addresses")
public class Addresses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int postalcode;
    private String country;
    private String rayon;
    private String city;
    private String street;
    private String home;
    private String roomnumber;
    @Column(length = 3000)
    private String schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contacts contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clients_region_id")
    private ClientsRegion clientsRegion;

    // Конструкторы
    public Addresses() {}

    public Addresses(int postalcode, String country, ClientsRegion clientsRegion, String rayon,
                     String city, String street, String home, String roomnumber,
                     String schedule) {
        this.postalcode = postalcode;
        this.country = country;
        this.clientsRegion = clientsRegion;
        this.rayon = rayon;
        this.city = city;
        this.street = street;
        this.home = home;
        this.roomnumber = roomnumber;
        this.schedule = schedule;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPostalcode() { return postalcode; }
    public void setPostalcode(int postalcode) { this.postalcode = postalcode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public ClientsRegion getClientsRegion() { return clientsRegion; }
    public void setClientsRegion(ClientsRegion clientsRegion) { this.clientsRegion = clientsRegion; }

    public String getRayon() { return rayon; }
    public void setRayon(String rayon) { this.rayon = rayon; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHome() { return home; }
    public void setHome(String home) { this.home = home; }

    public String getRoomnumber() { return roomnumber; }
    public void setRoomnumber(String roomnumber) { this.roomnumber = roomnumber; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Contacts getContact() { return contact; }
    public void setContact(Contacts contact) { this.contact = contact; }

    // Удобный метод для получения имени региона
    public String getRegionName() {
        return clientsRegion != null && clientsRegion.getRegion() != null
                ? clientsRegion.getRegion().getName() : "Не указано";
    }
}