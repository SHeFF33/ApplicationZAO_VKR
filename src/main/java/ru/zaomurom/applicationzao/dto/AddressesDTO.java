package ru.zaomurom.applicationzao.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressesDTO {
    private Long id;
    private String postalcode;
    private String country;
    private String regionName;
    private String rayon;
    private String city;
    private String street;
    private String home;
    private String roomnumber;
    private String schedule;
    private ContactsDTO contactDTO;
    private Long clientId;
}