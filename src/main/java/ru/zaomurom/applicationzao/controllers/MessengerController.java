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
import java.security.Principal;
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

        if (conversation.getClient().getId().equals(client.getId())) {
            model.addAttribute("conversation", conversation);
            return "conversation";
        }
        return "redirect:/messenger";
    }

    @PostMapping("/start")
    public String startConversation(
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam(required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        Client client = getClientFromPrincipal(principal);
        messengerService.startNewConversation(client, subject, message, files);
        return "redirect:/messenger";
    }

    @PostMapping("/reply/{conversationId}")
    public String replyToConversation(
            @PathVariable Long conversationId,
            @RequestParam String message,
            @RequestParam(required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        User user = getUserFromPrincipal(principal);
        messengerService.addMessageToConversation(conversationId, user, message, files);
        return "redirect:/messenger/" + conversationId;
    }

    @GetMapping("/attachment/{id}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable Long id) {
        Attachment attachment = messengerService.getAttachmentById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(attachment.getFileType()));
        headers.setContentDispositionFormData("attachment", attachment.getFileName());

        return new ResponseEntity<>(attachment.getFileData(), headers, HttpStatus.OK);
    }

    private Client getClientFromPrincipal(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return user.getClient();
    }

    private User getUserFromPrincipal(Principal principal) {
        return userService.findByUsername(principal.getName());
    }
}