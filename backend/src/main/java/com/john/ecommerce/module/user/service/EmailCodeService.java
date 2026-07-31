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

    /** 校验验证码，不消费（登录发 token 失败时可重试同一验证码）。 */
    public void verify(String email, String code) {
        String cached = getRequired(email);
        if (!cached.equals(code.trim())) {
            throw new BizException("验证码错误");
        }
    }

    public void consume(String email) {
        stringRedisTemplate.delete(KEY_PREFIX + normalize(email));
    }

    /** 校验并消费；仅在后续步骤不会失败时使用。 */
    public void verifyAndConsume(String email, String code) {
        verify(email, code);
        consume(email);
    }

    private String getRequired(String email) {
        String key = KEY_PREFIX + normalize(email);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        return cached;
    }

    static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
