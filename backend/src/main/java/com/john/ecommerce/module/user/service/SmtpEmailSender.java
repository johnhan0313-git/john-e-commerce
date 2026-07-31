package com.john.ecommerce.module.user.service;

import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.config.AppProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

/**
 * SMTP 发信，配置对齐 john-ip-studio（SSL:465 / 可选 STARTTLS）。
 */
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    @Override
    public void send(String to, String subject, String body) {
        AppProperties.Mail mail = appProperties.getMail();
        if (!StringUtils.hasText(mail.getHost())) {
            throw new BizException(503, "邮件服务(SMTP)未配置,无法下发验证码,请联系管理员");
        }
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            String from = StringUtils.hasText(mail.getFrom()) ? mail.getFrom() : mail.getUsername();
            if (!StringUtils.hasText(from)) {
                throw new BizException(503, "邮件服务(SMTP)发件人未配置");
            }
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            javaMailSender.send(message);
            log.info("[smtp] sent to={} subject={}", to, subject);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("[smtp] send failed to={} subject={}", to, subject, e);
            throw new BizException("邮件发送失败，请稍后重试");
        }
    }
}
