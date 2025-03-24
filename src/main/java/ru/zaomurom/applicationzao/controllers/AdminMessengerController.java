package ru.zaomurom.applicationzao.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.messenger.Conversation;
import ru.zaomurom.applicationzao.services.MessengerService;
import ru.zaomurom.applicationzao.services.UserService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/messenger")
public class AdminMessengerController {
    @Autowired
    private MessengerService messengerService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String messenger(Model model, Principal principal) {
        User admin = getUserFromPrincipal(principal);
        model.addAttribute("openConversations", messengerService.getOpenConversations());
        model.addAttribute("myConversations", messengerService.getAdminConversations(admin));
        return "admin/messenger";
    }

    @GetMapping("/{id}")
    public String viewConversation(@PathVariable Long id, Model model) {
        Conversation conversation = messengerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        model.addAttribute("conversation", conversation);
        return "admin/conversation";
    }

    @PostMapping("/reply/{conversationId}")
    public String replyToConversation(
            @PathVariable Long conversationId,
            @RequestParam String message,
            @RequestParam(required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        User admin = getUserFromPrincipal(principal);
        messengerService.addMessageToConversation(conversationId, admin, message, files);
        return "redirect:/admin/messenger/" + conversationId;
    }

    @PostMapping("/close/{conversationId}")
    public String closeConversation(@PathVariable Long conversationId) {
        messengerService.closeConversation(conversationId);
        return "redirect:/admin/messenger";
    }

    private User getUserFromPrincipal(Principal principal) {
        return userService.findByUsername(principal.getName());
    }
}