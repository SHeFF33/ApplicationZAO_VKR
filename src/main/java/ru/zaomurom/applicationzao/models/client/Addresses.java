        package ru.zaomurom.applicationzao.models.client;

        import jakarta.persistence.*;

        @Entity
        @Table(name = "addresses")
        public class Addresses {
            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;
            private int postalcode;
            private String country;
            private String region;
            private String rayon;
            private String city;
            private String street;
            private String home;
            private String roomnumber;

            @ManyToOne
            @JoinColumn(name = "client_id")
            private Client client;

            @ManyToOne
            @JoinColumn(name = "contact_id")
            private Contacts contact;

            public Addresses() {}

            public Addresses(int postalcode, String country, String region, String rayon, String city, String street, String home, String roomnumber) {
                this.postalcode = postalcode;
                this.country = country;
                this.region = region;
                this.rayon = rayon;
                this.city = city;
                this.street = street;
                this.home = home;
                this.roomnumber = roomnumber;
            }

            public Contacts getContact() {
                return contact;
            }

            public void setContact(Contacts contact) {
                this.contact = contact;
            }

            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public int getPostalcode() {
                return postalcode;
            }

            public void setPostalcode(int postalcode) {
                this.postalcode = postalcode;
            }

            public String getCountry() {
                return country;
            }

            public void setCountry(String country) {
                this.country = country;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getRayon() {
                return rayon;
            }

            public void setRayon(String rayon) {
                this.rayon = rayon;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getStreet() {
                return street;
            }

            public void setStreet(String street) {
                this.street = street;
            }

            public String getHome() {
                return home;
            }

            public void setHome(String home) {
                this.home = home;
            }

            public String getRoomnumber() {
                return roomnumber;
            }

            public void setRoomnumber(String roomnumber) {
                this.roomnumber = roomnumber;
            }

            public Client getClient() {
                return client;
            }

            public void setClient(Client client) {
                this.client = client;
            }
        }
