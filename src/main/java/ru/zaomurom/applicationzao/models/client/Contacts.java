    package ru.zaomurom.applicationzao.models.client;

    import jakarta.persistence.*;

    @Entity
    @Table(name = "contacts")
    public class Contacts {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String contactType;
        private String name;
        private String phonenumber;
        private String email;

        @ManyToOne
        @JoinColumn(name = "client_id")
        private Client client;

        public Contacts() {}

        public Contacts(String contactType, String name, String phonenumber, String email) {
            this.contactType = contactType;
            this.name = name;
            this.phonenumber = phonenumber;
            this.email = email;
        }

        public String getContactType() {
            return contactType;
        }

        public void setContactType(String contactType) {
            this.contactType = contactType;
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

        public String getPhonenumber() {
            return phonenumber;
        }

        public void setPhonenumber(String phonenumber) {
            this.phonenumber = phonenumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }
    }
