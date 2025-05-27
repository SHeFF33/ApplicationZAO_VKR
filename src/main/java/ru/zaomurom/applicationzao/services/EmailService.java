package ru.zaomurom.applicationzao.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.Contacts;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.order.Order;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        mailSender.send(message);
    }

    public void sendOrderStatusUpdateEmail(String to, Order order, String newStatus) throws MessagingException {
        var formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        String orderDate = formatter.format(order.getOrderDate());
        String subject = "Статус вашего заказа от " + orderDate + " изменен!";
        String text = "Статус вашего заказа от " + orderDate + " изменен на: " + newStatus;
        sendEmail(to, subject, text);
    }
    @Async
    public void sendOrderStatusUpdateEmailAsync(Order order, String newStatus) {
        Client client = order.getClient();
        if (client != null) {
            try {

                Hibernate.initialize(client.getContacts());

                for (Contacts contact : client.getContacts()) {
                    if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
                        sendOrderStatusUpdateEmail(contact.getEmail(), order, newStatus);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка при отправке email: " + e.getMessage());
            }
        }
    }


    @Async
    public void sendNewOrderNotification(Order order, String clientName) {
        List<User> admins = userService.findAllAdminsWithEmail();
        if (admins.isEmpty()) return;

        String currentDateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));
        String subject = "Новый заказ от " + currentDateTime;
        String text = "Получен новый заказ от " + currentDateTime +
                "<br>Клиент: " + clientName;

        admins.forEach(admin -> {
            try {
                sendEmail(admin.getEmail(), subject, text);
            } catch (MessagingException e) {
                // Логируем ошибку, но не прерываем выполнение
                System.err.println("Ошибка при отправке email администратору " +
                        admin.getEmail() + ": " + e.getMessage());
            }
        });
    }
}