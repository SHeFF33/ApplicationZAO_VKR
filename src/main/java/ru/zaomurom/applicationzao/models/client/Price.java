package ru.zaomurom.applicationzao.models.client;

import jakarta.persistence.*;
import ru.zaomurom.applicationzao.models.product.Sum;

import java.util.List;

@Entity
@Table(name = "price")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String vid;
    private String name;

    @OneToMany(mappedBy = "selectedPrice")
    private List<Client> clients;

    @OneToMany(mappedBy = "price")
    private List<Sum> sums;

    public Price() {}

    public Price(String vid, String name) {
        this.vid = vid;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Sum> getSums() {
        return sums;
    }

    public void setSums(List<Sum> sums) {
        this.sums = sums;
    }
}
