package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.DocumentZAO;

public interface DocumentZAORepository extends JpaRepository<DocumentZAO, Long> {
}

