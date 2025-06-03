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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Async("asyncExecutor")
    public CompletableFuture<Void> sendEmail(String to, String subject, String text) throws MessagingException {
        try {
            logger.info("Начинаем отправку email на адрес: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            mailSender.send(message);
            logger.info("Email успешно отправлен на адрес: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Ошибка при отправке email на адрес {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    @Async("asyncExecutor")
    public CompletableFuture<Void> sendOrderStatusUpdateEmail(String to, Order order, String newStatus) throws MessagingException {
        try {
            logger.info("Подготовка email об обновлении статуса заказа для: {}", to);
            var formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
            String orderDate = formatter.format(order.getOrderDate());
            String subject = "Статус вашего заказа от " + orderDate + " изменен!";
            
            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append("Статус вашего заказа от ").append(orderDate)
                    .append(" изменен на: ").append(newStatus);

            return sendEmail(to, subject, textBuilder.toString());
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления о статусе заказа на {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    @Async("asyncExecutor")
    public void sendOrderStatusUpdateEmailAsync(Order order, String newStatus, List<String> emailAddresses) {
        logger.info("Начало асинхронной отправки уведомлений о статусе заказа");
        
        if (emailAddresses.isEmpty()) {
            logger.warn("Не найдены email адреса для отправки уведомлений");
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String email : emailAddresses) {
            try {
                logger.info("Отправка уведомления о статусе заказа на адрес: {}", email);
                futures.add(sendOrderStatusUpdateEmail(email, order, newStatus));
            } catch (Exception e) {
                logger.error("Ошибка при отправке уведомления на адрес {}: {}", email, e.getMessage(), e);
            }
        }

        // Ждем завершения всех отправок
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Async("asyncExecutor")
    public void sendNewOrderNotification(Order order, String clientName) {
        try {
            logger.info("Начало отправки уведомлений о новом заказе администраторам");
            List<User> admins = userService.findAllAdminsWithEmail();
            if (admins.isEmpty()) {
                logger.warn("Не найдено администраторов с email для отправки уведомлений");
                return;
            }

            String currentDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));
            String subject = "Новый заказ от " + currentDateTime;
            
            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append("Получен новый заказ от ").append(currentDateTime)
                    .append("<br>Клиент: ").append(clientName);

            if (order.getRelatedOrdersInfo() != null) {
                textBuilder.append("<br><br>").append(order.getRelatedOrdersInfo());
            }

            String text = textBuilder.toString();
            logger.info("Найдено {} администраторов для отправки уведомлений", admins.size());

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            admins.forEach(admin -> {
                try {
                    logger.info("Отправка уведомления о новом заказе администратору: {}", admin.getEmail());
                    futures.add(sendEmail(admin.getEmail(), subject, text));
                } catch (MessagingException e) {
                    logger.error("Ошибка при отправке уведомления администратору {}: {}", 
                            admin.getEmail(), e.getMessage(), e);
                }
            });

            // Ждем завершения всех отправок
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            logger.error("Общая ошибка при отправке уведомлений о новом заказе: {}", e.getMessage(), e);
        }
    }
}