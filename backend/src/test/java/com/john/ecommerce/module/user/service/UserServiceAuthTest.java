package com.john.ecommerce.module.user.service;

import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.user.dto.EmailCodeSendDTO;
import com.john.ecommerce.module.user.dto.LoginDTO;
import com.john.ecommerce.module.user.dto.LoginVO;
import com.john.ecommerce.module.user.entity.User;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import com.john.ecommerce.module.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthTest {

    @Mock UserMapper userMapper;
    @Mock EmailCodeService emailCodeService;
    @Mock UserIdentityService userIdentityService;
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
    void loginAutoRegistersMallUserWithBuyerIdentity() {
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
        when(userIdentityService.listActiveCodes(99L)).thenReturn(List.of(IdentityCodes.BUYER));

        LoginVO vo = userService.login(dto, 1L);

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(cap.capture());
        assertThat(cap.getValue().getEmail()).isEqualTo("buyer@example.com");
        assertThat(cap.getValue().getTenantId()).isEqualTo(1L);
        verify(userIdentityService).ensureBuyer(99L);
        assertThat(vo.getUser().getIdentities()).containsExactly(IdentityCodes.BUYER);
        assertThat(vo.getToken()).isNotBlank();
        verify(emailCodeService).consume("buyer@example.com");
    }

    @Test
    void loginMerchantGrantsBuyerAndSeller() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("seller@example.com");
        dto.setCode("123456");
        dto.setPortal("merchant");

        User user = new User();
        user.setId(5L);
        user.setEmail("seller@example.com");
        user.setTenantId(1L);
        user.setStatus(1);
        when(userMapper.selectByEmail("seller@example.com")).thenReturn(user);
        when(userIdentityService.listActiveCodes(5L))
                .thenReturn(List.of(IdentityCodes.BUYER, IdentityCodes.SELLER));

        LoginVO vo = userService.login(dto, 1L);

        verify(userIdentityService).ensureBuyer(5L);
        verify(userIdentityService).ensureSeller(5L);
        assertThat(vo.getUser().getIdentities()).contains(IdentityCodes.BUYER, IdentityCodes.SELLER);
    }

    @Test
    void loginAdminRejectsWithoutOpsIdentity() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("buyer@example.com");
        dto.setCode("123456");
        dto.setPortal("admin");

        User buyer = new User();
        buyer.setId(2L);
        buyer.setEmail("buyer@example.com");
        buyer.setTenantId(1L);
        buyer.setStatus(1);
        when(userMapper.selectByEmail("buyer@example.com")).thenReturn(buyer);
        when(userIdentityService.listActiveCodes(2L)).thenReturn(List.of(IdentityCodes.BUYER));

        assertThatThrownBy(() -> userService.login(dto, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("管理后台");
        verify(emailCodeService, never()).consume(anyString());
        verify(userIdentityService, never()).ensureBuyer(eq(2L));
    }
}
