package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.client.Addresses;
import ru.zaomurom.applicationzao.repositories.AddressesRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AddressesService {
    @Autowired
    private AddressesRepository addressesRepository;

    public Addresses save(Addresses address) {
        return addressesRepository.save(address);
    }


    public List<Addresses> findAll() {
        return addressesRepository.findAll();
    }

    public List<Addresses> findByClientId(Long clientId) {
        return addressesRepository.findByClientId(clientId);
    }

    public Optional<Addresses> findById(Long id) {
        return addressesRepository.findById(id);
    }
}
