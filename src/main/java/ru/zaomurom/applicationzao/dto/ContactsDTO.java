package ru.zaomurom.applicationzao.dto;

public class ContactsDTO {
    private Long id;
    private String contactType;
    private String name;
    private String phonenumber;
    private String email;
    private Long clientId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}