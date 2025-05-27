package ru.zaomurom.applicationzao.models.prices;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.client.Client;

@Entity
@Table(name = "clients_region")
public class ClientsRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    // Constructors
    public ClientsRegion() {}

    public ClientsRegion(Client client, Region region) {
        this.client = client;
        this.region = region;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}