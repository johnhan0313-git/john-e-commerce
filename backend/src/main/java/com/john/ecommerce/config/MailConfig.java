package com.john.ecommerce.config;

import com.john.ecommerce.module.user.service.EmailSender;
import com.john.ecommerce.module.user.service.LoggingEmailSender;
import com.john.ecommerce.module.user.service.SmtpEmailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(AppProperties appProperties) {
        AppProperties.Mail mail = appProperties.getMail();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        if (!StringUtils.hasText(mail.getHost())) {
            // 占位，实际不会通过 SmtpEmailSender 发送
            sender.setHost("localhost");
            sender.setPort(25);
            return sender;
        }
        sender.setHost(mail.getHost());
        sender.setPort(mail.getPort());
        if (StringUtils.hasText(mail.getUsername())) {
            sender.setUsername(mail.getUsername());
            sender.setPassword(mail.getPassword() != null ? mail.getPassword() : "");
        }
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(StringUtils.hasText(mail.getUsername())));
        props.put("mail.smtp.ssl.enable", String.valueOf(mail.isUseSsl()));
        props.put("mail.smtp.starttls.enable", String.valueOf(mail.isUseTls()));
        props.put("mail.smtp.starttls.required", String.valueOf(mail.isUseTls()));
        if (mail.isUseSsl()) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(mail.getPort()));
        }
        return sender;
    }

    @Bean
    @Primary
    public EmailSender emailSender(JavaMailSender javaMailSender, AppProperties appProperties) {
        if (StringUtils.hasText(appProperties.getMail().getHost())) {
            return new SmtpEmailSender(javaMailSender, appProperties);
        }
        return new LoggingEmailSender();
    }
}
