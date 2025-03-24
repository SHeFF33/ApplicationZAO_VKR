package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.client.Contacts;

public interface ContactsRepository extends JpaRepository<Contacts, Long> {
}