package com.john.ecommerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        TenantContext.setTenantId(1L);
        when(userIdentityMapper.selectOne(any())).thenReturn(null);

        service.ensure(10L, IdentityCodes.BUYER);

        ArgumentCaptor<UserIdentity> cap = ArgumentCaptor.forClass(UserIdentity.class);
        verify(userIdentityMapper).insert(cap.capture());
        assertThat(cap.getValue().getUserId()).isEqualTo(10L);
        assertThat(cap.getValue().getIdentityCode()).isEqualTo(IdentityCodes.BUYER);
        assertThat(cap.getValue().getStatus()).isEqualTo(1);
        assertThat(cap.getValue().getTenantId()).isEqualTo(1L);
    }

    @Test
    void ensureReactivatesDisabled() {
        TenantContext.setTenantId(1L);
        UserIdentity existing = new UserIdentity();
        existing.setId(1L);
        existing.setUserId(10L);
        existing.setIdentityCode(IdentityCodes.OPS);
        existing.setStatus(0);
        when(userIdentityMapper.selectOne(any())).thenReturn(existing);

        service.ensure(10L, IdentityCodes.OPS);

        verify(userIdentityMapper).updateById(existing);
        assertThat(existing.getStatus()).isEqualTo(1);
        verify(userIdentityMapper, never()).insert(any(UserIdentity.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listActiveCodesFiltersAndOrders() {
        UserIdentity buyer = new UserIdentity();
        buyer.setIdentityCode(IdentityCodes.BUYER);
        UserIdentity ops = new UserIdentity();
        ops.setIdentityCode(IdentityCodes.OPS);
        when(userIdentityMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(buyer, ops));

        assertThat(service.listActiveCodes(1L)).containsExactly(IdentityCodes.BUYER, IdentityCodes.OPS);
    }

    @Test
    void hasChecksActiveCount() {
        when(userIdentityMapper.selectCount(any())).thenReturn(1L);
        assertThat(service.has(1L, IdentityCodes.OPS)).isTrue();
    }
}
