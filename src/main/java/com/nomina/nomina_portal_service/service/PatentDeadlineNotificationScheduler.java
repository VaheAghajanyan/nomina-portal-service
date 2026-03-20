package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.model.Notification;
import com.nomina.nomina_portal_service.model.Patent;
import com.nomina.nomina_portal_service.model.User;
import com.nomina.nomina_portal_service.repository.NotificationRepositoryJdbc;
import com.nomina.nomina_portal_service.repository.PatentRepositoryJdbc;
import com.nomina.nomina_portal_service.repository.UserRepositoryJdbc;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PatentDeadlineNotificationScheduler {
	private static final Logger log = LoggerFactory.getLogger(PatentDeadlineNotificationScheduler.class);
	private static final Set<Integer> SUPPORTED_DEADLINE_TYPES = Set.of(7, 14, 30, 180, 360);
	private static final String NOTIFICATION_TYPE = "PATENT_DEADLINE";
	private final PatentRepositoryJdbc patentRepository;
	private final UserRepositoryJdbc userRepository;
	private final NotificationRepositoryJdbc notificationRepository;
	private final JavaMailSender mailSender;
	private final String mailFrom;
	private final NotificationEmailTemplateService notificationEmailTemplateService;

	public PatentDeadlineNotificationScheduler(
		PatentRepositoryJdbc patentRepository,
		UserRepositoryJdbc userRepository,
		NotificationRepositoryJdbc notificationRepository,
		NotificationEmailTemplateService notificationEmailTemplateService,
		ObjectProvider<JavaMailSender> mailSenderProvider,
		@Value("${notifications.mail.from:no-reply@nomina.local}") String mailFrom
	) {
		this.patentRepository = patentRepository;
		this.userRepository = userRepository;
		this.notificationRepository = notificationRepository;
		this.notificationEmailTemplateService = notificationEmailTemplateService;
		this.mailSender = mailSenderProvider.getIfAvailable();
		this.mailFrom = mailFrom;
	}

	@Scheduled(
		cron = "${notifications.patent.cron:${notifications.trademark.cron:0 0 9 * * *}}",
		zone = "${notifications.patent.zone:${notifications.trademark.zone:UTC}}"
	)
	public void checkPatentDeadlinesAndSendNotifications() {
		LocalDate today = LocalDate.now();
		List<User> users = userRepository.findAll();
		if (users.isEmpty()) {
			return;
		}

		for (Patent patent : patentRepository.findAll()) {
			Integer deadlineType = resolveDeadlineType(today, patent.validationDeadlines());
			if (deadlineType == null || patent.createdByUser() == null) {
				continue;
			}

			String body = buildBody(patent.title(), deadlineType);
			boolean alreadyExists = notificationRepository.existsDeadlineNotification(
				NOTIFICATION_TYPE,
				patent.createdByUser(),
				body,
				today,
				patent.validationDeadlines(),
				deadlineType
			);
			if (alreadyExists) {
				continue;
			}

			String htmlBody = notificationEmailTemplateService.render(
				"Patent",
				patent.title(),
				patent.patentNumber(),
				patent.assignee(),
				patent.validationDeadlines(),
				deadlineType
			);
			boolean sentToAll = sendToAllUsers(users, patent.title(), body, htmlBody);
			if (!sentToAll) {
				continue;
			}

			notificationRepository.insert(new Notification(
				null,
				NOTIFICATION_TYPE,
				patent.createdByUser(),
				body,
				today,
				patent.validationDeadlines(),
				deadlineType,
				Boolean.FALSE
			));
		}
	}

	private Integer resolveDeadlineType(LocalDate today, LocalDate deadlineDate) {
		if (deadlineDate == null) {
			return null;
		}
		long days = ChronoUnit.DAYS.between(today, deadlineDate);
		if (days < 0 || days > Integer.MAX_VALUE) {
			return null;
		}
		int daysLeft = (int) days;
		return SUPPORTED_DEADLINE_TYPES.contains(daysLeft) ? daysLeft : null;
	}

	private String buildBody(String patentTitle, int daysLeft) {
		return "The patent with the name '" + patentTitle + "' has " + daysLeft + " days left until its deadline.";
	}

	private boolean sendToAllUsers(
		List<User> users,
		String patentTitle,
		String body,
		String htmlBody
	) {
		if (mailSender == null) {
			log.warn("JavaMailSender bean is not configured. Skipping patent deadline email sending.");
			return false;
		}

		String subject = "Patent Deadline Reminder - " + patentTitle;
		boolean sentAtLeastOne = false;
		for (User user : users) {
			if (user.getUsername() == null || user.getUsername().isBlank()) {
				log.warn("Skipping notification email for user {} due to missing username email.", user.getId());
				continue;
			}

			try {
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				helper.setFrom(mailFrom);
				helper.setTo(user.getUsername());
				helper.setSubject(subject);
				helper.setText(htmlBody, true);
				helper.addInline("nomina-logo", new ClassPathResource("email/nomina-logo.png"), "image/png");
				mailSender.send(message);
				sentAtLeastOne = true;
			} catch (Exception ex) {
				log.error("Failed to send patent deadline email to {}", user.getUsername(), ex);
			}
		}
		return sentAtLeastOne;
	}
}
