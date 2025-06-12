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

    public List<Conversation> getAllAdminConversations(User admin) {
        return conversationRepository.findByAdmin(admin);
    }

    public void reopenConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        conversation.setClosed(false);
        conversationRepository.save(conversation);
    }
    public Optional<Conversation> findById(Long id) {
        return conversationRepository.findById(id);
    }

    public void addMessageToConversation(Long conversationId, User sender, String text, List<MultipartFile> files) throws IOException {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (sender.isAdmin() && conversation.getAdmin() == null) {
            conversation.setAdmin(sender);
            conversation.setClosed(false);
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    Attachment attachment = new Attachment();
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setFileType(file.getContentType());
                    attachment.setFileData(file.getBytes());
                    attachment.setMessage(message);
                    message.getAttachments().add(attachment);
                }
            }
        }

        conversation.getMessages().add(message);
        
        if (sender.isAdmin()) {
            conversation.setClosed(false);
        }
        
        conversationRepository.save(conversation);
    }

    public void closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        conversation.setClosed(true);
        conversationRepository.save(conversation);
    }

        @Autowired
        private AttachmentRepository attachmentRepository;

        public List<Conversation> getClientConversations(Client client) {
            return conversationRepository.findByClient(client);
        }

        public Conversation getConversationById(Long id) {
            return conversationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

    public Conversation startNewConversation(Client client, User sender, String subject, String messageText,
                                             List<MultipartFile> files) throws IOException {
        Conversation conversation = new Conversation();
        conversation.setClient(client);
        conversation.setSubject(subject);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setClosed(false);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setText(messageText);
        message.setSentAt(LocalDateTime.now());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    Attachment attachment = new Attachment();
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setFileType(file.getContentType());
                    attachment.setFileData(file.getBytes());
                    attachment.setMessage(message);
                    message.getAttachments().add(attachment);
                }
            }
        }

        conversation.getMessages().add(message);
        return conversationRepository.save(conversation);
    }

        public Attachment getAttachmentById(Long id) {
            return attachmentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        }
}