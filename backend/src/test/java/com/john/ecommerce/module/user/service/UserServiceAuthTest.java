package com.john.ecommerce.module.user.service;

import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.user.dto.EmailCodeSendDTO;
import com.john.ecommerce.module.user.dto.LoginDTO;
import com.john.ecommerce.module.user.dto.LoginVO;
import com.john.ecommerce.module.user.entity.User;
import com.john.ecommerce.module.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthTest {

    @Mock UserMapper userMapper;
    @Mock EmailCodeService emailCodeService;
    @InjectMocks UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "jwtSecret", "john-ecommerce-test-jwt-secret-change-me-32b+");
        ReflectionTestUtils.setField(userService, "jwtExpireMs", 3600000L);
        ReflectionTestUtils.setField(userService, "defaultTenantId", 1L);
    }

    @Test
    void sendCodeAllowsUnregisteredMallEmail() {
        EmailCodeSendDTO dto = new EmailCodeSendDTO();
        dto.setEmail("new@example.com");
        dto.setPortal("mall");
        when(userMapper.selectByEmail("new@example.com")).thenReturn(null);

        userService.sendLoginCode(dto);

        verify(emailCodeService).sendLoginCode("new@example.com");
    }

    @Test
    void sendCodeRejectsUnregisteredAdminEmail() {
        EmailCodeSendDTO dto = new EmailCodeSendDTO();
        dto.setEmail("new@example.com");
        dto.setPortal("admin");
        when(userMapper.selectByEmail("new@example.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.sendLoginCode(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("未注册");
        verify(emailCodeService, never()).sendLoginCode(anyString());
    }

    @Test
    void loginAutoRegistersMallUser() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("Buyer@Example.com");
        dto.setCode("123456");
        dto.setPortal("mall");

        when(userMapper.selectByEmail("buyer@example.com")).thenReturn(null);
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        LoginVO vo = userService.login(dto, 1L);

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(cap.capture());
        assertThat(cap.getValue().getEmail()).isEqualTo("buyer@example.com");
        assertThat(cap.getValue().getUserType()).isEqualTo(0);
        assertThat(cap.getValue().getTenantId()).isEqualTo(1L);
        assertThat(cap.getValue().getNickname()).isEqualTo("buyer");
        assertThat(vo.getToken()).isNotBlank();
        assertThat(vo.getUser().getId()).isEqualTo(99L);
        verify(emailCodeService).verify("buyer@example.com", "123456");
        verify(emailCodeService).consume("buyer@example.com");
    }

    @Test
    void loginAdminRejectsBuyerUserType() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("buyer@example.com");
        dto.setCode("123456");
        dto.setPortal("admin");

        User buyer = new User();
        buyer.setId(2L);
        buyer.setEmail("buyer@example.com");
        buyer.setTenantId(1L);
        buyer.setUserType(0);
        buyer.setStatus(1);
        when(userMapper.selectByEmail("buyer@example.com")).thenReturn(buyer);

        assertThatThrownBy(() -> userService.login(dto, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("管理后台");
        verify(emailCodeService, never()).consume(anyString());
    }
}
