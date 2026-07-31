package com.john.ecommerce.module.user.service;

import lombok.extern.slf4j.Slf4j;

/** 未配置 SMTP 时的回退（测试 / 本地无 SMTP_HOST）。 */
@Slf4j
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[email] to={} subject={} body={}", to, subject, body);
    }
}
