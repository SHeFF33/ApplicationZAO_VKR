package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.messenger.*;
import ru.zaomurom.applicationzao.repositories.AttachmentRepository;
import ru.zaomurom.applicationzao.repositories.ConversationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessengerService {
    @Autowired
    private ConversationRepository conversationRepository;

    public List<Conversation> getOpenConversations() {
        return conversationRepository.findOpenConversations();
    }

    public List<Conversation> getAdminConversations(User admin) {
        return conversationRepository.findAdminConversations(admin);
    }

    // Поиск обращения по ID
    public Optional<Conversation> findById(Long id) {
        return conversationRepository.findById(id);
    }

    // Добавление сообщения в обращение
    public void addMessageToConversation(Long conversationId, User sender, String text, List<MultipartFile> files) throws IOException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Если это первый ответ администратора, назначаем его ответственным
        if (conversation.getAdmin() == null && sender.isAdmin()) {
            conversation.setAdmin(sender);
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                Attachment attachment = new Attachment();
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileType(file.getContentType());
                attachment.setFileData(file.getBytes());
                attachment.setMessage(message);
                message.getAttachments().add(attachment);
            }
        }

        conversation.getMessages().add(message);
        conversationRepository.save(conversation);
    }

    // Закрытие обращения
    public void closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        conversation.setClosed(true);
        conversationRepository.save(conversation);
    }

        @Autowired
        private AttachmentRepository attachmentRepository;

        // Получение всех обращений клиента
        public List<Conversation> getClientConversations(Client client) {
            return conversationRepository.findByClient(client);
        }

        // Получение обращения по ID
        public Conversation getConversationById(Long id) {
            return conversationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

        // Создание нового обращения
        public Conversation startNewConversation(Client client, String subject, String messageText,
                                                 List<MultipartFile> files) throws IOException {
            Conversation conversation = new Conversation();
            conversation.setClient(client);
            conversation.setSubject(subject);
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setClosed(false);

            // Первое сообщение от клиента
            User clientUser = client.getUsers().isEmpty() ? null : client.getUsers().get(0);
            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(clientUser);
            message.setText(messageText);
            message.setSentAt(LocalDateTime.now());

            // Обработка вложений
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    Attachment attachment = new Attachment();
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setFileType(file.getContentType());
                    attachment.setFileData(file.getBytes());
                    attachment.setMessage(message);
                    message.getAttachments().add(attachment);
                }
            }

            conversation.getMessages().add(message);
            return conversationRepository.save(conversation);
        }



        // Получение вложения по ID
        public Attachment getAttachmentById(Long id) {
            return attachmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        }
}