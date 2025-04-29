package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.messenger.Conversation;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c WHERE c.admin IS NULL AND c.isClosed = false")
    List<Conversation> findOpenConversations();

    @Query("SELECT c FROM Conversation c WHERE c.admin = :admin AND c.isClosed = false")
    List<Conversation> findAdminConversations(@Param("admin") User admin);

    List<Conversation> findByClient(Client client);

    List<Conversation> findByAdmin(User admin);
}