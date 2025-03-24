package ru.zaomurom.applicationzao.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.order.Order;

import java.text.SimpleDateFormat;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

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
}