package com.urlshortener.auth.service;

import com.urlshortener.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    /**
     * Sends a password reset email containing the 6-digit reset code.
     * The raw token is NEVER logged. Only the recipient email is logged for audit.
     *
     * @param toEmail   the recipient's email address
     * @param rawToken  the 6-digit plaintext reset code (never stored, never logged)
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String rawToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(appProperties.getMail().getFrom());
            helper.setTo(toEmail);
            helper.setSubject("ZipLink Password Reset Code");
            helper.setText(buildHtmlEmail(rawToken), true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildHtmlEmail(String rawToken) {
        // Token digits are split for the styled display boxes
        String d1 = String.valueOf(rawToken.charAt(0));
        String d2 = String.valueOf(rawToken.charAt(1));
        String d3 = String.valueOf(rawToken.charAt(2));
        String d4 = String.valueOf(rawToken.charAt(3));
        String d5 = String.valueOf(rawToken.charAt(4));
        String d6 = String.valueOf(rawToken.charAt(5));

        return "<!DOCTYPE html>" +
               "<html lang=\"en\">" +
               "<head>" +
               "  <meta charset=\"UTF-8\">" +
               "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
               "  <title>ZipLink Password Reset</title>" +
               "</head>" +
               "<body style=\"margin:0;padding:0;background-color:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;\">" +
               "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f4f6f9;padding:40px 20px;\">" +
               "    <tr>" +
               "      <td align=\"center\">" +
               "        <table width=\"560\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width:560px;width:100%;\">" +
               "          <!-- Header -->" +
               "          <tr>" +
               "            <td style=\"background-color:#0d6efd;border-radius:12px 12px 0 0;padding:32px 40px;text-align:center;\">" +
               "              <span style=\"font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.5px;\">&#128279; ZipLink</span>" +
               "            </td>" +
               "          </tr>" +
               "          <!-- Body -->" +
               "          <tr>" +
               "            <td style=\"background-color:#ffffff;padding:40px;border-radius:0 0 12px 12px;box-shadow:0 4px 24px rgba(0,0,0,0.08);\">" +
               "              <h2 style=\"margin:0 0 8px 0;font-size:22px;font-weight:700;color:#1a1a2e;\">Reset Your Password</h2>" +
               "              <p style=\"margin:0 0 24px 0;color:#6c757d;font-size:15px;line-height:1.6;\">" +
               "                We received a request to reset your ZipLink password. Use the 6-digit code below to complete the reset." +
               "              </p>" +
               "              <!-- Token display -->" +
               "              <div style=\"background-color:#f8f9ff;border:2px solid #e8eaff;border-radius:12px;padding:28px;text-align:center;margin-bottom:24px;\">" +
               "                <p style=\"margin:0 0 16px 0;font-size:13px;font-weight:600;color:#6c757d;text-transform:uppercase;letter-spacing:1px;\">Your password reset code</p>" +
               "                <div style=\"display:inline-flex;gap:8px;\">" +
               "                  <span style=\"display:inline-block;width:44px;height:56px;line-height:56px;background:#0d6efd;color:#fff;font-size:26px;font-weight:800;border-radius:8px;text-align:center;\">" + d1 + "</span>" +
               "                  <span style=\"display:inline-block;width:44px;height:56px;line-height:56px;background:#0d6efd;color:#fff;font-size:26px;font-weight:800;border-radius:8px;text-align:center;\">" + d2 + "</span>" +
               "                  <span style=\"display:inline-block;width:44px;height:56px;line-height:56px;background:#0d6efd;color:#fff;font-size:26px;font-weight:800;border-radius:8px;text-align:center;\">" + d3 + "</span>" +
               "                  <span style=\"display:inline-block;width:44px;height:56px;line-height:56px;background:#0d6efd;color:#fff;font-size:26px;font-weight:800;border-radius:8px;text-align:center;\">" + d4 + "</span>" +
               "                  <span style=\"display:inline-block;width:44px;height:56px;line-height:56px;background:#0d6efd;color:#fff;font-size:26px;font-weight:800;border-radius:8px;text-align:center;\">" + d5 + "</span>" +
               "                  <span style=\"display:inline-block;width:44px;height:56px;line-height:56px;background:#0d6efd;color:#fff;font-size:26px;font-weight:800;border-radius:8px;text-align:center;\">" + d6 + "</span>" +
               "                </div>" +
               "                <p style=\"margin:16px 0 0 0;font-size:13px;color:#6c757d;\">&#128336; This code expires in <strong>15 minutes</strong></p>" +
               "              </div>" +
               "              <p style=\"margin:0 0 32px 0;color:#6c757d;font-size:14px;line-height:1.6;\">" +
               "                If you did not request a password reset, you can safely ignore this email. Your password will not change." +
               "              </p>" +
               "              <hr style=\"border:none;border-top:1px solid #e9ecef;margin:0 0 24px 0;\">" +
               "              <p style=\"margin:0;font-size:12px;color:#adb5bd;text-align:center;\">" +
               "                This email was sent by ZipLink &bull; Do not reply to this email" +
               "              </p>" +
               "            </td>" +
               "          </tr>" +
               "        </table>" +
               "      </td>" +
               "    </tr>" +
               "  </table>" +
               "</body>" +
               "</html>";
    }
}
