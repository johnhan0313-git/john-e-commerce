package com.john.ecommerce.module.user.service;

import com.john.ecommerce.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailCodeService {

    private static final String KEY_PREFIX = "auth:email-code:";

    private final StringRedisTemplate stringRedisTemplate;
    private final EmailSender emailSender;

    @Value("${app.auth.code-ttl-seconds:300}")
    private long codeTtlSeconds;

    /** 非空时固定验证码（dev/CI）；生产请置空走随机码 */
    @Value("${app.auth.fixed-code:123456}")
    private String fixedCode;

    public void sendLoginCode(String email) {
        String normalized = normalize(email);
        String code = StringUtils.hasText(fixedCode)
                ? fixedCode.trim()
                : String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + normalized, code, codeTtlSeconds, TimeUnit.SECONDS);
        emailSender.send(normalized, "登录验证码",
                "您的登录验证码是 " + code + "，" + (codeTtlSeconds / 60) + " 分钟内有效。");
    }

    public void verifyAndConsume(String email, String code) {
        String normalized = normalize(email);
        String key = KEY_PREFIX + normalized;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!cached.equals(code.trim())) {
            throw new BizException("验证码错误");
        }
        stringRedisTemplate.delete(key);
    }

    static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
