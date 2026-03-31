package stud.ntnu.no.fullstack_project.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending account verification emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationEmailService {

  private final JavaMailSender mailSender;

  @Value("${app.mail.enabled:false}")
  private boolean mailEnabled;

  @Value("${app.mail.from}")
  private String fromAddress;

  @Value("${spring.mail.username:}")
  private String smtpUsername;

  @Value("${spring.mail.password:}")
  private String smtpPassword;

  /**
   * Sends the verification email for a new account.
   *
   * @param recipientEmail verified recipient address
   * @param verificationLink verification URL to include in the email
   */
  public void sendVerificationEmail(String recipientEmail, String verificationLink) {
    logMailConfiguration("verification");

    if (!mailEnabled) {
      log.info(
          "Verification email sending is disabled. recipient={} verificationLink={}",
          recipientEmail,
          verificationLink
      );
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipientEmail);
    message.setSubject("Verify your CheckMate account");
    message.setText("""
        Welcome to CheckMate.

        Verify your email address before logging in by opening this link:
        %s

        If you did not create this account, you can ignore this message.
        """.formatted(verificationLink));

    try {
      mailSender.send(message);
      log.info("Sent verification email to {}", recipientEmail);
    } catch (MailException exception) {
      log.error("Failed to send verification email to {}", recipientEmail, exception);
      throw new IllegalStateException("Failed to send verification email");
    }
  }

  /**
   * Sends a one-time login code to a verified email address.
   *
   * @param recipientEmail recipient email
   * @param code six-digit login code
   */
  public void sendLoginCodeEmail(String recipientEmail, String code) {
    logMailConfiguration("login-code");

    if (!mailEnabled) {
      log.info("Login code email sending is disabled. recipient={} code={}", recipientEmail, code);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipientEmail);
    message.setSubject("Your CheckMate login code");
    message.setText("""
        Use this one-time code to sign in to CheckMate:

        %s

        The code expires in 10 minutes.
        """.formatted(code));

    try {
      mailSender.send(message);
      log.info("Sent login code email to {}", recipientEmail);
    } catch (MailException exception) {
      log.error("Failed to send login code email to {}", recipientEmail, exception);
      throw new IllegalStateException("Failed to send login code email");
    }
  }

  /**
   * Sends the initial account setup email for an invited organization admin.
   *
   * @param recipientEmail recipient email
   * @param setupLink one-time frontend setup link
   * @param organizationName organization the admin will manage
   */
  public void sendAdminSetupEmail(String recipientEmail, String setupLink, String organizationName) {
    logMailConfiguration("admin-setup");

    if (!mailEnabled) {
      log.info(
          "Admin setup email sending is disabled. recipient={} organization={} setupLink={}",
          recipientEmail,
          organizationName,
          setupLink
      );
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipientEmail);
    message.setSubject("Set up your CheckMate admin account");
    message.setText("""
        You have been invited as an administrator for %s.

        Open this one-time link to set your password and activate your account:
        %s

        If you were not expecting this invitation, you can ignore this message.
        """.formatted(organizationName, setupLink));

    try {
      mailSender.send(message);
      log.info("Sent admin setup email to {}", recipientEmail);
    } catch (MailException exception) {
      log.error("Failed to send admin setup email to {}", recipientEmail, exception);
      throw new IllegalStateException("Failed to send admin setup email");
    }
  }

  /**
   * Logs the currently active mail configuration for a specific email flow.
   *
   * <p>This is used to diagnose local and deployed mail problems without
   * logging the SMTP password value itself.</p>
   *
   * @param flow logical email flow name, such as verification or admin setup
   */
  private void logMailConfiguration(String flow) {
    boolean passwordPresent = smtpPassword != null && !smtpPassword.isBlank();
    int passwordLength = passwordPresent ? smtpPassword.length() : 0;

    log.info(
        "Mail configuration for {} flow: enabled={} from={} smtpUsername={} passwordPresent={} passwordLength={}",
        flow,
        mailEnabled,
        fromAddress,
        smtpUsername,
        passwordPresent,
        passwordLength
    );
  }
}
