package ru.zaomurom.applicationzao.dto;

public class AddressesDTO {
    private Long id;
    private int postalcode;
    private String country;
    private String region;
    private String rayon;
    private String city;
    private String street;
    private String home;
    private String roomnumber;
    private String schedule;
    private Long clientId;
    private Long contactId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getPostalcode() { return postalcode; }
    public void setPostalcode(int postalcode) { this.postalcode = postalcode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
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
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
}