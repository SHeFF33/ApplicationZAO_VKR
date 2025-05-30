package ru.zaomurom.applicationzao.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ClientDTO {
    private Long id;
    private String name;
    private String inn;
    private String kpp;
    private String uraddress;
    private String factaddress;
    private Double sum1;
    private Double sum2;
    private List<AddressesDTO> addresses;
    private List<ContactsDTO> contacts;
    private List<UserDTO> users;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }
    public String getKpp() { return kpp; }
    public void setKpp(String kpp) { this.kpp = kpp; }
    public String getUraddress() { return uraddress; }
    public void setUraddress(String uraddress) { this.uraddress = uraddress; }
    public String getFactaddress() { return factaddress; }
    public void setFactaddress(String factaddress) { this.factaddress = factaddress; }
    public Double getSum1() { return sum1; }
    public void setSum1(Double sum1) { this.sum1 = sum1; }
    public Double getSum2() { return sum2; }
    public void setSum2(Double sum2) { this.sum2 = sum2; }
    public List<AddressesDTO> getAddresses() { return addresses; }
    public void setAddresses(List<AddressesDTO> addresses) { this.addresses = addresses; }
    public List<ContactsDTO> getContacts() { return contacts; }
    public void setContacts(List<ContactsDTO> contacts) { this.contacts = contacts; }
    public List<UserDTO> getUsers() { return users; }
    public void setUsers(List<UserDTO> users) { this.users = users; }
}