package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.model.Notification;
import com.nomina.nomina_portal_service.model.Trademark;
import com.nomina.nomina_portal_service.model.User;
import com.nomina.nomina_portal_service.repository.NotificationRepositoryJdbc;
import com.nomina.nomina_portal_service.repository.TrademarkRepositoryJdbc;
import com.nomina.nomina_portal_service.repository.UserRepositoryJdbc;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrademarkDeadlineNotificationScheduler {
	private static final Logger log = LoggerFactory.getLogger(TrademarkDeadlineNotificationScheduler.class);
	private static final Set<Integer> SUPPORTED_DEADLINE_TYPES = Set.of(7, 14, 30, 180, 360);
	private static final String NOTIFICATION_TYPE = "TRADEMARK_DEADLINE";
	private final TrademarkRepositoryJdbc trademarkRepository;
	private final UserRepositoryJdbc userRepository;
	private final NotificationRepositoryJdbc notificationRepository;
	private final JavaMailSender mailSender;
	private final String mailFrom;

	public TrademarkDeadlineNotificationScheduler(
		TrademarkRepositoryJdbc trademarkRepository,
		UserRepositoryJdbc userRepository,
		NotificationRepositoryJdbc notificationRepository,
		ObjectProvider<JavaMailSender> mailSenderProvider,
		@Value("${notifications.mail.from:no-reply@nomina.local}") String mailFrom
	) {
		this.trademarkRepository = trademarkRepository;
		this.userRepository = userRepository;
		this.notificationRepository = notificationRepository;
		this.mailSender = mailSenderProvider.getIfAvailable();
		this.mailFrom = mailFrom;
	}

	@Scheduled(
		cron = "${notifications.trademark.cron:0 0 9 * * *}",
		zone = "${notifications.trademark.zone:UTC}"
	)
	public void checkTrademarkDeadlinesAndSendNotifications() {
		LocalDate today = LocalDate.now();
		List<User> users = userRepository.findAll();
		if (users.isEmpty()) {
			return;
		}

		for (Trademark trademark : trademarkRepository.findAll()) {
			Integer deadlineType = resolveDeadlineType(today, trademark.renewalDate());
			if (deadlineType == null) {
				continue;
			}
			if (trademark.createdByUser() == null) {
				continue;
			}

			String body = buildBody(trademark.trademarkName(), deadlineType);
			boolean alreadyExists = notificationRepository.existsDeadlineNotification(
				NOTIFICATION_TYPE,
				trademark.createdByUser(),
				body,
				today,
				trademark.renewalDate(),
				deadlineType
			);
			if (alreadyExists) {
				continue;
			}

			boolean sentToAll = sendToAllUsers(users, trademark.trademarkName(), body);
			if (!sentToAll) {
				continue;
			}

			notificationRepository.insert(new Notification(
				null,
				NOTIFICATION_TYPE,
				trademark.createdByUser(),
				body,
				today,
				trademark.renewalDate(),
				deadlineType,
				Boolean.FALSE
			));
		}
	}

	private Integer resolveDeadlineType(LocalDate today, LocalDate renewalDate) {
		if (renewalDate == null) {
			return null;
		}
		long days = ChronoUnit.DAYS.between(today, renewalDate);
		if (days < 0 || days > Integer.MAX_VALUE) {
			return null;
		}
		int daysLeft = (int) days;
		return SUPPORTED_DEADLINE_TYPES.contains(daysLeft) ? daysLeft : null;
	}

	private String buildBody(String trademarkName, int daysLeft) {
		return "The trademark with the name '" + trademarkName + "' has " + daysLeft + " days left until its deadline.";
	}

	private boolean sendToAllUsers(List<User> users, String trademarkName, String body) {
		if (mailSender == null) {
			log.warn("JavaMailSender bean is not configured. Skipping trademark deadline email sending.");
			return false;
		}

		String subject = "Trademark Deadline Reminder - " + trademarkName;
		boolean sentAtLeastOne = false;
		for (User user : users) {
			if (user.getUsername() == null || user.getUsername().isBlank()) {
				log.warn("Skipping notification email for user {} due to missing username email.", user.getId());
				continue;
			}

			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(mailFrom);
				message.setTo(user.getUsername());
				message.setSubject(subject);
				message.setText(body);
				mailSender.send(message);
				sentAtLeastOne = true;
			} catch (Exception ex) {
				log.error("Failed to send trademark deadline email to {}", user.getUsername(), ex);
				// Keep sending to the remaining users.
			}
		}
		return sentAtLeastOne;
	}
}
