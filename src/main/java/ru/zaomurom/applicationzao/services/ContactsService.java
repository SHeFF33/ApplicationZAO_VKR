package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.client.Addresses;
import ru.zaomurom.applicationzao.models.client.Contacts;
import ru.zaomurom.applicationzao.repositories.ContactsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ContactsService {
    @Autowired
    private ContactsRepository contactsRepository;

    public Contacts save(Contacts contact) {
        return contactsRepository.save(contact);
    }

    public List<Contacts> findAll() {
        return contactsRepository.findAll();
    }

    public Optional<Contacts> findById(Long id) {
        return contactsRepository.findById(id);
    }
}