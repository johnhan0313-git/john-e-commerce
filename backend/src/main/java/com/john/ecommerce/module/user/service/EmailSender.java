package com.john.ecommerce.module.user.service;

/**
 * 发信抽象；默认实现仅打日志，后续可换 SMTP/第三方。
 */
public interface EmailSender {
    void send(String to, String subject, String body);
}
