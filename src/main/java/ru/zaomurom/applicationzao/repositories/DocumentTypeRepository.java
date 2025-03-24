package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.order.DocumentType;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
}
