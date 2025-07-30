package co.ke.mail.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener; // New import
import org.springframework.transaction.event.TransactionPhase; // New import

import co.ke.finsis.entity.OfficerRegistration;
import co.ke.finsis.repository.OfficerRegistrationRepository;
import co.ke.finsis.events.OfficerCreatedEvent; // New import for the event
import co.ke.mail.beans.Mail;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OfficerRegistrationRepository repository;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public void sendMail(final Mail mail) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(mailFrom);
        mailMessage.setTo("info@capdo.org"); // Consider making this configurable or dynamic
        mailMessage.setBcc("akidamjaffar@gmail.com"); // Consider making this configurable or dynamic
        mailMessage.setSubject("You Have A New Capdo-form Mail from;- " + mail.getFrom());
        mailMessage.setText(mail.toString());

        mailSender.send(mailMessage);
    }

    // This method is now an event listener, triggered AFTER the transaction commits
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async // Still make this method asynchronous to avoid blocking the main thread
    public void handleOfficerCreated(OfficerCreatedEvent event) {
        Long officerId = event.getOfficerId();

        OfficerRegistration officer = repository.findById(officerId)
                .orElse(null);

        if (officer == null) {
            System.err.println("Error: Officer not found for ID: " + officerId + ". Cannot send credentials email.");
            return;
        }

        // IMPORTANT: Ensure officer.getSystemUser() and officer.getSystemUser().getEmail() are not null
        if (officer.getSystemUser() == null || officer.getSystemUser().getEmail() == null) {
            System.err.println("Error: SystemUser or email is missing for officer " + officer.getId() + ". Cannot send credentials email.");
            return;
        }

        String body = """
                Dear %s,

                Your officer account has been created.

                Usermail: %s
                Password: %s

                Please login and change your password after your first login.

                Regards,
                TRES Team
                """.formatted(officer.getFullName(), officer.getSystemUser().getEmail(), "Password@2906"); // Reminder: Generate or fetch a secure password

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(officer.getSystemUser().getEmail());
        mailMessage.setBcc("akidamjaffar@gmail.com");
        mailMessage.setSubject("Your Officer Account Has Been Created");
        mailMessage.setText(body);

        try {
            mailSender.send(mailMessage);
            System.out.println("Credentials email sent successfully to " + officer.getSystemUser().getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send credentials email to " + officer.getSystemUser().getEmail() + ": " + e.getMessage());
            // Log the exception for further investigation in production
        }
    }
}