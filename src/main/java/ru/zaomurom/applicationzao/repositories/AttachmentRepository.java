package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.messenger.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
