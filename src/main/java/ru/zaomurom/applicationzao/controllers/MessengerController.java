// MessengerController.java
package ru.zaomurom.applicationzao.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.messenger.Attachment;
import ru.zaomurom.applicationzao.models.messenger.Conversation;
import ru.zaomurom.applicationzao.services.ClientService;
import ru.zaomurom.applicationzao.services.MessengerService;
import ru.zaomurom.applicationzao.services.UserService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/messenger")
public class MessengerController {
    @Autowired
    private MessengerService messengerService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClientService clientService;

    @GetMapping
    public String messenger(Model model, Principal principal) {
        Client client = getClientFromPrincipal(principal);
        model.addAttribute("conversations", messengerService.getClientConversations(client));
        return "messenger";
    }

    @GetMapping("/{id}")
    public String viewConversation(@PathVariable Long id, Model model, Principal principal) {
        Client client = getClientFromPrincipal(principal);
        Conversation conversation = messengerService.getConversationById(id);

        if (client == null || conversation.getClient() == null ||
                !client.getId().equals(conversation.getClient().getId())) {
            return "redirect:/messenger";
        }

        model.addAttribute("conversation", conversation);
        return "conversation";
    }

    @PostMapping("/start")
    public String startConversation(
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam(required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        if (files != null) {
            for (MultipartFile file : files) {
                validateFile(file);
            }
        }

        Client client = getClientFromPrincipal(principal);
        User user = getUserFromPrincipal(principal);
        messengerService.startNewConversation(client, user, subject, message, files);
        return "redirect:/messenger";
    }

    @PostMapping("/reply/{conversationId}")
    public String replyToConversation(
            @PathVariable Long conversationId,
            @RequestParam String message,
            @RequestParam(required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        if (files != null) {
            for (MultipartFile file : files) {
                validateFile(file);
            }
        }

        User user = getUserFromPrincipal(principal);
        messengerService.addMessageToConversation(conversationId, user, message, files);
        return "redirect:/messenger/" + conversationId;
    }

    @GetMapping("/attachment/{id}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable Long id) {
        Attachment attachment = messengerService.getAttachmentById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(attachment.getFileType()));

        String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        if (attachment.getFileType().startsWith("image/")) {
            headers.add("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
        } else {
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        }

        return new ResponseEntity<>(attachment.getFileData(), headers, HttpStatus.OK);
    }

    private Client getClientFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }

        User user = userService.findByUsername(principal.getName());
        return user != null ? user.getClient() : null;
    }

    private User getUserFromPrincipal(Principal principal) {
        return userService.findByUsername(principal.getName());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return;

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("jpeg", "jpg", "png", "pdf", "doc", "docx");

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Недопустимый формат файла. Разрешены: " + String.join(", ", allowedExtensions));
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new IllegalArgumentException("Файл слишком большой. Максимальный размер - 5MB");
        }
    }
}