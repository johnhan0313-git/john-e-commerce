package com.john.ecommerce.module.user.service;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.module.user.entity.UserIdentity;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import com.john.ecommerce.module.user.mapper.UserIdentityMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceTest {

    @Mock UserIdentityMapper userIdentityMapper;
    @InjectMocks UserIdentityService service;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void ensureInsertsWhenMissing() {
        when(userIdentityMapper.selectByUserAndCode(10L, IdentityCodes.BUYER)).thenReturn(null);

        service.ensure(10L, IdentityCodes.BUYER, 1L);

        ArgumentCaptor<UserIdentity> cap = ArgumentCaptor.forClass(UserIdentity.class);
        verify(userIdentityMapper).insert(cap.capture());
        assertThat(cap.getValue().getUserId()).isEqualTo(10L);
        assertThat(cap.getValue().getIdentityCode()).isEqualTo(IdentityCodes.BUYER);
        assertThat(cap.getValue().getStatus()).isEqualTo(1);
        assertThat(cap.getValue().getTenantId()).isEqualTo(1L);
    }

    @Test
    void ensureReactivatesDisabled() {
        UserIdentity existing = new UserIdentity();
        existing.setId(1L);
        existing.setUserId(10L);
        existing.setIdentityCode(IdentityCodes.OPS);
        existing.setStatus(0);
        when(userIdentityMapper.selectByUserAndCode(10L, IdentityCodes.OPS)).thenReturn(existing);

        service.ensure(10L, IdentityCodes.OPS, 1L);

        verify(userIdentityMapper).updateById(existing);
        assertThat(existing.getStatus()).isEqualTo(1);
        verify(userIdentityMapper, never()).insert(any(UserIdentity.class));
    }

    @Test
    void listActiveCodesUsesIgnoredQuery() {
        when(userIdentityMapper.listActiveCodesByUserId(1L))
                .thenReturn(List.of(IdentityCodes.BUYER, IdentityCodes.OPS));

        assertThat(service.listActiveCodes(1L)).containsExactly(IdentityCodes.BUYER, IdentityCodes.OPS);
    }

    @Test
    void hasChecksActiveCount() {
        when(userIdentityMapper.countActive(eq(1L), eq(IdentityCodes.OPS))).thenReturn(1L);
        assertThat(service.has(1L, IdentityCodes.OPS)).isTrue();
    }
}
