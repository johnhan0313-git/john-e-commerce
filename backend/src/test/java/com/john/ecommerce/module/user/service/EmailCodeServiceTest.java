package com.john.ecommerce.module.user.service;

import com.john.ecommerce.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailCodeServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> values;
    @Mock EmailSender emailSender;
    @InjectMocks EmailCodeService service;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(values);
        ReflectionTestUtils.setField(service, "codeTtlSeconds", 300L);
        ReflectionTestUtils.setField(service, "fixedCode", "123456");
    }

    @Test
    void sendLoginCodeStoresFixedCodeAndEmails() {
        service.sendLoginCode("JohnHan0313@gmail.com");

        verify(values).set(eq("auth:email-code:johnhan0313@gmail.com"), eq("123456"), eq(300L), eq(TimeUnit.SECONDS));
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("johnhan0313@gmail.com"), anyString(), body.capture());
        assertThat(body.getValue()).contains("123456");
    }

    @Test
    void verifyAndConsumeAcceptsMatchingCode() {
        when(values.get("auth:email-code:a@b.com")).thenReturn("123456");
        service.verifyAndConsume("a@b.com", "123456");
        verify(redis).delete("auth:email-code:a@b.com");
    }

    @Test
    void verifyRejectsWrongCode() {
        when(values.get("auth:email-code:a@b.com")).thenReturn("123456");
        assertThatThrownBy(() -> service.verifyAndConsume("a@b.com", "000000"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("验证码错误");
    }
}
